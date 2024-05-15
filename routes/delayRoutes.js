const express = require('express');
const router = express.Router();
const delayController = require('../controllers/delayController.js');
const { extractToken } = require('../controllers/shared');
const { getRoleFromToken } = require('../controllers/shared');

// GET all
router.get('/', delayController.list);

// GET by ID
router.get('/:id', delayController.show);

// POST (create)
router.post('/', extractToken, getRoleFromToken, delayController.create);

// PUT (update)
router.put('/:id', extractToken, getRoleFromToken, delayController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, delayController.remove);

module.exports = router;
