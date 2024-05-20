const StationModel = require('../models/stationModel.js');
const shared = require('./shared.js');

module.exports = {

    // Get all (GET)
    list: async function (req, res) {
        try {
            const stations = await StationModel.find();

            return res.json(stations);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting all stations", err);
        }
    },

    // Get one by ID (GET)
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const station = await StationModel.findById(id);

            if (!station) {
                return shared.handleError(res, 404, "Station not found", null);
            }

            return res.json(station);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting station", err);
        }
    },

    // Create new station (POST)
    create: async function (req, res) {
        const newStation = new StationModel({
            name: req.body.name,
            officialStationNumber: req.body.officialStationNumber,
            coordinates: req.body.coordinates
        });

        try {
            const savedStation = await newStation.save();
            return res.status(201).json(savedStation);
        } catch (err) {
            return shared.handleError(res, 500, "Error when creating station", err);
        }
    },

    // Update by ID (PUT)
    update: async function (req, res) {
        try {
            const station = await StationModel.findByIdAndUpdate(
                req.params.id,
                req.body,
                { new: true } // Return modified
            );

            if (!station) {
                return shared.handleError(res, 404, "Station not found", null);
            }

            return res.json(station);
        } catch (err) {
            return shared.handleError(res, 500, "Error when updating station", err);
        }
    },

    // Delete by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const station = await StationModel.findByIdAndDelete(id);

            if (!station) {
                return shared.handleError(res, 404, "Station not found", null);
            }

            return res.status(204).json();
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting station", err);
        }
    }
};