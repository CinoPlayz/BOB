module.exports = {
    handleError: function(err, code, message, res){
        return res.status(code).json({
            message: message,
            error: err
        });
    },
}
