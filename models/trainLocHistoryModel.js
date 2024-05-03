const mongoose = require('mongoose');

const trainLocHistorySchema = new mongoose.Schema({
    timeOfRequest: {
        type: Date,
        required: true
    },
    trainType: {
        type: String,
        required: true,
        //enum: ['x', 'y']    omejitve vrst vlakov po potrebi
    },
    trainNumber: {
        type: String,
        required: true
    },
    routeFrom: {
        type: String,
        required: true
    },
    routeTo: {
        type: String,
        required: true
    },
    routeStartTime: {
        type: String,  
        required: true
    },
    nextStation: {
        type: String,
        required: true
    },
    delay: {
        type: Number,
        required: true,
        min: 0  
    },
    coordinates: {
        lat: {
            type: Number,
            required: true
        },
        lng: {
            type: Number,
            required: true
        }
    }
});

const TrainLocHistory = mongoose.model('TrainLocHistory', trainLocHistorySchema);
module.exports = TrainLocHistory;
