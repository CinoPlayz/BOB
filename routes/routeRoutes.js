const express = require('express');
const router = express.Router();
const routeController = require('../controllers/routeController.js');
const { extractToken, getRoleFromToken, isReqRole } = require('./shared');

// GET all
router.get('/', routeController.list);

// GET by ID
router.get('/:id', routeController.show);

// POST (create)
router.post('/', extractToken, getRoleFromToken, isReqRole("admin"), routeController.create);

// PUT (update)
router.put('/:id', extractToken, getRoleFromToken, isReqRole("admin"), routeController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("admin"), routeController.remove);

module.exports = router;
