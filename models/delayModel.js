const mongoose = require('mongoose');

const delaySchema = new mongoose.Schema({
    timeOfRequest: {
        type: Date,
        required: true
    },
    route: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'trainLocHistory',
        required: true
    },
    currentStation: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'station',
        required: true
    },
    delay: {
        type: Number, // in minutes
        required: true
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('delay', delaySchema);
