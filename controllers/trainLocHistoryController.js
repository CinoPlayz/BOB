const TrainLocHistoryModel = require('../models/trainLocHistoryModel.js');
const shared = require('./shared.js');

module.exports = {

    // Get all (GET)
    list: async function (req, res) {
        try {
            const trainLocHistories = await TrainLocHistoryModel.find();
            return res.json(trainLocHistories);
        } catch (err) {
            console.log("Error. trainLocHistoryController, list(), 1");
            return shared.handleError(err, 500, "Error when getting all train location histories.", res);
        }
    },

    // Get one by ID (GET)
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const trainLocHistory = await TrainLocHistoryModel.findById(id);

            if (!trainLocHistory) {
                console.log("Error. trainLocHistoryController, show(), 1");
                return shared.handleError(err, 404, "Train location history not found", res);
            }

            return res.json(trainLocHistory);
        } catch (err) {
            console.log("Error. trainLocHistoryController, show(), 2");
            return shared.handleError(err, 500, "Error when getting train location history.", res);
        }
    },

    // Create new train location history (POST)
    create: async function (req, res) {
        const newTrainLocHistory = new TrainLocHistoryModel({
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
            const savedTrainLocHistory = await newTrainLocHistory.save();
            return res.status(201).json(savedTrainLocHistory);
        } catch (err) {
            console.log("Error. trainLocHistoryController, create(), 1");
            return shared.handleError(err, 500, "Error when creating train location history", res);
        }
    },

    // Update train location history by ID (PUT)
    update: async function (req, res) {
        try {
            const trainLocHistory = await TrainLocHistoryModel.findByIdAndUpdate(
                req.params.id,
                req.body,
                { new: true } // Return modified
            );

            if (!trainLocHistory) {
                console.log("Error. trainLocHistoryController, update(), 1");
                return res.status(404).json({ message: "Train location history not found" });
            }

            return res.json(trainLocHistory);
        } catch (err) {
            console.log("Error. trainLocHistoryController, update(), 2");
            return shared.handleError(err, 500, "Error when updating train location history", res);
        }
    },

    // Delete train location history by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const trainLocHistory = await TrainLocHistoryModel.findByIdAndDelete(id);

            if (!trainLocHistory) {
                console.log("Error. trainLocHistoryController, remove(), 1");
                return res.status(404).json({ message: "Train location history not found" });
            }

            return res.status(204).json();
        } catch (err) {
            console.log("Error. trainLocHistoryController, remove(), 2");
            return shared.handleError(err, 500, "Error when deleting train location history", res);
        }
    }
};
