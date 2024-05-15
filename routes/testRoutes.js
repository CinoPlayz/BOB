var express = require('express');
var router = express.Router();
var testController = require('../controllers/testController.js');
const { extractToken } = require('../controllers/shared');
const { getRoleFromToken } = require('../controllers/shared');

//Test

/*
 * GET
 */
router.get('/', testController.list);

/*
 * GET
 */
router.get('/:id', testController.show);

/*
 * POST
 */
router.post('/', extractToken, getRoleFromToken, testController.create);

/*
 * PUT
 */
router.put('/:id', extractToken, getRoleFromToken, testController.update);

/*
 * DELETE
 */
router.delete('/:id', extractToken, getRoleFromToken, testController.remove);

module.exports = router;
