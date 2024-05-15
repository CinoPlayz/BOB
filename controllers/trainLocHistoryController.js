var TrainLocHistory = require('../models/trainLocHistoryModel.js');
var shared = require('./shared.js');


module.exports = {


    list: async function (req, res) {
        try {
            const trainHistory = await TrainLocHistory.find();
            return res.json(trainHistory);
        } catch (err) {
            return shared.handleError(err, 500, "Error when getting train location history", res);
        }
    },


    show: async function (req, res) {
        try {
            const train = await TrainLocHistory.findById(req.params.id);
            if (!train) {
                return res.status(404).json({ message: "Train not found" });
            }
            return res.json(train);
        } catch (err) {
            return shared.handleError(err, 500, "Error when getting train", res);
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
            return shared.handleError(err, 500, "Error when creating train", res);
        }
    },


    update: async function (req, res) {
        try {
            const train = await TrainLocHistory.findByIdAndUpdate(req.params.id, req.body, { new: true });
            if (!train) {
                return res.status(404).json({ message: "Train not found" });
            }
            return res.json(train);
        } catch (err) {
            return shared.handleError(err, 500, "Error when updating train", res);
        }
    },


    remove: async function (req, res) {
        try {
            const train = await TrainLocHistory.findByIdAndDelete(req.params.id);
            if (!train) {
                return res.status(404).json({ message: "Train not found" });
            }
            return res.status(204).json(); 
        } catch (err) {
            return shared.handleError(err, 500, "Error when deleting train", res);
        }
    },

};