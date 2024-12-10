// https://www.npmjs.com/package/express-bearer-token
const bearerToken = require('express-bearer-token');
var UserModel = require('../models/userModel.js');

module.exports = {

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
     * Return 401 HTTP code
     */
    getRoleFromToken: async (req, res, next) => {
        // Check if empty token
        if (!req.token) {
            return res.status(401).json({ message: 'Unauthorized access.', error: null });
        }

        const currentToken = req.token;

        // Find the user with the matching token
        const user = await UserModel.findOne({
            "tokens.token": currentToken,
        })

        if (!user) {
            return res.status(401).json({ message: 'Unauthorized access.', error: null });
        }

        // Find the matching token document within the user's tokens array
        const userToken = user.tokens.find((t) => t.token === currentToken);
        if (!userToken) {
            return res.status(401).json({ message: 'Unauthorized access', error: null });
        }

        // Check if the token has expired
        if (userToken.expiresOn < Date.now()) {
            return res.status(401).json({ message: 'Unauthorized access', error: null });
        }

        // Set the user role on the request object
        req.role = user.role;

        next();
    },

    /**
     * Get user from bearer token
     * Get token extracted with extractToken() and saved in req.token
     * Check received token against userbase
     * Return 401 HTTP code
     */
    getRoleAndUserFromToken: async (req, res, next) => {
        // Check if empty token
        if (!req.token) {
            return res.status(401).json({ message: 'Unauthorized access.', error: null });
        }

        const currentToken = req.token;

        // Find the user with the matching token
        const user = await UserModel.findOne({
            "tokens.token": currentToken,
        })

        if (!user) {
            return res.status(401).json({ message: 'Unauthorized access.', error: null });
        }

        // Find the matching token document within the user's tokens array
        const userToken = user.tokens.find((t) => t.token === currentToken);
        if (!userToken) {
            return res.status(401).json({ message: 'Unauthorized access', error: null });
        }

        // Check if the token has expired
        if (userToken.expiresOn < Date.now()) {
            return res.status(401).json({ message: 'Unauthorized access', error: null });
        }

        // Set the user user on the request object
        req.user = user;
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
                return res.status(401).json({ message: 'Unauthorized access.', error: null });
            }

            //Check if role
            if(req.role != role && req.role != "admin"){
                return res.status(403).json({ message: 'Unauthorized access - Insufficient role', error: null });
            }

            next();
        }
    }
}