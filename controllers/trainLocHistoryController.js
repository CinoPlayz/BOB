var TrainLocHistory = require('../models/trainLocHistoryModel.js');
var shared = require('./shared.js');


module.exports = {


    list: async function (req, res) {
        try {
            const trainHistory = await TrainLocHistory.find();
            return res.json(trainHistory);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting train location history", err);
        }
    },


    show: async function (req, res) {
        try {
            const train = await TrainLocHistory.findById(req.params.id);
            if (!train) {
                return shared.handleError(res, 404, "Train not found", null);
            }
            return res.json(train);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting train location history", err);
        }
    },


    create: async function (req, res) {
        try {
            const newTrain = new TrainLocHistory({
                timeOfRequest: req.body.timeOfRequest,
                trainType: req.body.trainType,
                trainNumber: req.body.trainNumber,
                routeFrom: req.body.routeFrom,
                routeTo: req.body.routeTo,
                routeStartTime: req.body.routeStartTime,
                nextStation: req.body.nextStation,
                delay: req.body.delay,
                coordinates: req.body.coordinates
            });
            const savedTrain = await newTrain.save();
            return res.status(201).json(savedTrain);
        } catch (err) {
            return shared.handleError(res, 500, "Error when creating train location history", err);
        }
    },


    update: async function (req, res) {
        try {
            const train = await TrainLocHistory.findByIdAndUpdate(req.params.id, req.body, { new: true });
            if (!train) {
                return shared.handleError(res, 404, "Train not found", null);
            }
            return res.json(train);
        } catch (err) {
            return shared.handleError(res, 500, "Error when updating train location history", err);
        }
    },


    remove: async function (req, res) {
        try {
            const train = await TrainLocHistory.findByIdAndDelete(req.params.id);
            if (!train) {
                return shared.handleError(res, 404, "Train not found", null);
            }
            return res.status(204).json(); 
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting train location history", err);
        }
    },

};