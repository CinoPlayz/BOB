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
router.post('/:id/TwofaSetup', userController.TwofaSetup);
/*
 * PUT
 */
router.put('/:id', extractToken, getRoleFromToken, userController.update);

/*
 * DELETE
 */
router.delete('/:id', extractToken, getRoleFromToken, userController.remove);
router.delete('/:id', extractToken, getRoleFromToken, userController.remove);
router.delete('/:userId/tokens', extractToken, getRoleFromToken, userController.deleteAllTokens); // Dodajanje rute za brisanje vseh tokenov

module.exports = router;