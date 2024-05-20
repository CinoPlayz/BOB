const RouteModel = require('../models/routeModel.js');
const shared = require('./shared.js');

module.exports = {

    // Get all (GET)
    list: async function (req, res) {
        try {
            const routes = await RouteModel.find();
            return res.json(routes);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting all routes", err);
        }
    },

    // Get one by ID (GET)
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const route = await RouteModel.findById(id);

            if (!route) {
                return shared.handleError(res, 404, "Route not found", null);
            }

            return res.json(route);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting route", err);
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
            return shared.handleError(res, 500, "Error when creating route", err);
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
                return shared.handleError(res, 404, "Route not found", null);
            }

            return res.json(route);
        } catch (err) {
            return shared.handleError(res, 500, "Error when updating route", err);
        }
    },

    // Delete route by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const route = await RouteModel.findByIdAndDelete(id);

            if (!route) {
                return shared.handleError(res, 404, "Route not found", null);
            }

            return res.status(204).json();
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting route", err);
        }
    }
};
