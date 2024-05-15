var express = require('express');
var router = express.Router();
var trainLocHistoryController = require('../controllers/trainLocHistoryController.js');


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
router.post('/', trainLocHistoryController.create);

/*
 * PUT
 */
router.put('/:id', trainLocHistoryController.update);

/*
 * DELETE
 */
router.delete('/:id', trainLocHistoryController.remove);

module.exports = router;