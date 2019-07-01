
var exec = require('cordova/exec');

module.exports = {
  init: function init(successCallback, errorCallback){
    exec(successCallback, errorCallback, 'GeofenceFM', "init", []);
  },
  addOrUpdateFence: function addOrUpdateFence(data, successCallback, errorCallback){
    exec(successCallback, errorCallback, 'GeofenceFM', "addOrUpdateFence", [data]);
  },
  removeAllFences: function removeAllFences(successCallback, errorCallback){
    exec(successCallback, errorCallback, 'GeofenceFM', "removeAllFences", []);
  },

  readIsButtonDisabled: function readIsButtonDisabled(successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'GeofenceFM', "readIsButtonDisabled", [])
  },
  storeDisabledButton: function  storeDisabledButton(data, successCallback, errorCallback) {
    console.log('hola desde metodo de plugin');
    exec(successCallback, errorCallback, 'GeofenceFM', "storeDisabledButton", [data])
  }
};
