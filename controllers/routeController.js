const RouteModel = require('../models/routeModel.js');
const shared = require('./shared.js');

module.exports = {

    // Get all (GET)
    list: async function (req, res) {
        try {
            const routes = await RouteModel.find();
            return res.json(routes);
        } catch (err) {
            console.log("Error. routeController, list(), 1");
            return shared.handleError(err, 500, "Error when getting all routes.", res);
        }
    },

    // Get one by ID (GET)
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const route = await RouteModel.findById(id);

            if (!route) {
                console.log("Error. routeController, show(), 1");
                return shared.handleError(err, 404, "Route not found.", res);
            }

            return res.json(route);
        } catch (err) {
            console.log("Error. routeController, show(), 2");
            return shared.handleError(err, 500, "Error when getting route.", res);
        }
    },

    // Create new route (POST)
    create: async function (req, res) {
        const newRoute = new RouteModel({
            trainType: req.body.trainType,
            trainNumber: req.body.trainNumber,
            vaildFrom: req.body.vaildFrom,
            validUntil: req.body.validUntil,
            canSupportBikes: req.body.canSupportBikes,
            drivesOn: req.body.drivesOn,
            start: req.body.start,
            end: req.body.end,
            middle: req.body.middle
        });

        try {
            const savedRoute = await newRoute.save();
            return res.status(201).json(savedRoute);
        } catch (err) {
            console.log("Error. routeController, create(), 1");
            return shared.handleError(err, 500, "Error when creating route", res);
        }
    },

    // Update route by ID (PUT)
    update: async function (req, res) {
        try {
            const route = await RouteModel.findByIdAndUpdate(
                req.params.id,
                req.body,
                { new: true } // Return modified
            );

            if (!route) {
                console.log("Error. routeController, update(), 1");
                return res.status(404).json({ message: "Route not found" });
            }

            return res.json(route);
        } catch (err) {
            console.log("Error. routeController, update(), 2");
            return shared.handleError(err, 500, "Error when updating route", res);
        }
    },

    // Delete route by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const route = await RouteModel.findByIdAndDelete(id);

            if (!route) {
                console.log("Error. routeController, remove(), 1");
                return res.status(404).json({ message: "Route not found" });
            }

            return res.status(204).json();
        } catch (err) {
            console.log("Error. routeController, remove(), 2");
            return shared.handleError(err, 500, "Error when deleting route", res);
        }
    }
};
