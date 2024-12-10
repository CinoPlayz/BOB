var TrainLocHistory = require('../models/trainLocHistoryModel.js');
var shared = require('./shared.js');
const axios = require('axios');
const fs = require('fs');
const path = require('path');

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
        // console.log(req.body)
        
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


    getActiveTrains: async function (req, res) {
        try {
            const cookiesPath = path.join(__dirname, '..', 'cookies.json');
            const cookiesData = fs.readFileSync(cookiesPath, 'utf-8');
            const cookies = JSON.parse(cookiesData);
            console.log('Using cookies:', cookiesData);
            const cookieHeader = cookies.map(cookie => `${cookie.name}=${cookie.value}`).join('; ');
            const response = await axios.post('https://potniski.sz.si/wp-admin/admin-ajax.php', new URLSearchParams({
                action: 'aktivni_vlaki'
            }), {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'User-Agent': 'PostmanRuntime/7.39.0',
                    'Accept': '*/*',
                    'Accept-Encoding': 'gzip, deflate, br',
                    'Connection': 'keep-alive',
                    'Cookie': cookieHeader
                }
            });
    
            const data = response.data;
            res.status(200).json(data);
        } catch (err) {
            return shared.handleError(res, 500, "Error fetching active trains", err);
        }
    },


     getTrainHistoryByDateRange: async function(req, res) {
        const { startDate, endDate } = req.query;
    
        try {
            const trainData = await TrainLocHistory.find({
                timeOfRequest: {
                    $gte: new Date(startDate),
                    $lte: new Date(endDate)
                }
            });
            res.status(200).json(trainData);
        } catch (err) {
            return shared.handleError(res, 500, "Error fetching train history data", err);
        }
    },
};