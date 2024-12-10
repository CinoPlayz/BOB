var express = require('express');
var path = require('path');
var logger = require('morgan');
var mongoose = require('mongoose');
var cors = require('cors');
const cron = require('node-cron');
const { exec } = require('child_process');

//Reading mongoDB URI from args or env variable
var args = process.argv.slice(2);
var mongoDBURI = ""

if (!args || !args.length) {
    if(process.env.DBURI === undefined){
        console.log("No database URI passed! Usage: node <filename> <URI>");
        process.exit(1);
    }
    else {
        mongoDBURI = process.env.DBURI;
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


// Cron job za posodobitev piškotkov 
cron.schedule('*/5 * * * *', () => {
    //console.log('Začenjam posodobitev piškotkov...');
    exec('node updateCookies.js', (error, stdout, stderr) => {
        if (error) {
            console.error(`Error: ${error.message}`);
            return;
        }
        if (stderr) {
            console.error(`stderr: ${stderr}`);
            return;
        }
        console.log(`stdout: ${stdout}`);
    });
});


var indexRouter = require('./routes/index');
var testRouter = require('./routes/testRoutes');
const trainLocHistoryRouter = require('./routes/trainLocHistoryRoutes');
var userRouter = require('./routes/userRoutes');
const stationRouter = require('./routes/stationRoutes');
const delayRouter = require('./routes/delayRoutes');
const routeRouter = require('./routes/routeRoutes');

app.use('/', indexRouter);
app.use('/tests', testRouter);
app.use('/trainLocHistories', trainLocHistoryRouter);
app.use('/users', userRouter);
app.use('/stations', stationRouter);
app.use('/delays', delayRouter);
app.use('/routes', routeRouter);

module.exports = app;
