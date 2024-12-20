var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');
const { extractToken, getRoleFromToken, getRoleAndUserFromToken, isReqRole } = require('./shared');

/*
 * GET
 */
router.get('/', extractToken, getRoleFromToken, userController.list);
router.get('/delays', extractToken, getRoleAndUserFromToken, userController.listUserDelays);
router.get('/:id', extractToken, getRoleFromToken, userController.show);


/*
 * POST
 */
router.post('/', userController.create);
router.post('/createFromApp', extractToken, getRoleAndUserFromToken, isReqRole("admin"), userController.createFromApp);
router.post('/login', userController.login);
router.post('/twoFaSetup', extractToken, getRoleAndUserFromToken, userController.twoFaSetup);
router.post('/twoFaLogin', userController.twoFaLogin);
router.post('/verify2Fa', extractToken, getRoleAndUserFromToken, userController.verify2Fa);
router.post('/delays', extractToken, getRoleAndUserFromToken, userController.createUserDelay);

/*
 * PUT
 */
router.put('/:id/updateFromApp', extractToken, getRoleAndUserFromToken, isReqRole("admin"),  userController.updateFromApp);
router.put('/:id', extractToken, getRoleFromToken, isReqRole("admin"),  userController.update);

/*
 * DELETE
 */
router.delete('/token', extractToken, getRoleAndUserFromToken, userController.deleteToken); 
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("admin"), userController.remove);
router.delete('/delays/:id', extractToken, getRoleAndUserFromToken, userController.deleteUserDelay);
router.delete('/:userId/tokens', extractToken, getRoleFromToken, isReqRole("admin"), userController.deleteAllTokens); 

module.exports = router;