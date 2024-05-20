const mongoose = require('mongoose');
var bcrypt = require('bcrypt');
const Schema = mongoose.Schema;

const tokenSchema = new Schema({
    token: {
        type: String,
        required: true
    },
    expiresOn: {
        type: Date,
        required: true
    },
    type: {
        type: String,
        enum: ['all', 'login'],
        required: true
    }
});

const userSchema = new Schema({
    username: {
        type: String,
        required: true
        //minlength: 3,
        //maxlength: 30
    },
    email: {
        type: String,
        required: true,
        unique: true,       //mora biti edinstven
        match: [/^\S+@\S+\.\S+$/, 'Email is invalid.']//Ustreza nizu, ki začne z enim ali več znaki, 
    },                                  //ki niso presledki, sledi '@', sledi en ali več znakov, ki niso presledki, sledi pika, in nato ponovno en ali več znakov, ki niso presledki.
    password: {
        type: String,
        required: true
        //minlength: 8   ->za boljšo varnost?
    },
    tokens: [tokenSchema],
    '2faEnabled': {
        type: Boolean,
        default: false
    },
    '2faSecret': {
        type: String,
        required: function() { return this['2faEnabled']; } // prisoten le, če je 2faEnabled enak true.
    },
    role: {
        type: String,
        enum: ['admin', 'user'],
        default: 'user'
    }
}, {
    timestamps: true
});


// Pre-save hook za šifriranje gesla
userSchema.pre('save', async function(next) {
    const user = this;

    if (!user.isModified('password')) { //preprečuje ponovno šifriranje gesla, 
        return next();                  //ko se uporabnik posodobi, ampak geslo ostane nespremenjeno.
    }   

    try {
        const salt = await bcrypt.genSalt(10);//Sol se uporablja za zaščito gesel pred napadi s predpripravljenimi tabelami
        const hash = await bcrypt.hash(user.password, salt);
        user.password = hash;
        next();
    } catch (err) {
        next(err);
    }
});

userSchema.methods.comparePassword = async function(candidatePassword) {
    return bcrypt.compare(candidatePassword, this.password);
};

userSchema.statics.authenticate = async function(username, password) {
    const user = await this.findOne({ username }).exec();
    if (!user) {
        throw new Error("User not found.");
    }
    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
        throw new Error("Wrong username or password.");
    }
    return user;
};



const User = mongoose.model('User', userSchema);

module.exports = User;
