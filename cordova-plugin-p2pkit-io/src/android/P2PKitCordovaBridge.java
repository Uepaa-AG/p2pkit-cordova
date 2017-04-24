package ch.uepaa.p2pkit;

import android.util.Base64;
import android.util.Log;

import ch.uepaa.p2pkit.discovery.*;
import ch.uepaa.p2pkit.discovery.Peer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;


public class P2PKitCordovaBridge extends CordovaPlugin {

    private CallbackContext cordovaCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (callbackContext == null) {
            return false;
        }

        if (cordovaCallbackContext == null && "enableP2PKit".equals(action)) {
            cordovaCallbackContext = callbackContext;
        }

        if ("enableP2PKit".equals(action)) {
            String appkey = args.getString(0);
            enableP2PKit(appkey);
            return true;
        }

        if ("disableP2PKit".equals(action)) {
            disableP2PKit();
            return true;
        }

        if ("getMyPeerId".equals(action)) {
            getMyPeerId();
            return true;
        }

        if ("startDiscovery".equals(action)) {
            String discoveryInfo = args.getString(0);
            String discoveryPowerMode = args.getString(1);
            startDiscovery(discoveryInfo, discoveryPowerMode);
            return true;
        }

        if ("stopDiscovery".equals(action)) {
            stopDiscovery();
            return true;
        }

        if ("enableProximityRanging".equals(action)) {
            enableProximityRanging();
            return true;
        }

        if ("pushNewDiscoveryInfo".equals(action)) {
            String discoveryInfo = args.getString(0);
            pushNewDiscoveryInfo(discoveryInfo);
            return true;
        }
        
        if ("getDiscoveryPowerMode".equals(action)) {
        	getDiscoveryPowerMode();
        	return true;
        }
        
        if ("setDiscoveryPowerMode".equals(action)) {
        	String discoveryPowerMode = args.getString(0);
        	setDiscoveryPowerMode(discoveryPowerMode);
        	return true;
        }

        return false;
    }

    private void enableP2PKit(String appKey) {

        try {
            Context context = this.cordova.getActivity().getApplicationContext();
            P2PKit.enable(context, appKey, p2pKitStatusListener);

        } catch (AlreadyEnabledException e) {
            invokePluginResultError("Failed ot enable p2pkit with error "+e.toString());
        }
    }

    private void disableP2PKit() {
        P2PKit.disable();
    }

    private void getMyPeerId() {

        if (!P2PKit.isEnabled()) {
            invokePluginResultError("p2pkit is not enabled");
            return;
        }

        try {
            invokePluginResultSuccess("onGetMyPeerId", new JSONObject().put("myPeerId", P2PKit.getMyPeerId().toString()));
        } catch (JSONException e) {
            invokePluginResultError("Failed to get my peer id with exception " +e.toString());
        }
    }

    private void startDiscovery(String discoveryInfoBase64String, String discoveryPowerMode) {

        if (!P2PKit.isEnabled()) {
            invokePluginResultError("p2pkit is not enabled");
            return;
        }

        byte [] discoveryInfo = null;

        if (discoveryInfoBase64String != null) {
            discoveryInfo = Base64.decode(discoveryInfoBase64String,Base64.DEFAULT);
        }
        
        DiscoveryPowerMode powerModeToUse = getDiscoveryPowerModeFromString(discoveryPowerMode);
        if (powerModeToUse == null) {
        	invokePluginResultError("Unknown DiscoveryPowerMode: " + discoveryPowerMode);
        } else {

        	try {
            	P2PKit.startDiscovery(discoveryInfo, powerModeToUse, mDiscoveryListener);
        	} catch (DiscoveryInfoTooLongException e) {
            	invokePluginResultError("Failed to start discovery with exception " +e.toString());
        	}
        }
    }

    private void stopDiscovery() {

        if (!P2PKit.isEnabled()) {
            invokePluginResultError("p2pkit is not enabled");
            return;
        }

        P2PKit.stopDiscovery();
    }

    private void enableProximityRanging() {

        if (!P2PKit.isEnabled()) {
            invokePluginResultError("p2pkit is not enabled");
            return;
        }

        P2PKit.enableProximityRanging();
    }

    private void pushNewDiscoveryInfo(String discoveryInfoBase64String) {

        if (!P2PKit.isEnabled()) {
            invokePluginResultError("p2pkit is not enabled");
            return;
        }

        byte [] discoveryInfo = null;

        if (discoveryInfoBase64String != null) {

            try {
                discoveryInfo = Base64.decode(discoveryInfoBase64String,Base64.DEFAULT);
            }catch (Exception e) {
                invokePluginResultError("Failed to extract base64 encoded discovery info with exception " +e.toString());
                return;
            }
        }

        try {
            P2PKit.pushDiscoveryInfo(discoveryInfo);
        } catch (DiscoveryInfoTooLongException e) {
            invokePluginResultError("Failed to update discovery info with exception " +e.toString());
        } catch (DiscoveryInfoUpdatedTooOftenException e) {
            invokePluginResultError("Failed to update discovery info with exception " +e.toString());
        }

    }
    
    private void getDiscoveryPowerMode() {

        if (!P2PKit.isEnabled()) {
            invokePluginResultError("p2pkit is not enabled");
            return;
        }
        
        try {
        	invokePluginResultSuccess("onGetDiscoveryPowerMode", new JSONObject().put("discoveryPowerMode", P2PKit.getDiscoveryPowerMode()));
        } catch (JSONException e) {
        	invokePluginResultError("Failed to get the current DiscoveryPowerMode with exception " +e.toString());
        }
    }
    
    private void setDiscoveryPowerMode(String discoveryPowerMode) {
    	
    	if (!P2PKit.isEnabled()) {
            invokePluginResultError("p2pkit is not enabled");
            return;
        }
        
        DiscoveryPowerMode powerModeToSet = getDiscoveryPowerModeFromString(discoveryPowerMode);
        
        if (powerModeToSet == null) {
        	invokePluginResultError("Unknown DiscoveryPowerMode: " + discoveryPowerMode);
        } else {
        	P2PKit.setDiscoveryPowerMode(powerModeToSet);
        }
    }

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {

        @Override
        public void onStateChanged(final int state) {

            try {
                invokePluginResultSuccess("onDiscoveryStateChanged", new JSONObject().put("platform", "android").put("state",String.valueOf(state)));
            } catch (JSONException e) {
                invokePluginResultError("Failed to create json result with exception " +e.toString());
            }
        }

        @Override
        public void onPeerDiscovered(final Peer peer) {

            try {
                invokePluginResultSuccess("onPeerDiscovered", createJSONFromPeer(peer));
            } catch (JSONException e) {
                invokePluginResultError("Failed to create json result with exception " +e.toString());
            }
        }

        @Override
        public void onPeerLost(final Peer peer) {

            try {
                invokePluginResultSuccess("onPeerLost", createJSONFromPeer(peer));
            } catch (JSONException e) {
                invokePluginResultError("Failed to create json result with exception " +e.toString());
            }
        }

        @Override
        public void onPeerUpdatedDiscoveryInfo(Peer peer) {

            try {
                invokePluginResultSuccess("onPeerUpdatedDiscoveryInfo", createJSONFromPeer(peer));
            } catch (JSONException e) {
                invokePluginResultError("Failed to create json result with exception " +e.toString());
            }
        }

        @Override
        public void onProximityStrengthChanged(Peer peer) {

            try {
                invokePluginResultSuccess("onProximityStrengthChanged", createJSONFromPeer(peer));
            } catch (JSONException e) {
                invokePluginResultError("Failed to create json result with exception " +e.toString());
            }
        }
    };


    private final P2PKitStatusListener p2pKitStatusListener = new P2PKitStatusListener() {

        @Override
        public void onEnabled() {
            invokePluginResultSuccess("onEnabled", null);
        }

        @Override
        public void onDisabled() {
            invokePluginResultSuccess("onDisabled", null);
        }

        @Override
        public void onError(StatusResult statusResult) {

            try {
                invokePluginResultSuccess("onError", new JSONObject().put("platform", "android").put("errorCode",String.valueOf(statusResult.getStatusCode())));
            } catch (JSONException e) {
                invokePluginResultError("Failed to create json result with exception " +e.toString());
            }
        }
        
        @Override
        public void onException(Throwable throwable) {
        	try {
        		invokePluginResultSuccess("onException", new JSONObject().put("platform", "android").put("exception", ""+Log.getStackTraceString(throwable)));
        	} catch (JSONException e) {
        		invokePluginResultError("Failed to create json result with exception " +e.toString());
        	}
        }
    };

    private void invokePluginResultError(String errorString) {

        if (errorString == null) {
            return;
        }

        PluginResult result = new PluginResult(PluginResult.Status.ERROR, errorString);
        invokePluginResult(result);
    }

    private void invokePluginResultSuccess(String methodName, JSONObject parms) {

        if (methodName == null) {
            return;
        }

        JSONObject jsonReply = new JSONObject();

        try {
            jsonReply.put("methodName",methodName);
            if (parms != null) jsonReply.put("parms",parms);
        } catch (JSONException e) {
           invokePluginResultError("error creating json plugin result");
            return;
        }

        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonReply);
        invokePluginResult(result);
    }

    private void invokePluginResult(PluginResult result) {

        if (cordovaCallbackContext == null) {
            P2PKit.disable();
            return;
        }

        if (result == null) {
            return;
        }

        result.setKeepCallback(true);
        cordovaCallbackContext.sendPluginResult(result);
    }

    private JSONObject createJSONFromPeer(Peer peer) throws JSONException {

        String peerID = peer.getPeerId().toString();
        String discoveryInfo = Base64.encodeToString(peer.getDiscoveryInfo(),Base64.DEFAULT);
        String proximityStrength = String.valueOf(peer.getProximityStrength());

        JSONObject jsonPeer = new JSONObject();

        jsonPeer.put("peerID",peerID);
        jsonPeer.put("discoveryInfo",discoveryInfo);
        jsonPeer.put("proximityStrength",proximityStrength);

        return jsonPeer;
    }
    
    private DiscoveryPowerMode getDiscoveryPowerModeFromString(String discoveryPowerMode) {
    
    	if (DiscoveryPowerMode.LOW_POWER.name().equals(discoveryPowerMode)) {
    		return DiscoveryPowerMode.LOW_POWER;
    	} else if (DiscoveryPowerMode.HIGH_PERFORMANCE.name().equals(discoveryPowerMode)) {
    		return DiscoveryPowerMode.HIGH_PERFORMANCE;
    	} else {
    		return null;
    	}
    }
}
