var express = require('express');
var router = express.Router();
var testController = require('../controllers/testController.js');
const { extractToken, getRoleFromToken, isReqRole } = require('./shared');

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
router.post('/', extractToken, getRoleFromToken, isReqRole("user"), testController.create);

/*
 * PUT
 */
router.put('/:id', extractToken, getRoleFromToken, isReqRole("user"), testController.update);

/*
 * DELETE
 */
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("user"), testController.remove);

module.exports = router;
