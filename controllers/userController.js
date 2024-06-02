var UserModel = require('../models/userModel.js');
var DelayModel = require('../models/delayModel.js');
const crypto = require('node:crypto')
const OTPAuth = require('otpauth');
var shared = require('./shared.js');
const delayModel = require('../models/delayModel.js');

module.exports = {

    /**
     * userController.list()
     */
    list: async function (req, res) {
        try {
            const users = await UserModel.find();

            return res.json(users);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when getting users", err);
        }

    },


    /**
    * userController.show()
    */
    show: async function (req, res) {
        var id = req.params.id;

        try {
            const user = await UserModel.findOne({ _id: id });

            if (!user) {
                return shared.handleError(res, 404, "No such user", null);
            }

            return res.json(user);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when getting user", err);
        }
    },

    /**
     * userController.list()
     */
    listDelays: async function (req, res) {
        try {
            let user = req.user;
            if (!user["traveledDelays"]) {
                res.json([]);
            } else {
                res.json(user.traveledDelays);
            }
        }
        catch (err) {
            console.log(err);
            return shared.handleError(res, 500, "Error when getting users", err);
        }

    },


    /**
     * userController.create()
     */
    create: async function (req, res) {

        if (!req.body.username || !req.body.password || !req.body.email) {
            return shared.handleError(res, 400, "Missing required fields", null);
        }

        var userToCreate = new UserModel({
            username: req.body.username,
            password: req.body.password,
            email: req.body.email,
            tokens: [],
            '2faEnabled': false,
            '2faSecret': undefined,
            role: 'user'
        });


        try {
            const user = await userToCreate.save();
            return res.status(201).json(user);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when creating user", err);
        }
    },

    createFromApp: async function (req, res) {

        if (!req.body.username || !req.body.password || !req.body.email) {
            return shared.handleError(res, 400, "Missing required fields", null);
        }

        var userToCreate = new UserModel({
            username: req.body.username,
            password: req.body.password,
            email: req.body.email,
            tokens: [],
            '2faEnabled': false,
            '2faSecret': undefined,
            role: req.body.role
        });


        try {
            const user = await userToCreate.save();
            return res.status(201).json(user);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when creating user", err);
        }
    },

    login: async function (req, res) {
        try {
            const { username, password } = req.body;
            const user = await UserModel.authenticate(username, password);

            if (user['2faEnabled']) {
                //Checks if login token exists
                const existingLoginTokenIndex = user.tokens.findIndex(token => token.type === 'login' && new Date(token.expiresOn) > new Date());

                if (existingLoginTokenIndex !== -1) {
                    //Update existing login token
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
                return res.json({ message: '2FA enabled, provide the OTP code', loginToken: user.tokens.find(token => token.type === 'login').token });
            } else {

                const allToken = {
                    token: crypto.randomBytes(32).toString('hex'),
                    expiresOn: new Date(Date.now() + 14 * 24 * 3600 * 1000), // 14 days
                    type: 'all'
                };
                user.tokens.push(allToken);

                await user.save();
                return res.json({ token: user.tokens.find(token => token.type === 'all').token });
            }
        } catch (err) {
            return shared.handleError(res, 500, "Error while logging in", err.message);
        }
    },

    twoFaSetup: async function (req, res, next) {
        try {
            const user = req.user;

            if (!user) {
                return shared.handleError(res, 404, "User not found", null);
            }

            if (user['2faEnabled']) {
                return shared.handleError(res, 400, "2FA is already enabled", null);
            }

            const secret = new OTPAuth.Secret({ size: 32 });

            const totp = new OTPAuth.TOTP({
                secret: secret,
                issuer: "BOB",
                label: `${user.username} Login`
            });

            user['2faSecret'] = secret.base32;
            user['2faEnabled'] = true;
            await user.save();

            const uri = totp.toString();
            return res.json({ uri });
        } catch (err) {
            return shared.handleError(res, 500, "Error in TwoFaSetup", err);
        }

    },



    /**
    * userController.2faLogin()
    */
    twoFaLogin: async function (req, res) {
        try {
            const { loginToken, otpCode } = req.body;

            if (!loginToken || !otpCode) {
                return shared.handleError(res, 400, "Missing login token or OTP code", null);
            }

            //Finds user by login token
            const user = await UserModel.findOne({ 'tokens.token': loginToken, 'tokens.expiresOn': { $gt: new Date() }, 'tokens.type': 'login' });
            if (!user) {
                return shared.handleError(res, 401, "Invalid or expired login token", null);
            }

            const totp = new OTPAuth.TOTP({
                secret: OTPAuth.Secret.fromBase32(user['2faSecret']),
                issuer: "BOB",
                label: `${user.username} Login`
            });

            const delta = totp.validate({ token: otpCode, window: 1 });
            if (delta === null) {
                return shared.handleError(res, 401, "Invalid OTP code", null);
            }

            const allToken = {
                token: crypto.randomBytes(32).toString('hex'),
                expiresOn: new Date(Date.now() + 14 * 24 * 3600 * 1000), // 14 days
                type: 'all'
            };

            user.tokens.push(allToken);
            await user.save();

            return res.json({ token: allToken.token });
        } catch (err) {
            return shared.handleError(res, 500, "Invalid OTP code", err);
        }
    },

    /**
    * userController.2faLogin()
    */
    createDelay: async function (req, res) {
        try {
            const user = req.user;
            let requestDelay = req.body.delay
            let requestTime = req.body.time

            if (!requestDelay || !requestTime) {
                return shared.handleError(res, 400, "Missing required fields", null);
            }

            if (!user) {
                return shared.handleError(res, 404, "User not found", null);
            }

            if (!user['traveledDelays']) {
                user.traveledDelays = [];
            }

            let requestTimeDate = new Date(requestTime);
            if (user.traveledDelays.some(e => e.delay == requestDelay && new Date(e.time).getTime() == requestTimeDate.getTime())) {
                return shared.handleError(res, 400, "Delay already added", null);
            }

            let foundDelay = await delayModel.findOne({
                route: requestDelay, timeOfRequest: {
                    $gte: new Date(requestTimeDate).setHours(0, 0, 0, 0),
                    $lt: new Date(requestTimeDate).setHours(23, 59, 59, 999)
                }
            })

            if (foundDelay == null) {
                return shared.handleError(res, 400, "No delay saved for that route at that time", null);
            }

            let userDelay = { "delay": requestDelay, "time": requestTimeDate };
            console.log(userDelay);
            user.traveledDelays.push(userDelay);
            await user.save();

            return res.json(user.traveledDelays);
        } catch (err) {
            console.log(err);
            return shared.handleError(res, 500, "Error in creating user delay", err);
        }

    },


    /**
     * userController.update()
     */
    update: async function (req, res) {
        var id = req.params.id;

        try {
            const userFound = await UserModel.findOne({ _id: id });

            if (!userFound) {
                return shared.handleError(res, 404, "No such user", null);
            }

            userFound.username = req.body.username ? req.body.username : userFound.username;
            userFound.password = req.body.password ? req.body.password : userFound.password;
            userFound.email = req.body.email ? req.body.email : userFound.email;

            const userUpdated = await userFound.save();

            return res.json(userUpdated);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when updating user", err);
        }
    },

    updateFromApp: async function (req, res) {
        var id = req.params.id;
        // console.log(req.body)

        try {
            const userFound = await UserModel.findOne({ _id: id });

            if (!userFound) {
                return shared.handleError(res, 404, "No such user", null);
            }

            if (req.body.username !== undefined && req.body.username.trim() !== "") {
                userFound.username = req.body.username.trim();
            }

            if (req.body.email !== undefined && req.body.email.trim() !== "") {
                userFound.email = req.body.email.trim();
            }

            if (req.body.password !== undefined && req.body.password.trim() !== "") {
                userFound.password = req.body.password.trim();
            }

            if (req.body.role !== undefined && req.body.role.trim() !== "") {
                userFound.role = req.body.role.trim();
            }

            // If 2faEnabled == false && 2faSecret == empty --> disable 2fa
            if ((!req.body['2faSecret'] || req.body['2faSecret'].trim() === "" || req.body['2faSecret'] === undefined) && !req.body['2faEnabled']) {
                userFound['2faEnabled'] = false;
                delete userFound['2faSecret'];
            }


            // If 2faEnabled == true && 2faSecret != empty --> enable 2fa
            if ((req.body['2faSecret'] || req.body['2faSecret'].trim() !== "" || req.body['2faSecret'] !== undefined) && req.body['2faEnabled']) {
                userFound['2faSecret'] = req.body['2faSecret'].trim();
            }

            // Update tokens if provided in request body
            if (req.body.tokens !== undefined) {
                if (Array.isArray(req.body.tokens) && req.body.tokens.length > 0) {
                    userFound.tokens = req.body.tokens;
                } else {
                    userFound.tokens = [];
                }
            } else { // if tokens undefine, delete all tokens
                userFound.tokens = [];
            }

            // Add new tokens to existing tokens
            if (req.body.newTokens !== undefined) {
                if (Array.isArray(req.body.newTokens) && req.body.newTokens.length > 0) {
                    if (!userFound.tokens) {
                        userFound.tokens = [];
                    }
                    userFound.tokens = userFound.tokens.concat(req.body.newTokens);
                }
            }

            const userUpdated = await userFound.save();

            return res.json(userUpdated);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when updating user", err);
        }
    },

    /**
     * userController.remove()
     */
    remove: async function (req, res) {
        var id = req.params.id;

        try {
            await UserModel.findByIdAndDelete(id);

            return res.status(204).json();
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when deleting the user", err);
        }
    },

    /**
    * userController.deleteAllTokens()
    */
    deleteAllTokens: async function (req, res) {
        try {
            const { userId } = req.params;

            const user = await UserModel.findByIdAndUpdate(
                userId,
                { $set: { tokens: [] } },
                { new: true }
            );

            if (!user) {
                return shared.handleError(res, 404, "User not found", null);
            }

            return res.status(200).json({ message: "All tokens deleted successfully", user });
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting user tokens", err);
        }
    },

    /**
    * userController.deleteToken()
    */
    deleteToken: async function (req, res) {
        try {
            const user = req.user
            const token = req.token

            await user.updateOne({
                $pull: {
                    tokens: { token: token }
                }
            });

            /* const index = user.tokens.indexOf(req.token);
            if (index > -1) {
                user.tokens.splice(index, 1)
            } */

            await user.save();
            return res.status(200).json({ message: "Token deleted successfully: User Logged Out", user });
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting user token", err);
        }
    },
};