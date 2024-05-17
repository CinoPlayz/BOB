// https://www.npmjs.com/package/express-bearer-token
const bearerToken = require('express-bearer-token');
var UserModel = require('../models/userModel.js');

module.exports = {
    handleError: function (err, code, message, res) {
        return res.status(code).json({
            message: message,
            error: err
        });
    }    
}