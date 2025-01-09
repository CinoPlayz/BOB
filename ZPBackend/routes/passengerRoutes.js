const express = require('express');
const router = express.Router();
const passengersController = require('../controllers/passengersController.js');
const { extractToken, getRoleFromToken, getRoleAndUserFromToken, isReqRole } = require('./shared.js');
const multer  = require('multer');
const upload = multer({ dest: 'uploads/' });

// GET all
router.get('/', passengersController.list);

// GET all
//router.get('/stats', delayController.listJoined);

// GET seats by train type and wagon
router.get('/seats/:type/:num', passengersController.getSeats);

router.get('/seats/:type', passengersController.getNumberOfWAgons);

// GET by ID
//router.get('/:id', delayController.show);

// POST count number of people from image
router.post('/countPassengers', extractToken, getRoleFromToken, upload.single('image'), passengersController.countPassengers);

// POST (create)
router.post('/', extractToken, getRoleAndUserFromToken, passengersController.create);

// DELETE (remove)
router.delete('/:id', extractToken, getRoleAndUserFromToken, passengersController.remove);

module.exports = router;
