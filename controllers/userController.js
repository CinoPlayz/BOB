var UserModel = require('../models/userModel.js');
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
            tokens: req.body.tokens || [], // polje tokenov
            '2faEnabled': req.body['2faEnabled'],
            '2faSecret': req.body['2faEnabled'] ? req.body['2faSecret'] : undefined, // doda samo, če je 2faEnabled enak true
            role: req.body.role || 'user'
        });


        try {
            const user = await userToCreate.save();
            return res.status(201).json(user);
        }
        catch (err) {
            shared.handleError(err, 500, "Error when creating user", res)
        }
    }, 

    /**
     * userController.login()
     */
    login: async function (req, res, next) {
        try {
            const { username, password } = req.body;

            const user = await UserModel.authenticate(username, password);

            if (!user) {
                const err = new Error('Wrong username or password');
                err.status = 401;
                return next(err);
            }

            return res.json(user);
        } catch (err) {
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
};