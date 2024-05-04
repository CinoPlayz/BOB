var express = require('express');
var path = require('path');
var logger = require('morgan');
var mongoose = require('mongoose');
var cors = require('cors')

//Reading mongoDB URI from args or env variable
var args = process.argv.slice(2);
var mongoDBURI = ""

if (!args || !args.length) {
    if(process.env.DBURL === undefined){
        console.log("No database URI passed! Usage: node <filename> <URI>");
        process.exit(1);
    }
    else {
        mongoDBURI = process.env.DBURL;
        console.log("Read URI from env variable!");
    }        
}
else {
    mongoDBURI = args[0];
    console.log("Read URI from args!")
}


mongoose.connect(mongoDBURI)
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
var trainLocHistoryRouter = require('./routes/trainLocHistoryRoutes');

app.use('/', indexRouter);
app.use('/tests', testRouter);
app.use('/trainLocHistories', trainLocHistoryRouter);

module.exports = app;


