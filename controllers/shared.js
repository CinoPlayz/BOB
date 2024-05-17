// https://www.npmjs.com/package/express-bearer-token
const bearerToken = require('express-bearer-token');
var UserModel = require('../models/userModel.js');

module.exports = {
    handleError: function (response, httpCode, message, errObject) {
        return response.status(httpCode).json({
            message: message,
            error: errObject
        });
    }    
}