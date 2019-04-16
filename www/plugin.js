
var exec = require('cordova/exec');

module.exports = {
  init: function init(successCallback, errorCallback){
    exec(successCallback, errorCallback, 'MiPlugin', "init", []);
  },
  addOrUpdateFence: function addOrUpdateFence(data, successCallback, errorCallback){
    exec(successCallback, errorCallback, 'MiPlugin', "addOrUpdateFence", [data]);
  },
};
