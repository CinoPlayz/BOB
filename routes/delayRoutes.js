const express = require('express');
const router = express.Router();
const delayController = require('../controllers/delayController.js');
const { extractToken } = require('../controllers/shared');
const { getRoleFromToken } = require('../controllers/shared');
const { isReqRole } = require('../controllers/shared');

// GET all
router.get('/', delayController.list);

// GET by ID
router.get('/:id', delayController.show);

// POST (create)
router.post('/', extractToken, getRoleFromToken, isReqRole("user"), delayController.create);

// PUT (update)
router.put('/:id', extractToken, getRoleFromToken, isReqRole("user"), delayController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("user"), delayController.remove);

module.exports = router;
