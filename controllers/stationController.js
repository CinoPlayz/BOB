const StationModel = require('../models/stationModel.js');
const shared = require('./shared.js');

module.exports = {

    // Get all (GET)
    list: async function (req, res) {
        try {
            const stations = await StationModel.find();

            return res.json(stations);
        } catch (err) {
            console.log("Error. stationController, list(), 1");
            return shared.handleError(err, 500, "Error when getting all stations.", res);
        }
    },

    // Get one by ID (GET)
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const station = await StationModel.findById(id);

            if (!station) {
                console.log("Error. stationController, show(), 1");
                return shared.handleError(err, 404, "Station not found", res);
            }

            return res.json(station);
        } catch (err) {
            console.log("Error. stationController, show(), 2");
            return shared.handleError(err, 500, "Error when getting station.", res);
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
            console.log("Error. stationController, create(), 1");
            return shared.handleError(err, 500, "Error when creating station", res);
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
                console.log("Error. stationController, update(), 1");
                return res.status(404).json({ message: "Station not found" });
            }

            return res.json(station);
        } catch (err) {
            console.log("Error. stationController, update(), 2");
            return shared.handleError(err, 500, "Error when updating station", res);
        }
    },

    // Delete by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const station = await StationModel.findByIdAndDelete(id);

            if (!station) {
                console.log("Error. stationController, remove(), 1");
                return res.status(404).json({ message: "Station not found" });
            }

            return res.status(204).json();
        } catch (err) {
            console.log("Error. stationController, remove(), 2");
            return shared.handleError(err, 500, "Error when deleting station", res);
        }
    }
};