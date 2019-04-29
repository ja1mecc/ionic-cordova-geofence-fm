#import "GeofenceFM.h"

#import <Cordova/CDVAvailability.h>

@implementation GeofenceFM

- (void)pluginInitialize {
}

- (void)init:(CDVInvokedUrlCommand*)command
{
    NSLog(@"init -> GeofenceFM");
    self.callbackId = command.callbackId;
    
    self.locationManager = [[CLLocationManager alloc] init];
    [self.locationManager setDelegate:self];

    if ([self.locationManager respondsToSelector:@selector(requestAlwaysAuthorization)]) {
        [self.locationManager requestAlwaysAuthorization];
    }

    [self.locationManager startUpdatingLocation];
    
    NSString* msg = [NSString stringWithFormat: @"OK"];
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)addOrUpdateFence:(CDVInvokedUrlCommand*)command
{
    
    NSArray* array = [command.arguments objectAtIndex:0];
    
    for (id object in array) {
        
        NSMutableDictionary* options = object;
        double latitud = [[options objectForKey:@"latitud"] doubleValue];
        double longitud = [[options objectForKey:@"longitud"] doubleValue];
        double radius = [[options objectForKey:@"radius"] doubleValue];
        NSString* _id = [options objectForKey:@"id"];
        
        CLLocationCoordinate2D center = CLLocationCoordinate2DMake(latitud, longitud);
        CLRegion *bridge = [[CLCircularRegion alloc]initWithCenter:center radius:radius identifier:_id];
        
        [self.locationManager startMonitoringForRegion:bridge];
    }
    
    NSString* msg = [NSString stringWithFormat: @"OK"];
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

-(void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region {
    NSLog(@"Welcome to %@", region.identifier);
    
    NSArray *array = [region.identifier componentsSeparatedByString:@"|"];
    NSLog(@"%@",array);
    [self loginSession:array[1] changeArea:array[0] deviceToken:array[2] action:@"entrada"];
}


-(void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region {
    NSLog(@"Bye bye %@", region.identifier);
    NSArray *array = [region.identifier componentsSeparatedByString:@"|"];
    NSLog(@"%@",array);
    [self loginSession:array[1] changeArea:array[0] deviceToken:array[2] action:@"salida"];
}

-(void)locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region {
    NSLog(@"Now monitoring for %@", region.identifier);
    [self.locationManager performSelector:@selector(requestStateForRegion:) withObject:region afterDelay:5];

}

- (void)locationManager:(CLLocationManager *)manager didDetermineState:(CLRegionState)state forRegion:(CLRegion *)region {
    
    if (state == CLRegionStateInside){
        
        [self enterGeofence:region];
        
    } else if (state == CLRegionStateOutside){
        
        [self exitGeofence:region];
        
    } else if (state == CLRegionStateUnknown){
        NSLog(@"Unknown state for geofence: %@", region);
        return;
    }
}

- (void)enterGeofence:(CLRegion *)region {
    NSLog(@"enterGeofence -> %@", region.identifier);
    
    NSArray *array = [region.identifier componentsSeparatedByString:@"|"];
    NSLog(@"%@",array);
    [self loginSession:array[1] changeArea:array[0] deviceToken:array[2] action:@"entrada"];
}

- (void)exitGeofence:(CLRegion *)region {
    NSLog(@"exitGeofence -> %@", region.identifier);
    
    NSArray *array = [region.identifier componentsSeparatedByString:@"|"];
    NSLog(@"%@",array);
    [self loginSession:array[1] changeArea:array[0] deviceToken:array[2] action:@"salida"];
}

-(void) loginSession:(NSString *) rut changeArea:(NSString *) identifier deviceToken:(NSString *) deviceToken action:(NSString *)action {

    // 1
    NSURL *url = [NSURL URLWithString:@"http://www.finmarketsbackup.cl/jsepulveda/collahuasi-sos/api/login"];
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:config];
    
    // 2
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    request.HTTPMethod = @"POST";
    
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    // 3
    NSDictionary *dictionary = @{@"rut": rut, @"clave": deviceToken};
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:dictionary
                                                   options:kNilOptions error:&error];
    
    if (!error) {
        // 4
        NSURLSessionUploadTask *uploadTask = [session uploadTaskWithRequest:request
                                                                   fromData:data completionHandler:^(NSData *data,NSURLResponse *response,NSError *error) {
                                                                       // Handle response here
                                                                       NSLog(@"%@",data);
                                                                       NSLog(@"%@",response);
                                                                       NSLog(@"%@",error);
                                                                       
                                                                       NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
                                                                       
                                                                       NSLog(@"jsonSession: %@",  [[json objectForKey:@"data"] objectForKey:@"token"]);
                                                                       [self callServiceEnterArea:identifier tokenSession:[[json objectForKey:@"data"] objectForKey:@"token"] action:action];
                                                                   }];
        
        // 5
        [uploadTask resume];
    }
    
}

-(void) callServiceEnterArea:(NSString *)area tokenSession:(NSString *)token action:(NSString *)action {
    
    NSString *tokenSession = [NSString stringWithFormat:@"Bearer %@", token];
    
    // 1
    NSURL *url = [NSURL URLWithString:@"http://www.finmarketsbackup.cl/jsepulveda/collahuasi-sos/api/user/updatearea"];
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:config];
    
    // 2
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    request.HTTPMethod = @"POST";
    
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:tokenSession forHTTPHeaderField:@"Authorization"];
    
    // 3
    NSDictionary *dictionary = @{@"area": area, @"action": action};
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:dictionary
                                                   options:kNilOptions error:&error];
    
    if (!error) {
        // 4
        NSURLSessionUploadTask *uploadTask = [session uploadTaskWithRequest:request
                                                                   fromData:data completionHandler:^(NSData *data,NSURLResponse *response,NSError *error) {
                                                                       // Handle response here
                                                                       NSLog(@"%@",data);
                                                                       NSLog(@"%@",response);
                                                                       NSLog(@"%@",error);
                                                                       
                                                                       NSMutableArray *json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
                                                                       NSLog(@"json: %@", json);
                                                                   }];
        
        // 5
        [uploadTask resume];
    }
    
}

@end
