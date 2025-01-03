const express = require('express');
const router = express.Router();
const passengersController = require('../controllers/passengersController.js');
const { extractToken, getRoleFromToken, isReqRole } = require('./shared.js');
const multer  = require('multer');
const upload = multer({ dest: 'uploads/' });

// GET all
//router.get('/', delayController.list);

// GET all
//router.get('/stats', delayController.listJoined);

// GET seats by train type and wagon
router.get('/seats/:type/:num', passengersController.getSeats);

// GET by ID
//router.get('/:id', delayController.show);

// POST (create)
router.post('/countPassengers', extractToken, getRoleFromToken, isReqRole("user"), upload.single('image'), passengersController.countPassengers);

// DELETE (remove)
//router.delete('/:id', extractToken, getRoleFromToken, isReqRole("user"), delayController.remove);

module.exports = router;
