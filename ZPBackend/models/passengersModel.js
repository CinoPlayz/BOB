const mongoose = require('mongoose');

const passangersSchema = new mongoose.Schema({
    timeOfRequest: {
        type: Date,
        required: true
    },
    route: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'route',
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

module.exports = mongoose.model('passangers', passangersSchema);
