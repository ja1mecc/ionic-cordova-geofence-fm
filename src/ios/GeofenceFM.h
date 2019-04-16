#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import <Cordova/CDVPlugin.h>
#import <CoreLocation/CoreLocation.h>

@interface GeofenceFM : CDVPlugin <CLLocationManagerDelegate> {
}

// Encabezados de las funciones del plugin
- (void) init:(CDVInvokedUrlCommand*)command;
- (void) addOrUpdateFence:(CDVInvokedUrlCommand*)command;

@property (strong, nonatomic) CLLocationManager *locationManager;
@property (strong, nonatomic) CDVInvokedUrlCommand *command;
@property (nonatomic, copy) NSString *callbackId;

@end
