const PassengersModel = require('../models/PassengersModel.js');
const SeatsModel = require('../models/seatsModel.js');
const shared = require('./shared.js');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const fs = require('fs');

module.exports = {

    // Get all passangers (GET)
    list: async function (req, res) {
        try {
            const delays = await PassengersModel.find();
            return res.json(delays);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting all delays", err);
        }
    },

    // Get seats by train type and wagon (GET)
    getSeats: async function (req, res) {
        const type = req.params.type;
        const num = req.params.num;
        
        const newDelay = new SeatsModel({type: "ICS", wagonNumber: 1, countOfSeats: 41});
       

        try {
            const savedDelay = await newDelay.save();
            return res.status(201).json(savedDelay);

            /*const num = await PassengersModel.find();
            return res.json(delays);*/
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting all delays", err);
        }
    },

    // Create new delay (POST)
    countPassengers: async function (req, res) {       
        try {
            const { stdout, stderr } = await exec('conda run -n PRO --live-stream python .\\..\\ZPOccupancyDetection\\image_processing.py count uploads\\'+req.file.filename);
            
            const numOfPeople = {"numOfPeople": parseInt(stdout)};
            fs.unlink('./uploads/'+req.file.filename, (err) => {if (err) throw err});            

            return res.status(200).json(numOfPeople);
        } catch (err) {
            return shared.handleError(res, 500, "Error when counting passangers", err);
        }
    },

    // Create new delay (POST)
    create: async function (req, res) {
        const newDelay = new PassengersModel(req.body);

        try {
            const savedDelay = await newDelay.save();
            return res.status(201).json(savedDelay);
        } catch (err) {
            return shared.handleError(res, 500, "Error when creating delay", err);
        }
    },

    // Delete delay by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            const delay = await PassengersModel.findByIdAndDelete(id);

            if (!delay) {
                return shared.handleError(res, 404, "Delay not found", null);
            }

            return res.status(204).json();
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting delay", err);
        }
    }
};
