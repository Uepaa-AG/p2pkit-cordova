/**
* PPKCordovaBridge.m
* PPKCordovaBridge
*
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

#import <Cordova/CDV.h>
#import <P2PKit/P2Pkit.h>

@interface PPKCordovaBridge : CDVPlugin <PPKControllerDelegate> {
    NSString *cordovaCallbackHandlerId_;
}
@end

@implementation PPKCordovaBridge

#pragma mark - API

- (void)enableP2PKit:(CDVInvokedUrlCommand*)command {

    cordovaCallbackHandlerId_ = command.callbackId;

    dispatch_async(dispatch_get_main_queue(), ^{

        NSString* appKey = [command.arguments objectAtIndex:0];

        @try {
            [PPKController enableWithConfiguration:appKey observer:self];
        } @catch (NSException *exception) {
            [self invokePluginResultErrorWithString:[NSString stringWithFormat:@"Failed to enable p2pkit with exception %@", exception.description]];
        }
    });
}

-(void)disableP2PKit:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{

        [PPKController disable];
        [self invokePluginResultSuccessWithMethodName:@"onDisabled" parms:nil];
    });
}

-(void)getMyPeerId:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{

        if (![PPKController isEnabled]) {
            [self invokePluginResultErrorWithString:@"p2pkit is not enabled"];
            return;
        }

        [self invokePluginResultSuccessWithMethodName:@"onGetMyPeerId" parms:@{@"myPeerId":[PPKController myPeerID]}];
    });
}

-(void)startDiscovery:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{

        if (![PPKController isEnabled]) {
            [self invokePluginResultErrorWithString:@"p2pkit is not enabled"];
            return;
        }

        NSString* discoveryInfoBase64 = [command.arguments objectAtIndex:0];

        if (!discoveryInfoBase64) {
            [self invokePluginResultErrorWithString:@"could not extract discovery info"];
            return;
        }

        NSData *discoveryInfo = [[NSData alloc] initWithBase64EncodedString:discoveryInfoBase64 options:0];

        if (!discoveryInfo) {
            [self invokePluginResultErrorWithString:@"could not convert base64 discovery info to data"];
            return;
        }

        @try {
            [PPKController startDiscoveryWithDiscoveryInfo:discoveryInfo stateRestoration:NO];
        } @catch (NSException *exception) {
            [self invokePluginResultErrorWithString:[NSString stringWithFormat:@"Failed to start discovery with exception %@", exception.description]];
        }
    });
}

-(void)stopDiscovery:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{

        if (![PPKController isEnabled]) {
            [self invokePluginResultErrorWithString:@"p2pkit is not enabled"];
            return;
        }

        [PPKController stopDiscovery];
    });
}

-(void)enableProximityRanging:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{

        if (![PPKController isEnabled]) {
            [self invokePluginResultErrorWithString:@"p2pkit is not enabled"];
            return;
        }

        [PPKController enableProximityRanging];
    });
}

-(void)pushNewDiscoveryInfo:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{

        if (![PPKController isEnabled] || [PPKController discoveryState] == PPKDiscoveryStateStopped) {
            [self invokePluginResultErrorWithString:@"p2pkit is not enabled or discovery is not running"];
            return;
        }

        NSString* discoveryInfoBase64 = [command.arguments objectAtIndex:0];

        if (!discoveryInfoBase64) {
            [self invokePluginResultErrorWithString:@"could not extract discovery info"];
            return;
        }

        NSData *discoveryInfo = [[NSData alloc] initWithBase64EncodedString:discoveryInfoBase64 options:0];

        if (!discoveryInfo) {
            [self invokePluginResultErrorWithString:@"could not convert base64 discovery info to data"];
            return;
        }

        @try {
            [PPKController pushNewDiscoveryInfo:discoveryInfo];
        } @catch (NSException *exception) {
             [self invokePluginResultErrorWithString:[NSString stringWithFormat:@"Failed to update discovery info with exception %@", exception.description]];
        }
    });
}

-(void)setDiscoveryPowerMode:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{
        [self invokePluginResultErrorWithString:@"Power modes are not available on iOS"];
    });
}

-(void)getDiscoveryPowerMode:(CDVInvokedUrlCommand*)command  {

    dispatch_async(dispatch_get_main_queue(), ^{

        if (![PPKController isEnabled]) {
            [self invokePluginResultErrorWithString:@"p2pkit is not enabled"];
            return;
        }

        [self invokePluginResultSuccessWithMethodName:@"onGetDiscoveryPowerMode" parms:@{@"discoveryPowerMode":@"NOT_AVAILABLE_ON_IOS"}];
    });
}

#pragma mark - PPKControllerDelegate

-(void)PPKControllerInitialized {

    [self invokePluginResultSuccessWithMethodName:@"onEnabled" parms:nil];
}

-(void)PPKControllerFailedWithError:(PPKErrorCode)error {

    [self invokePluginResultSuccessWithMethodName:@"onError" parms:@{@"platform":@"ios", @"errorCode":[NSNumber numberWithInt:error]}];
}

-(void)discoveryStateChanged:(PPKDiscoveryState)state {

    [self invokePluginResultSuccessWithMethodName:@"onDiscoveryStateChanged" parms:@{@"platform":@"ios", @"state":[NSNumber numberWithInt:state]}];
}

-(void)peerDiscovered:(PPKPeer *)peer {

    [self invokePluginResultSuccessWithMethodName:@"onPeerDiscovered" parms:[self createDictionaryFromPeer:peer]];
}

-(void)peerLost:(PPKPeer *)peer {

    [self invokePluginResultSuccessWithMethodName:@"onPeerLost" parms:[self createDictionaryFromPeer:peer]];
}

-(void)discoveryInfoUpdatedForPeer:(PPKPeer *)peer {

    [self invokePluginResultSuccessWithMethodName:@"onPeerUpdatedDiscoveryInfo" parms:[self createDictionaryFromPeer:peer]];
}

-(void)proximityStrengthChangedForPeer:(PPKPeer *)peer {

    [self invokePluginResultSuccessWithMethodName:@"onProximityStrengthChanged" parms:[self createDictionaryFromPeer:peer]];
}

#pragma mark - Helpers

-(void)invokePluginResultErrorWithString:(NSString*)errorString {

    if (!errorString) {
        return;
    }

    [self invokePluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorString]];
}

-(void)invokePluginResultSuccessWithMethodName:(NSString*)name parms:(NSDictionary*)parms {

    if (!name) {
        return;
    }

    NSMutableDictionary *statusMessage = [NSMutableDictionary new];
    [statusMessage setObject:name forKey:@"methodName"];
    if(parms) [statusMessage setObject:parms forKey:@"parms"];

    [self invokePluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:statusMessage]];
}

-(void)invokePluginResult:(CDVPluginResult *)result {

    if (!cordovaCallbackHandlerId_) {
        if ([PPKController isEnabled]) {
            [PPKController disable];
        }
        return;
    }

    if (!result) {
        return;
    }

    [result setKeepCallbackAsBool:YES];

    [self.commandDelegate sendPluginResult:result callbackId:cordovaCallbackHandlerId_];
}

-(NSDictionary*)createDictionaryFromPeer:(PPKPeer*)peer {

    NSString *peerID = peer.peerID;
    NSString *discoveryInfo = [peer.discoveryInfo base64EncodedStringWithOptions:0];
    NSString *proximityStrength = [NSString stringWithFormat:@"%.0li", (long)peer.proximityStrength];

    return @{@"peerID":peerID, @"discoveryInfo":discoveryInfo, @"proximityStrength":proximityStrength};
}

@end
