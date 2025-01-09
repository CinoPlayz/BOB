const PassengersModel = require('../models/PassengersModel.js');
const SeatsModel = require('../models/seatsModel.js');
const shared = require('./shared.js');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const fs = require('fs');

module.exports = {
    // Get seats by train type and wagon (GET)
    getSeats: async function (req, res) {
        const type = req.params.type;
        const num = req.params.num;       

        try {
            const seats = await SeatsModel.find({type: type, wagonNumber: num});
            return res.json(seats);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting seats", err);
        }
    },

    getNumberOfWAgons: async function (req, res) {
        const type = req.params.type;

        try {
            const count = await SeatsModel.countDocuments({ type: type });
            console.log("count:", count);
            res.status(200).json({ numberOfWagons: count });
        } catch (err) {
            res.status(500).json({ error: "Internal server error" });
        }
    },

    // count the number of people in the picture (POST)
    countPassengers: async function (req, res) {    
        if (!req.file){
            return shared.handleError(res, 400, "Missing required fields", null);
        } 

        try {
            const { stdout, stderr } = await exec('conda run -n PRO --live-stream python .\\..\\ZPOccupancyDetection\\image_processing.py count uploads\\'+req.file.filename);
            
            const numOfPeople = {"numOfPeople": parseInt(stdout)};
            fs.unlink('./uploads/'+req.file.filename, (err) => {if (err) throw err});            

            return res.status(200).json(numOfPeople);
        } catch (err) {
            return shared.handleError(res, 500, "Error when counting passengers", err);
        }
    },

    // Get all passengers (GET)
    list: async function (req, res) {
        try {
            const passengers = await PassengersModel.find();
            return res.json(passengers);
        } catch (err) {
            return shared.handleError(res, 500, "Error when getting all passengers records", err);
        }
    },    

    // Create new passengers (POST)
    create: async function (req, res) {
        const user = req.user;
        if (!user) {
            return shared.handleError(res, 404, "User not found", null);
        }

        const timeOfRequest = req.body.timeOfRequest;
        const coordinatesOfRequest = req.body.coordinatesOfRequest;
        const guessedOccupancyRate = req.body.guessedOccupancyRate;
        const realOccupancyRate = req.body.realOccupancyRate;
        const route = req.body.route;

        if(!timeOfRequest || !coordinatesOfRequest || !guessedOccupancyRate || !route) {
            return shared.handleError(res, 400, "Missing required fields", null);
        }

        const newPassengers = new PassengersModel({timeOfRequest: timeOfRequest, coordinatesOfRequest: coordinatesOfRequest,
            guessedOccupancyRate: guessedOccupancyRate, realOccupancyRate: realOccupancyRate, route: route, postedByUser: user._id});

        try {
            const savedPassenger = await newPassengers.save();
            return res.status(201).json(savedPassenger);
        } catch (err) {
            return shared.handleError(res, 500, "Error when creating passengers record", err);
        }
    },

    // Delete passengers by ID (DELETE)
    remove: async function (req, res) {
        const id = req.params.id;
        const user = req.user;

        try {
            const passengers = await PassengersModel.findOneAndDelete({_id: id, postedByUser: user._id});

            if (!passengers) {
                return shared.handleError(res, 404, "Passengers record not found or not yours", null);
            }

            return res.status(204).json();
        } catch (err) {
            return shared.handleError(res, 500, "Error when deleting passengers record", err);
        }
    }
};
