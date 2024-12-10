const mongoose = require('mongoose');

const stationSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true
    },
    officialStationNumber: { // Station number from sz.si
        type: String,
        required: true,
        unique: true
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
}, {
    timestamps: true
});

module.exports = mongoose.model('station', stationSchema);
