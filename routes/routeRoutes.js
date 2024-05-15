const express = require('express');
const router = express.Router();
const routeController = require('../controllers/routeController.js');
const { extractToken } = require('../controllers/shared');
const { getRoleFromToken } = require('../controllers/shared');

// GET all
router.get('/', routeController.list);

// GET by ID
router.get('/:id', routeController.show);

// POST (create)
router.post('/', extractToken, getRoleFromToken, routeController.create);

// PUT (update)
router.put('/:id', extractToken, getRoleFromToken, routeController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, routeController.remove);

module.exports = router;
