const express = require('express');
const router = express.Router();
const trainLocHistoryController = require('../controllers/trainLocHistoryController.js');

/**
 * TODO: middleware (auth)
 */

// GET all
router.get('/', trainLocHistoryController.list);

// GET by ID
router.get('/:id', trainLocHistoryController.show);

// POST (create)
router.post('/', trainLocHistoryController.create);

// PUT (update)
router.put('/:id', trainLocHistoryController.update);

// DELETE (remove)
router.delete('/:id', trainLocHistoryController.remove);

module.exports = router;
