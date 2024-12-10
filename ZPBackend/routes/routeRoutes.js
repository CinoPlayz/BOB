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
router.post('/createWithIDs', extractToken, getRoleFromToken, isReqRole("admin"), routeController.createWithIDs);

// PUT (update)
router.put('/:id/updateFromApp', extractToken, getRoleFromToken, isReqRole("admin"), routeController.updateFromApp);
router.put('/:id', extractToken, getRoleFromToken, isReqRole("admin"), routeController.update);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleFromToken, isReqRole("admin"), routeController.remove);

module.exports = router;
