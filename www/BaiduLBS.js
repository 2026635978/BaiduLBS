var exec = require('cordova/exec');

exports.getCurrentLocation = function (success, error) {
    exec(success, error, "BaiduLBS", "getCurrentLocation", []);
};
