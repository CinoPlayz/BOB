const express = require('express');
const router = express.Router();
const delayController = require('../controllers/delayController.js');
const { extractToken, getRoleFromToken, isReqRole } = require('./shared');

// GET all
router.get('/', delayController.list);

// GET all
router.get('/stats', delayController.listJoined);

// GET by ID
router.get('/:id', delayController.show);

// POST (create)
router.post('/', extractToken, getRoleFromToken, isReqRole("user"), delayController.create);

// PUT (update)
router.put('/:id', extractToken, getRoleFromToken, isReqRole("user"), delayController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("user"), delayController.remove);

module.exports = router;
