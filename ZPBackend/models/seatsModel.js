const mongoose = require('mongoose');

const seatsSchema = new mongoose.Schema({
    type: {
        type: String,
        required: true
    },
    wagonNumber: {
        type: Number,
        required: true
    },
    countOfSeats: {
        type: Number,
        required: true
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('seats', seatsSchema);
