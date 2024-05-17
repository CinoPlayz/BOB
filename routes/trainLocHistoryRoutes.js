var express = require('express');
var router = express.Router();
var trainLocHistoryController = require('../controllers/trainLocHistoryController.js');
const { extractToken, getRoleFromToken, isReqRole } = require('./shared');

/*
 * GET
 */
router.get('/', trainLocHistoryController.list);

/*
 * GET
 */
router.get('/:id', trainLocHistoryController.show);

/*
 * POST
 */
router.post('/', extractToken, getRoleFromToken, isReqRole("user"), trainLocHistoryController.create);

/*
 * PUT
 */
router.put('/:id', extractToken, getRoleFromToken, isReqRole("user"), trainLocHistoryController.update);

/*
 * DELETE
 */
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("user"), trainLocHistoryController.remove);

module.exports = router;