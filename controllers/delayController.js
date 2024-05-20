const DelayModel = require('../models/delayModel.js');
const shared = require('./shared.js');

module.exports = {

    // Get all delays (GET)
    list: async function (req, res) {
        try {
            const delays = await DelayModel.find();
            return res.json(delays);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting all delays", err);
        }
    },

    // Get one delay by ID (GET)
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const delay = await DelayModel.findById(id);

            if (!delay) {
                return shared.handleError(res, 404, "Delay not found", null);
            }

            return res.json(delay);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting delay", err);
        }
    },

    // Create new delay (POST)
    create: async function (req, res) {
        const newDelay = new DelayModel(req.body);

        try {
            const savedDelay = await newDelay.save();
            return res.status(201).json(savedDelay);
        } catch (err) {
            return shared.handleError(res, 500, "Error when creating delay", err);
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
                return shared.handleError(res, 404, "Delay not found", null);
            }

            return res.json(delay);
        } catch (err) {
            return shared.handleError(res, 500, "Error when updating delay", err);
        }
    },

    // Delete delay by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const delay = await DelayModel.findByIdAndDelete(id);

            if (!delay) {
                return shared.handleError(res, 404, "Delay not found", null);
            }

            return res.status(204).json();
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting delay", err);
        }
    }
};
