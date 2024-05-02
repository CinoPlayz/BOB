var express = require('express');
var path = require('path');
var logger = require('morgan');
var mongoose = require('mongoose');
var cors = require('cors')

var mongoDBURL = 'mongodb://127.0.0.1/Test'
mongoose.connect(mongoDBURL)
mongoose.Promise = global.Promise
var db = mongoose.connection
db.on('error', console.error.bind(console, 'MongoDB connection error: '));

var corsOptions = {
    origin: 'http://localhost:5173', //Vite port for frontend
    credentials: true,
    optionsSuccessStatus: 200 // some legacy browsers (IE11, various SmartTVs) choke on 204
}

var app = express();

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(express.static(path.join(__dirname, 'public')));
app.use(cors(corsOptions));

var indexRouter = require('./routes/index');
var testRouter = require('./routes/testRoutes');

app.use('/', indexRouter);
app.use('/tests', testRouter);

module.exports = app;
