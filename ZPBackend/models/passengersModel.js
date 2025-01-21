const mongoose = require('mongoose');

const passengersSchema = new mongoose.Schema({
    timeOfRequest: {
        type: Date,
        required: true
    },
    coordinatesOfRequest: {
        lat: {
            type: Number,
            required: true
        },
        lng: {
            type: Number,
            required: true
        }
    },
    guessedOccupancyRate: {
        type: Number,
        required: true
    },
    realOccupancyRate: {
        type: Number,
        default: null
    },    
    route: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'route',
        required: true
    },
    postedByUser: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'user',
        required: true
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('passengers', passengersSchema);
