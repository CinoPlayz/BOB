const express = require('express');
const router = express.Router();
const routeController = require('../controllers/routeController.js');

/**
 * TODO: middleware (auth)
 */

// GET all
router.get('/', routeController.list);

// GET by ID
router.get('/:id', routeController.show);

// POST (create)
router.post('/', routeController.create);

// PUT (update)
router.put('/:id', routeController.update);

// DELETE (remove)
router.delete('/:id', routeController.remove);

module.exports = router;
