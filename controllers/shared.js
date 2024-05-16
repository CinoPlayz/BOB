// https://www.npmjs.com/package/express-bearer-token
const bearerToken = require('express-bearer-token');
var UserModel = require('../models/userModel.js');

module.exports = {
    handleError: function (err, code, message, res) {
        return res.status(code).json({
            message: message,
            error: err
        });
    },

    /**
     * Extract Bearer Token middleware.
     * Use express-bearer-token library
     * Token must be provided following Bearer token format (RFC6750)
     * Saved to req.token
     */
    extractToken: bearerToken(),

    /**
     * Get user role from bearer token
     * Get token extracted with extractToken() and saved in req.token
     * Check received token against userbase
     * Return 401 HTTP code and write to console on error.
     */
    getRoleFromToken: async (req, res, next) => {
        // Check if empty token
        if (!req.token) {
            console.log("Error. getRoleFromToken(), token empty");
            return res.status(401).json({ message: 'Unauthorized access.', error: null });
        }

        const currentToken = req.token;

        // Find the user with the matching token
        const user = await UserModel.findOne({
            "tokens.token": currentToken,
        })

        if (!user) {
            console.log("Error. getRoleFromToken(), user with provided token not found");
            return res.status(401).json({ message: 'Unauthorized access.', error: null });
        }

        // Find the matching token document within the user's tokens array
        const userToken = user.tokens.find((t) => t.token === currentToken);
        if (!userToken) {
            console.log("Error. getRoleFromToken(), no userToken match");
            return res.status(401).json({ message: 'Unauthorized access', error: null });
        }

        // Check if the token has expired
        if (userToken.expiresOn < Date.now()) {
            console.log("Error. getRoleFromToken(), userToken expired");
            return res.status(401).json({ message: 'Unauthorized access', error: null });
        }

        // Set the user role on the request object
        req.role = user.role;

        next();
    },

    /**
     * Check if request role is specific role
     */
    
    isReqRole : (role) => {
        return (req, res, next) => {
            // Check if empty role
            if (!req.role) {
                console.log("Error. getRoleFromToken(), role empty");
                return res.status(401).json({ message: 'Unauthorized access.', error: null });
            }

            //Check if role
            if(req.role != role){
                return res.status(401).json({ message: 'Unauthorized access - Insufficient role', error: null });
            }

            next();
        }
    }
}