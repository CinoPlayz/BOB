const express = require('express');
const router = express.Router();
const delayController = require('../controllers/delayController.js');

/**
 * TODO: middleware (auth)
 */

// GET all
router.get('/', delayController.list);

// GET by ID
router.get('/:id', delayController.show);

// POST (create)
router.post('/', delayController.create);

// PUT (update)
router.put('/:id', delayController.update);

// DELETE (remove)
router.delete('/:id', delayController.remove);

module.exports = router;
