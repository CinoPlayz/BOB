const mongoose = require('mongoose');

const trainLocHistorySchema = new mongoose.Schema({
    timeOfRequest: {
        type: Date,
        required: true
    },
    trainType: {
        type: String,
        required: true,
        //enum: ['x', 'y']    omejitve vrst vlakov
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
        type: String,  // Format časa kot niz
        required: true
    },
    nextStation: {
        type: String,
        required: true
    },
    delay: {
        type: Number,
        required: true,
        min: 0  // Zagotavlja, da je zamuda ne negativno število
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
