const DelayModel = require('../models/delayModel.js');
const shared = require('./shared.js');

module.exports = {

    // Get all delays (GET)
    list: async function (req, res) {
        try {
            const delays = await DelayModel.find();
            return res.json(delays);
        } catch (err) {
            console.log("Error. delayController, list(), 1");
            return shared.handleError(err, 500, "Error when getting all delays.", res);
        }
    },

    // Get one delay by ID (GET)
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const delay = await DelayModel.findById(id);

            if (!delay) {
                console.log("Error. delayController, show(), 1");
                return shared.handleError(err, 404, "Delay not found", res);
            }

            return res.json(delay);
        } catch (err) {
            console.log("Error. delayController, show(), 2");
            return shared.handleError(err, 500, "Error when getting delay.", res);
        }
    },

    // Create new delay (POST)
    create: async function (req, res) {
        const newDelay = new DelayModel(req.body);

        try {
            const savedDelay = await newDelay.save();
            return res.status(201).json(savedDelay);
        } catch (err) {
            console.log("Error. delayController, create(), 1");
            return shared.handleError(err, 500, "Error when creating delay", res);
        }
    },

    // Update delay by ID (PUT)
    update: async function (req, res) {
        try {
            const delay = await DelayModel.findByIdAndUpdate(
                req.params.id,
                req.body,
                { new: true } // Return modified
            );

            if (!delay) {
                console.log("Error. delayController, update(), 1");
                return res.status(404).json({ message: "Delay not found" });
            }

            return res.json(delay);
        } catch (err) {
            console.log("Error. delayController, update(), 2");
            return shared.handleError(err, 500, "Error when updating delay", res);
        }
    },

    // Delete delay by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const delay = await DelayModel.findByIdAndDelete(id);

            if (!delay) {
                console.log("Error. delayController, remove(), 1");
                return res.status(404).json({ message: "Delay not found" });
            }

            return res.status(204).json();
        } catch (err) {
            console.log("Error. delayController, remove(), 2");
            return shared.handleError(err, 500, "Error when deleting delay", res);
        }
    }
};
