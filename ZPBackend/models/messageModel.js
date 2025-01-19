const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
    timeOfMessage: {
        type: Date,
        required: true
    },
    postedByUser: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    message: {
        type: String,
        required: true
    },
    category: {
        type: String,
        enum: ['miscellaneous', 'extreme'],
        required: true
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('message', messageSchema);
