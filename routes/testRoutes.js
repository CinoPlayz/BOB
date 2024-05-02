var express = require('express');
var router = express.Router();
var testController = require('../controllers/testController.js');

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
router.post('/', testController.create);

/*
 * PUT
 */
router.put('/:id', testController.update);

/*
 * DELETE
 */
router.delete('/:id', testController.remove);

module.exports = router;
