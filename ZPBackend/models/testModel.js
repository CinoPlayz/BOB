var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var testSchema = new Schema({
	'name': String,
});

mongoose.model('test', testSchema);
module.exports = mongoose.model('test', testSchema);
