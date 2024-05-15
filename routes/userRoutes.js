var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');


/*
 * GET
 */
router.get('/', userController.list);
router.get('/:id', userController.show);


/*
 * POST
 */
router.post('/', userController.create);
router.post('/login', userController.login);
router.post('/:id/TwofaSetup', userController.TwofaSetup);
/*
 * PUT
 */
router.put('/:id', userController.update);

/*
 * DELETE
 */
router.delete('/:id', userController.remove);
router.delete('/:userId/tokens', userController.deleteAllTokens); // Dodajanje rute za brisanje vseh tokenov

module.exports = router;