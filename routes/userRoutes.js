var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');
const { extractToken } = require('../controllers/shared');
const { getRoleFromToken } = require('../controllers/shared');
const { isReqRole } = require('../controllers/shared');

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
router.post('/:id/TwofaSetup', userController.TwofaSetup);

/*
 * PUT
 */
router.put('/:id', extractToken, getRoleFromToken, isReqRole("admin"),  userController.update);

/*
 * DELETE
 */
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("admin"), userController.remove);
router.delete('/:userId/tokens', extractToken, getRoleFromToken, isReqRole("admin"), userController.deleteAllTokens); // Dodajanje rute za brisanje vseh tokenov

module.exports = router;