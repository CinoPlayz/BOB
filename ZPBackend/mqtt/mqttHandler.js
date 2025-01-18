const client = require('./mqttClient');
const UserModel = require('../models/userModel');
const crypto = require('crypto');
const OTPAuth = require('otpauth');
const fs = require('fs');
const { exec } = require('child_process');
const PassengersModel = require('../models/PassengersModel.js');
const SeatsModel = require('../models/seatsModel.js');
const TrainLocHistory = require('../models/trainLocHistoryModel.js');
const RouteModel = require('../models/routeModel.js');
const MessageModel = require('../models/messageModel.js');

// Handle Incoming MQTT Messages for Login
client.on('message', async (topic, message) => {
    console.log('Received MQTT message on topic:', topic);

    if (topic === 'app/login/request') {
        const { username, password } = JSON.parse(message.toString());

        try {
            const user = await UserModel.authenticate(username, password);

            if (user['2faEnabled']) {
                // Checks if login token exists
                const existingLoginTokenIndex = user.tokens.findIndex(token => token.type === 'login' && new Date(token.expiresOn) > new Date());

                if (existingLoginTokenIndex !== -1) {
                    // Update existing login token
                    const existingLoginToken = user.tokens[existingLoginTokenIndex];
                    existingLoginToken.token = crypto.randomBytes(32).toString('hex');
                    existingLoginToken.expiresOn = new Date(Date.now() + 60 * 60000); // 1 hour
                } else {
                    const loginToken = {
                        token: crypto.randomBytes(32).toString('hex'),
                        expiresOn: new Date(Date.now() + 60 * 60000), // 1 hour
                        type: 'login'
                    };
                    user.tokens.push(loginToken);
                }
                await user.save();

                // Publish the 2FA response with login token
                client.publish(`app/login/response/${username}`, JSON.stringify({
                    message: '2FA enabled, provide the OTP code',
                    loginToken: user.tokens.find(token => token.type === 'login').token
                }));
            } else {
                const allToken = {
                    token: crypto.randomBytes(32).toString('hex'),
                    expiresOn: new Date(Date.now() + 14 * 24 * 3600 * 1000), // 14 days
                    type: 'all'
                };
                user.tokens.push(allToken);

                await user.save();

                // Publish the successful login response with token
                client.publish(`app/login/response/${username}`, JSON.stringify({
                    token: user.tokens.find(token => token.type === 'all').token
                }));
            }
        } catch (err) {
            console.error('Login Error:', err);
            client.publish(`app/login/response/${username}`, JSON.stringify({
                success: false,
                message: 'Error during login'
            }));
        }
    }

    // Handling 2FA request (after receiving the login token)
    if (topic === 'app/twofa/request') {
        const { loginToken, otpCode } = JSON.parse(message.toString());

        console.log(loginToken, otpCode)

        if (!otpCode) {
            return client.publish(`app/twofa/response/${loginToken}`, JSON.stringify({
                success: false,
                message: 'OTP code is missing or invalid'
            }));
        }

        try {
            // Find user by login token
            const user = await UserModel.findOne({ 'tokens.token': loginToken, 'tokens.expiresOn': { $gt: new Date() }, 'tokens.type': 'login' });

            if (!user) {
                return client.publish(`app/twofa/response/${loginToken}`, JSON.stringify({
                    success: false,
                    message: 'Invalid or expired login token'
                }));
            }

            // Validate OTP code
            const totpInstance = new OTPAuth.TOTP({
                secret: OTPAuth.Secret.fromBase32(user['2faSecret']),
                issuer: 'BOB',
                label: `${user.username} Login`
            });

            const isValid = totpInstance.validate({ token: otpCode, window: 1 });
            if (!isValid) {
                return client.publish(`app/twofa/response/${loginToken}`, JSON.stringify({
                    success: false,
                    message: 'Invalid OTP code'
                }));
            }

            // OTP validated, issue a long-term token
            const allToken = {
                token: crypto.randomBytes(32).toString('hex'),
                expiresOn: new Date(Date.now() + 14 * 24 * 3600 * 1000), // 14 days
                type: 'all'
            };

            user.tokens.push(allToken);
            await user.save();

            client.publish(`app/twofa/response/${loginToken}`, JSON.stringify({
                token: allToken.token
            }));
        } catch (err) {
            console.error('2FA Error:', err);
            client.publish(`app/twofa/response/${loginToken}`, JSON.stringify({
                success: false,
                message: 'Error during 2FA verification'
            }));
        }
    }

    if (topic === 'app/logout/request') {
        const { username, token } = JSON.parse(message.toString());

        try {
            const user = await UserModel.findOne({ username });

            if (!user) {
                return client.publish(`app/logout/response/${username}`, JSON.stringify({
                    success: false,
                    message: 'User not found'
                }));
            }

            // Remove the token from the user's tokens array
            const tokenIndex = user.tokens.findIndex(t => t.token === token);
            if (tokenIndex === -1) {
                return client.publish(`app/logout/response/${username}`, JSON.stringify({
                    success: false,
                    message: 'Token not found'
                }));
            }

            // Pull the token from the array and save the user
            user.tokens.splice(tokenIndex, 1);
            await user.save();

            // Send a success response back to the client
            client.publish(`app/logout/response/${username}`, JSON.stringify({
                success: true,
                message: 'Token deleted successfully: User Logged Out'
            }));

        } catch (err) {
            console.error('Logout Error:', err);
            client.publish(`app/logout/response/${username}`, JSON.stringify({
                success: false,
                message: 'Error during logout process'
            }));
        }
    }

    if (topic === 'app/register/request') {
        const { email, username, password } = JSON.parse(message.toString());

        if (!username || !password || !email) {
            return client.publish(`app/register/response/${username}`, JSON.stringify({
                success: false,
                message: "Missing required fields"
            }));
        }

        try {
            const userToCreate = new UserModel({
                username: username,
                password: password,
                email: email,
                tokens: [],
                '2faEnabled': false,
                '2faSecret': undefined,
                role: 'user'
            });

            const user = await userToCreate.save();

            // Send success response back to the client
            client.publish(`app/register/response/${username}`, JSON.stringify({
                success: true,
                message: "User created successfully",
                // user: user
            }));
        } catch (err) {
            console.error('Error when creating user:', err);
            client.publish(`app/register/response/${username}`, JSON.stringify({
                success: false,
                message: "Error when creating user"
            }));
        }
    }

    if (topic === 'app/passengers/count/request') {
        try {
            const { token, username, imageBase64, trainType, wagonNumber } = JSON.parse(message.toString());

            // Check for missing fields
            if (!token || !username || !imageBase64 || !trainType || !wagonNumber) {
                return client.publish(`app/passengers/count/response/${username || 'unknown'}`, JSON.stringify({
                    success: false,
                    message: "Missing required fields"
                }));
            }

            // Authenticate user token
            const user = await UserModel.findOne({ "tokens.token": token });
            if (!user) {
                return client.publish(`app/passengers/count/response/${username}`, JSON.stringify({
                    success: false,
                    message: "Unauthorized access"
                }));
            }

            // Retrieve the number of seats for the given train type and wagon number
            const seatData = await SeatsModel.findOne({ type: trainType, wagonNumber: wagonNumber });
            if (!seatData) {
                return client.publish(`app/passengers/count/response/${username}`, JSON.stringify({
                    success: false,
                    message: "Seat data not found"
                }));
            }

            const numOfSeats = seatData.countOfSeats;

            // Save image temporarily
            const imageBuffer = Buffer.from(imageBase64, 'base64');
            const imagePath = `uploads/${username}_${Date.now()}.jpg`;
            fs.writeFileSync(imagePath, imageBuffer);

            // Process image with Python script
            exec(`conda run -n PRO python ../ZPOccupancyDetection/image_processing.py count ${imagePath}`, (error, stdout, stderr) => {
                // Clean up temp image
                fs.unlinkSync(imagePath);

                if (error) {
                    console.error(`Error executing script: ${stderr}`);
                    return client.publish(`app/passengers/count/response/${username}`, JSON.stringify({
                        success: false,
                        message: "Error during image processing"
                    }));
                }

                const numOfPeople = parseInt(stdout.trim());

                // Send back result with both passenger count and seat count
                client.publish(`app/passengers/count/response/${username}`, JSON.stringify({
                    success: true,
                    numOfPeople: numOfPeople,
                    numOfSeats: numOfSeats
                }));
            });
        } catch (err) {
            console.error('Error processing passenger count:', err);
            client.publish(`app/passengers/count/response/unknown`, JSON.stringify({
                success: false,
                message: "Invalid request format or server error"
            }));
        }
    }


    if (topic === 'app/passengers/create/request') {
        try {
            const { token, username, timeOfRequest, coordinatesOfRequest, guessedOccupancyRate, realOccupancyRate, route } = JSON.parse(message.toString());

            // Check for missing fields
            if (!token || !timeOfRequest || !coordinatesOfRequest || !guessedOccupancyRate || !realOccupancyRate || !route) {
                console.log("Missing required fields, sending failure response.");
                return client.publish(`app/passengers/create/response`, JSON.stringify({
                    success: false,
                    message: "Missing required fields"
                }));
            }

            // Retrieve user from token
            const user = await UserModel.findOne({
                "tokens.token": token,
            });

            if (!user) {
                console.log("User not found, sending failure response.");
                return client.publish(`app/passengers/create/response`, JSON.stringify({
                    success: false,
                    message: "User not found"
                }));
            }

            // Create new passenger record
            const newPassengers = new PassengersModel({
                timeOfRequest,
                coordinatesOfRequest,
                guessedOccupancyRate,
                realOccupancyRate,
                route,
                postedByUser: user._id
            });

            const savedPassenger = await newPassengers.save();

            // Log success before sending the response
            console.log("Passenger created successfully, sending success response.");

            // Respond with success
            const responseMessage = JSON.stringify({
                success: true,
                passengerId: savedPassenger._id
            });

            console.log("Publishing response to topic:", `app/passengers/create/response/${user.username}`);
            client.publish(`app/passengers/create/response/${user.username}`, responseMessage, (err) => {
                if (err) {
                    console.error("Error publishing response:", err);
                } else {
                    console.log("Response successfully published.");
                }
            });

        } catch (err) {
            console.error('Error processing passenger creation:', err);
            client.publish(`app/passengers/create/response`, JSON.stringify({
                success: false,
                message: "Error when creating passengers record"
            }));
        }
    }

    if (topic === 'app/trains/trainHistoryByDateRange/request') {
        try {
            const { startDate, endDate, uuid } = JSON.parse(message.toString());

            if (!startDate || !endDate || !uuid) {
                console.log("Missing required fields, sending failure response.");
                return client.publish(`app/trains/trainHistoryByDateRange/response/${uuid}`, JSON.stringify({
                    success: false,
                    message: "Missing required date fields"
                }));
            }

            const start = new Date(startDate);
            const end = new Date(endDate);

            // Fetch train history data within the specified date range
            const trainData = await TrainLocHistory.find({
                timeOfRequest: {
                    $gte: start,
                    $lte: end
                }
            });

            let occupancyData = await PassengersModel.find({
                timeOfRequest: {
                    $gte: start,
                    $lte: end
                }
            })

            const routes = await RouteModel.find();

            // Map routeId to trainNumber
            const routeMap = routes.reduce((map, route) => {
                map[route._id.toString()] = route.trainNumber;
                return map;
            }, {});

            // Replace routeId with trainNumber in occupancy data
            occupancyData = occupancyData.map(passenger => {
                return {
                    ...passenger.toObject(),
                    route: routeMap[passenger.route.toString()] || passenger.route
                };
            });

            console.log("Train history and occupancy data fetched successfully, sending response. No. of trainLocHistory: ", trainData.length, "No. of occupancy: ", occupancyData.length);

            const responseMessage = JSON.stringify({
                success: true,
                trainHistory: trainData,
                occupancy: occupancyData
            });

            console.log("Publishing response to topic:", `app/trains/trainHistoryByDateRange/response/${uuid}`);
            client.publish(`app/trains/trainHistoryByDateRange/response/${uuid}`, responseMessage, (err) => {
                if (err) {
                    console.error("Error publishing response:", err);
                } else {
                    console.log("Response successfully published.");
                }
            });

        } catch (err) {
            console.error('Error processing train history request:', err);
            client.publish(`app/trains/trainHistoryByDateRange/response`, JSON.stringify({
                success: false,
                message: "Error when fetching train history"
            }));
        }
    }


    if (topic === 'app/messages/create/request') {
        try {
            const { token, username, timeOfMessage, message: incomingMessage, category } = JSON.parse(message.toString());

            // Check for missing fields
            if (!token || !timeOfMessage || !incomingMessage || !category) {
                console.log("Missing required fields, sending failure response.");
                return client.publish(`app/messages/create/response`, JSON.stringify({
                    success: false,
                    message: "Missing required fields"
                }));
            }

            // Retrieve user from token
            const user = await UserModel.findOne({
                "tokens.token": token,
            });

            if (!user) {
                console.log("User not found, sending failure response.");
                return client.publish(`app/messages/create/response`, JSON.stringify({
                    success: false,
                    message: "User not found"
                }));
            }

            // Create new message record
            const newMessage = new MessageModel({
                timeOfMessage,
                postedByUser: user._id,
                message: incomingMessage,
                category
            });

            const savedMessage = await newMessage.save();

            // Log success before sending the response
            console.log("Message created successfully, sending success response.");

            // Respond with success
            const responseMessage = JSON.stringify({
                success: true,
                messageId: savedMessage._id
            });

            console.log("Publishing response to topic:", `app/messages/create/response/${user.username}`);
            client.publish(`app/messages/create/response/${user.username}`, responseMessage, (err) => {
                if (err) {
                    console.error("Error publishing response:", err);
                } else {
                    console.log("Response successfully published.");
                }
            });

        } catch (err) {
            console.error('Error processing message creation:', err);
            client.publish(`app/message/create/response`, JSON.stringify({
                success: false,
                message: "Error when creating message record"
            }));
        }
    }


    if (topic === 'app/messages/retrieve/all/request') {
        try {
            const { token, uuid } = JSON.parse(message.toString());

            if (!token, !uuid) {
                console.log("Missing required fields, sending failure response.");
                return client.publish(`app/messages/retrieve/all/response`, JSON.stringify({
                    success: false,
                    message: "Missing required date fields"
                }));
            }

            const messages = await MessageModel.find()
                .populate('postedByUser')  // Replace the ObjectId with the username
                .sort({ timeOfMessage: 1 })
                .exec();

            const formattedMessages = messages.map(message => ({
                timeOfMessage: message.timeOfMessage,
                message: message.message,
                category: message.category,
                postedByUser: message.postedByUser.username, // Replacing ObjectId with username
            }));

            console.log("Messages data fetched successfully, sending response. No. of messages: ", messages.length);

            const responseMessage = JSON.stringify({
                success: true,
                messages: formattedMessages
            });

            // console.log('Messages with usernames:', formattedMessages);
            console.log("Publishing response to topic:", `app/messages/retrieve/all/response/${uuid}`);
            client.publish(`app/messages/retrieve/all/response/${uuid}`, responseMessage, (err) => {
                if (err) {
                    console.error("Error publishing response:", err);
                } else {
                    console.log("Response successfully published.");
                }
            });

        } catch (err) {
            console.error('Error processing messages request:', err);
            client.publish(`app/messages/retrieve/all/response`, JSON.stringify({
                success: false,
                message: "Error when fetching messages"
            }));
        }
    }
});