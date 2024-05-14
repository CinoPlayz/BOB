const express = require('express');
const router = express.Router();
const stationController = require('../controllers/stationController.js');

/**
 * TODO: middleware (auth)
 */

// GET all
router.get('/', stationController.list);

// GET by ID
router.get('/:id', stationController.show);

// POST (create)
router.post('/', stationController.create);

// PUT (update)
router.put('/:id', stationController.update);

// DELETE (remove)
router.delete('/:id', stationController.remove);

module.exports = router;