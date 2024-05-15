var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');
const { extractToken } = require('../controllers/shared');
const { getRoleFromToken } = require('../controllers/shared');

/*
 * GET
 */
router.get('/', extractToken, getRoleFromToken, userController.list);
router.get('/:id', extractToken, getRoleFromToken, userController.show);


/*
 * POST
 */
router.post('/', userController.create);
router.post('/login', userController.login);
router.post('/:id/TwoFaSetup', userController.twoFaSetup);
router.post('/TwoFaLogin', userController.twoFaLogin);
/*
 * PUT
 */
router.put('/:id', extractToken, getRoleFromToken, userController.update);

/*
 * DELETE
 */
router.delete('/:id', extractToken, getRoleFromToken, userController.remove);

module.exports = router;