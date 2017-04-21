# p2pkit.io Cordova plugin for iOS & Android (alpha)

#### p2pkit is a peer-to-peer proximity SDK for iOS and Android

p2pkit is an easy to use SDK that bundles together several proximity technologies kung-fu style! With p2pkit apps immediately understand their proximity to nearby devices and users, estimate their range and exchange information with them.


## Get Started

1. Using p2pkit requires an application key, start by creating a p2pkit account here:
[Create p2pkit account](http://p2pkit.io/signup.html)

2. Once you have an account you can log-in to the console and create an application key: [Create your Application Key](https://p2pkit-console.uepaa.ch/login). p2pkit validates bundleID/package_name so <strong>don't forget to</strong> add ``your.package.name`` to the known BundleID/package_names when creating your application key

3. Download (or clone) the cordova plugin and unzip it:
[Download plugin](https://github.com/Uepaa-AG/p2pkit-cordova/archive/master.zip)

4. Download and place P2PKit.framework in the cordova-plugin-p2pkit-io/lib/ios folder:
[Download p2pkit](http://p2pkit.io/developer/get-started/ios/#download)

5. Add the cordova plugin to your project
```
cordova plugin add PATH_TO_PLUGIN --variable BluetoothPeripheralUsageDescription="describe why you use p2pkit" --save
```

6. iOS only: Remove and add the platform
```
cordova platform remove ios
cordova platform add ios
```

## Example

Here is an example app class extension that implements p2pkit functionality begin by calling <code>startP2PKit()</code>:
```
    p2pkitCallback : {

        onEnabled: function() {
            console.log('p2pkit is enabled')
            cordova.plugins.p2pkit.enableProximityRanging()
            cordova.plugins.p2pkit.startDiscovery('', cordova.plugins.p2pkit.HIGH_PERFORMANCE) // base64 encoded Data (bytes)
        },

        onDisabled: function() {
            console.log('p2pkit is disabled')
        },

        onError: function(errorObject) {
            console.log('p2pkit failed to enable on platform ' + errorObject.platform + ' with error code ' + errorObject.errorCode)
        },
        
        onException: function(exception) {
          console.log('p2pkit crashed with exception ' +exception.exception)
      	},

        onDiscoveryStateChanged: function(discoveryStateObject) {
            console.log('discovery state updated on platform ' + discoveryStateObject.platform + ' with state ' + discoveryStateObject.state)
        },

        onPeerDiscovered: function(peer) {
            console.log('peer discovered '+peer.peerID)
        },

        onPeerLost: function(peer) {
            console.log('peer lost '+peer.peerID)
        },

        onPeerUpdatedDiscoveryInfo: function(peer) {
            console.log('discovery info updated for peer ' + peer.peerID + ' info ' + peer.discoveryInfo)
        },

        onProximityStrengthChanged: function(peer) {
            console.log('proximity strength changed for peer ' + peer.peerID + ' proximity strength ' + peer.proximityStrength)
        },

        onGetMyPeerId: function(reply) {
            console.log(reply.myPeerId)
        },

        onGetDiscoveryPowerMode: function(reply) {
            console.log(reply.discoveryPowerMode);
        }
    },

    ppkErrorListener: function(error) {
        console.log(error)
    },

    startP2PKit: function() {
        cordova.plugins.p2pkit.enable("<YOUR APPLICATION KEY>", this.p2pkitCallback, this.ppkErrorListener)
    }
```

## Documentation
The full API for the plugin is available at <code>www/ppk.js</code>

In general, a tutorial as well as more documentation for p2pkit is available on our website at:
[http://p2pkit.io/developer](http://p2pkit.io/developer)

## p2pkit License
* By using P2PKit you agree to abide by our Terms of Service, License Agreement and Policies which are available here: http://p2pkit.io/policy.html
* Please refer to "Third_party_licenses.txt" included with P2PKit.framework for 3rd party software that P2PKit.framework may be using - You will need to abide by their licenses as well
