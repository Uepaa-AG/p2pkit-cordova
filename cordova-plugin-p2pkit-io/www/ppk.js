/**
* Copyright (c) 2017 by Uepaa AG, Zürich, Switzerland.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

var exec = require('cordova/exec');

exports.enable = function(appkey, p2pkitCallbackHandler, pluginErrorListener){

  var callback = function(nativeCallback) {

        if(p2pkitCallbackHandler[nativeCallback.methodName]) {
            p2pkitCallbackHandler[nativeCallback.methodName](nativeCallback.parms);
        }
    }

    var errorCallback = function(error) {
      pluginErrorListener(error)
    }

    exec(callback, errorCallback, 'p2pkit', 'enableP2PKit', [appkey]);
};

exports.disable = function(){
    exec(null, null, 'p2pkit', 'disableP2PKit', null);
};

exports.getMyPeerId = function(myPeerId){
    exec(null, null, 'p2pkit', 'getMyPeerId', null);
};

exports.startDiscovery = function(discoveryInfo){
    exec(null, null, 'p2pkit', 'startDiscovery', [discoveryInfo]);
};

exports.stopDiscovery = function(){
    exec(null, null, 'p2pkit', 'stopDiscovery');
};

exports.enableProximityRanging = function(){
    exec(null, null, 'p2pkit', 'enableProximityRanging');
};

exports.pushNewDiscoveryInfo = function(discoveryInfo){
    exec(null, null, 'p2pkit', 'pushNewDiscoveryInfo', [discoveryInfo]);
};
