var UserModel = require('../models/userModel.js');
const crypto = require('crypto');
const OTPAuth = require('otpauth');
var shared = require('./shared.js');




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
            return shared.handleError(err, 500, "Error when getting users", res);
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
                return shared.handleError(err, 404, "No such user", res);
            }

            return res.json(user);
        }
        catch (err) {
            return shared.handleError(err, 500, "Error when getting user", res);
        }
    },


    /**
     * userController.create()
     */
    create: async function (req, res) {

        if (!req.body.username || !req.body.password || !req.body.email) {
            return res.status(400).json({ error: "Missing required fields" });
        }

        // Preverite, če je 2FA omogočen, potem mora biti prisoten tudi 2faSecret
        if (req.body['2faEnabled'] && !req.body['2faSecret']) {
            return res.status(400).json({ error: "2faSecret is required when 2faEnabled is true" });
        }


        var userToCreate = new UserModel({
			username : req.body.username,
			password : req.body.password,
			email : req.body.email,
            tokens: [], // polje tokenov
            '2faEnabled': false,
            '2faSecret': undefined, // doda samo, če je 2faEnabled enak true
            role: 'user'
        });


        try {
            const user = await userToCreate.save();
            return res.status(201).json(user);
        }
        catch (err) {
            shared.handleError(err, 500, "Error when creating user", res)
        }
    }, 

    login: async function (req, res, next) {
        try {
            const { username, password } = req.body;

            const user = await UserModel.authenticate(username, password);

            if (!user) {
                const err = new Error('Wrong username or password');
                err.status = 401;
                return next(err);
            }

            if (user['2faEnabled']) {
                // Preverba, če že obstaja začasni login token
                const existingLoginTokenIndex = user.tokens.findIndex(token => token.type === 'login' && new Date(token.expiresOn) > new Date());
                
                if (existingLoginTokenIndex !== -1) {
                    // Posodabljanje obstoječega tokena
                    const existingLoginToken = user.tokens[existingLoginTokenIndex];
                    existingLoginToken.token = crypto.randomBytes(32).toString('hex');
                    existingLoginToken.expiresOn = new Date(Date.now() + 60 * 60000); // 1 ura veljavnosti
                } else {
                    // Ustvarjanje novega začasnega login tokena
                    const loginToken = {
                        token: crypto.randomBytes(32).toString('hex'),
                        expiresOn: new Date(Date.now() + 60 * 60000), // 1 ura veljavnosti
                        type: 'login'
                    };
                    user.tokens.push(loginToken);
                }
                await user.save();
                return res.json({ message: '2FA enabled, provide the OTP code', loginToken: user.tokens.find(token => token.type === 'login').token });
            } else {
                // Preverba, če že obstaja all token
                const existingAllTokenIndex = user.tokens.findIndex(token => token.type === 'all' && new Date(token.expiresOn) > new Date());

                if (existingAllTokenIndex !== -1) {
                    // Posodabljanje obstoječega tokena
                    console.log("posodabljanje obstoječega tokena");
                    const existingAllToken = user.tokens[existingAllTokenIndex];
                    existingAllToken.token = crypto.randomBytes(32).toString('hex');
                    existingAllToken.expiresOn = new Date(Date.now() + 14 * 24 * 3600 * 1000); // 14 dni veljavnosti
                } else {
                    // Ustvarjanje novega all tokena
                    console.log("ustvarjanje novega");
                    const allToken = {
                        token: crypto.randomBytes(32).toString('hex'),
                        expiresOn: new Date(Date.now() + 14 * 24 * 3600 * 1000), // 14 dni veljavnosti
                        type: 'all'
                    };
                    user.tokens.push(allToken);
                }
                await user.save();
                return res.json({ token: user.tokens.find(token => token.type === 'all').token });
            }
        } catch (err) {
            return next(err);
        }
    },

    isLoggedIn: async function(req, res, next) {
        try {
            const userId = req.params.id; // Vzeme ID iz URL parametrov zaenkrat->zaradi testiranja
            //console.log('User ID from URL:', userId); 

            const user = await UserModel.findById(userId);
            //console.log('User found:', user); 

            if (!user) {
                return res.status(404).send('User not found');
            }
            req.user = user;
            next();
        } catch (err) {
            console.error('Error in isLoggedIn middleware:', err); 
            return res.status(500).send('Server error');
        }
    },

    twoFaSetup: [
        //validateToken->še implemetirati
        async function (req, res, next) {
            await module.exports.isLoggedIn(req, res, async function() {
                try {
                    const user = req.user;
                    //console.log('User in TwoFaSetup:', user); 

                    if (!user) {
                        return res.status(400).json({ error: "User not found" });
                    }

                    if (user['2faEnabled']) {
                        return res.status(400).json({ error: "2FA is already enabled" });
                    }

                    // Ustvari naključno 2faSecret
                    const secret = new OTPAuth.Secret({ size: 32 });
                    //console.log('Secret generated:', secret.base32); 

                    // Ustvari TOTP objekt
                    const totp = new OTPAuth.TOTP({
                        secret: secret,
                        issuer: "BOB",
                        label: `${user.username} Login`
                    });

                    // Shranjevanje 2faSecret v uporabniški račun
                    user['2faSecret'] = secret.base32;
                    user['2faEnabled'] = true;
                    await user.save();

                    // Vrni URI za Google Authenticator
                    const uri = totp.toString();
                    console.log('TOTP URI:', uri); 
                    return res.json({ uri });
                } catch (err) {
                    console.error('Error in TwoFaSetup:', err); 
                    return next(err);
                }
            });
        }
    ],


     /**
     * userController.2faLogin()
     */
     twoFaLogin: async function (req, res, next) {
        try {
            const { loginToken, otpCode } = req.body;

            if (!loginToken || !otpCode) {
                console.log('Missing login token or OTP code');
                return res.status(400).json({ error: 'Missing login token or OTP code' });
            }

            console.log('Received loginToken:', loginToken);
            console.log('Received otpCode:', otpCode);

            // Poiščemo uporabnika na podlagi login tokena                                          //expiry date mora biti večji od trenutnega časa
            const user = await UserModel.findOne({ 'tokens.token': loginToken, 'tokens.expiresOn': { $gt: new Date() }, 'tokens.type': 'login' });
            if (!user) {
                console.log('Invalid or expired login token');
                return res.status(401).json({ error: 'Invalid or expired login token' });
            }

            console.log('User found:', user.username);

            // Ustvari TOTP objekt s shranjeno skrivnostjo, da lahko preverjamo otp kode
            const totp = new OTPAuth.TOTP({
                secret: OTPAuth.Secret.fromBase32(user['2faSecret']),//upoorabimo 2faSecret, ki je bila 
                issuer: "BOB",                                      //ustvarjena med TwoFaSetup
                label: `${user.username} Login`
            });

            console.log('TOTP object created with secret:', user['2faSecret']);

            // Preveri OTP kodo
            const delta = totp.validate({ token: otpCode, window: 1 });
            if (delta === null) {
                console.log('Invalid OTP code');
                return res.status(401).json({ error: 'Invalid OTP code' });
            }

            console.log('OTP code is valid');

            // Ustvari dolgoročni all token
            const allToken = {
                token: crypto.randomBytes(32).toString('hex'),
                expiresOn: new Date(Date.now() + 14 * 24 * 3600 * 1000), // 14 dni veljavnosti
                type: 'all'
            };

            console.log('Generated new all token:', allToken.token);

            // Odstranimo prejšnji login token
            user.tokens = user.tokens.filter(token => token.token !== loginToken);

            // Dodamo novi all token
            user.tokens.push(allToken);
            await user.save();

            console.log('Saved user with new all token');

            return res.json({ token: allToken.token });
        } catch (err) {
            console.error('Error in TwofaLogin:', err);
            return next(err);
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
                return shared.handleError(err, 404, "No such user", res);
            }

            userFound.username = req.body.username ? req.body.username : userFound.username;
			userFound.password = req.body.password ? req.body.password : userFound.password;
			userFound.email = req.body.email ? req.body.email : userFound.email;
            const userUpdated = await userFound.save();

            return res.json(userUpdated);
        }
        catch (err) {
            return shared.handleError(err, "Error when updating user", res);
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
            return shared.handleError(err, 500, "Error when deleting the user", res);
        }
    },

     /**
     * userController.deleteAllTokens()
     */
     deleteAllTokens: async function (req, res, next) {
        try {
            const { userId } = req.params;

            // Najdi uporabnika po ID-ju in izprazni polje tokens
            const user = await UserModel.findByIdAndUpdate(
                userId,
                { $set: { tokens: [] } },
                { new: true } // Vrne posodobljen dokument
            );

            if (!user) {
                return res.status(404).json({ error: "User not found" });
            }

            return res.status(200).json({ message: "All tokens deleted successfully", user });
        } catch (err) {
            return next(err);
        }
    },
};