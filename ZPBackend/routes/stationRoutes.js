const express = require('express');
const router = express.Router();
const stationController = require('../controllers/stationController.js');
const { extractToken, getRoleFromToken, isReqRole } = require('./shared');

// GET all
router.get('/', stationController.list);

// GET by ID
router.get('/:id', stationController.show);

// POST (create)
router.post('/', extractToken, getRoleFromToken, isReqRole("admin"), stationController.create);

// PUT (update)
router.put('/:id', extractToken, getRoleFromToken, isReqRole("admin"), stationController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("admin"), stationController.remove);

module.exports = router;