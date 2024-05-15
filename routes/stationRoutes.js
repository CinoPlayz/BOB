const express = require('express');
const router = express.Router();
const stationController = require('../controllers/stationController.js');
const { extractToken } = require('../controllers/shared');
const { getRoleFromToken } = require('../controllers/shared');

// GET all
router.get('/', stationController.list);

// GET by ID
router.get('/:id', stationController.show);

// POST (create)
router.post('/', extractToken, getRoleFromToken, stationController.create);

// PUT (update)
router.put('/:id', extractToken, getRoleFromToken, stationController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, stationController.remove);

module.exports = router;