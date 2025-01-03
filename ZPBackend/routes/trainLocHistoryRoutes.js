var express = require('express');
var router = express.Router();
var trainLocHistoryController = require('../controllers/trainLocHistoryController.js');
const { extractToken, getRoleFromToken, isReqRole } = require('./shared');

/*
 * GET
 */
router.get('/', trainLocHistoryController.list);
router.get('/activeTrains', trainLocHistoryController.getActiveTrains);
router.get('/trainLocByDate', trainLocHistoryController.getTrainHistoryByDateRange);
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