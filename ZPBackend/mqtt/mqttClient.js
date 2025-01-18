require('dotenv').config();
const mqtt = require('mqtt');

const brokerUrl = process.env.MQTT_BROKER_URL || 'mqtt://localhost:1883';

const client = mqtt.connect(brokerUrl);

client.on('connect', () => {
    console.log('Connected to MQTT Broker');
    // Subscribe to the login request topic (ensure this matches the client-side publish topic)
    client.subscribe('app/login/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/login/request');
        } else {
            console.error('Error subscribing to app/login/request:', err);
        }
    });

    client.subscribe('app/twofa/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/twofa/request');
        } else {
            console.error('Error subscribing to app/twofa/request:', err);
        }
    });

    client.subscribe('app/logout/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/logout/request');
        } else {
            console.error('Error subscribing to app/logout/request:', err);
        }
    });

    client.subscribe('app/register/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/register/request');
        } else {
            console.error('Error subscribing to app/register/request:', err);
        }
    });

    client.subscribe('app/passengers/count/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/passengers/count/request');
        } else {
            console.error('Error subscribing to app/passengers/count/request:', err);
        }
    });

    client.subscribe('app/passengers/create/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/passengers/create/request');
        } else {
            console.error('Error subscribing to app/passengers/create/request:', err);
        }
    });

    client.subscribe('app/trains/trainHistoryByDateRange/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/trains/trainHistoryByDateRange/request');
        } else {
            console.error('Error subscribing to app/trains/trainHistoryByDateRange/request:', err);
        }
    });

    client.subscribe('app/messages/create/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/messages/create/request');
        } else {
            console.error('Error subscribing to app/messages/create/request:', err);
        }
    })

    client.subscribe('app/messages/retrieve/all/request', (err) => {
        if (!err) {
            console.log('Subscribed to app/messages/retrieve/all/request');
        } else {
            console.error('Error subscribing to app/messages/retrieve/all/request:', err);
        }
    })
});

client.on('error', (err) => {
    console.error('MQTT Connection Error:', err);
});

module.exports = client;