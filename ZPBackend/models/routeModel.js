const mongoose = require('mongoose');
const StationModel = require('./stationModel')

const routeSchema = new mongoose.Schema({
    trainType: {
        type: String,
        required: true,
    },
    trainNumber: {
        type: Number,
        required: true,
        unique: true // unikatni identifikator proge
    },
    validFrom: {
        type: Date,
        required: true
    },
    validUntil: {
        type: Date,
        required: true
    },
    canSupportBikes: {
        type: Boolean,
        required: true
    },
    drivesOn: [{
        type: Number,
        required: true,
        min: 0, // 0 - Sunday, 1 - Monday ... 6 - Saturday, 7 - Holidays
        max: 7
    }],
    start: {
        station: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'station',
            required: true
        },
        time: {
            type: String, // time format: 08:45
            required: true
        }
    },
    end: {
        station: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'station',
            required: true
        },
        time: {
            type: String, // time format: 08:45
            required: true
        }
    },
    middle: [{
        station: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'station',
            required: true
        },
        time: {
            type: String, // time format: 08:45
            required: true
        }
    }]
}, {
    timestamps: true
});

module.exports = mongoose.model('route', routeSchema);
