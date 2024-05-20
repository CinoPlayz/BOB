var TestModel = require('../models/testModel.js');
var shared = require('./shared.js');

/**
 * testController.js
 *
 * @description :: Server-side logic for managing tests.
 */


module.exports = {

    /**
     * testController.list()
     */
    list: async function (req, res) {
        try {
            const tests = await TestModel.find();

            return res.json(tests);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when getting tests", err);
        }

    },

    /**
     * testController.show()
     */
    //TODO: Fix shared.handleError
    show: async function (req, res) {
        var id = req.params.id;

        try {
            const test = await TestModel.findOne({ _id: id });

            if (!test) {
                return shared.handleError(res, 404, "No such test", null);
            }

            return res.json(test);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when getting test", err);
        }
    },

    /**
     * testController.create()
     */
    create: async function (req, res) {

        var testToCreate = new TestModel({
            name: req.body.name
        });

        try {
            const test = await testToCreate.save();

            return res.status(201).json(test);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when creating test", err);
        }
    },

    /**
     * testController.update()
     */
    update: async function (req, res) {
        var id = req.params.id;

        try {
            const testFound = await TestModel.findOne({ _id: id });

            if (!testFound) {
                return shared.handleError(res, 404, "No such test", null);
            }

            testFound.name = req.body.name ? req.body.name : testFound.name;

            const testUpdated = await testFound.save();

            return res.json(testUpdated);
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when updating test", err);
        }
    },

    /**
     * testController.remove()
     */
    remove: async function (req, res) {
        var id = req.params.id;

        try {
            await TestModel.findByIdAndDelete(id);

            return res.status(204).json();
        }
        catch (err) {
            return shared.handleError(res, 500, "Error when deleting the test", err);
        }
    },
};