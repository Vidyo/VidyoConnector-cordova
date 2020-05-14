#import "VidyoIOPlugin.h"
#import "VidyoViewController.h"

@implementation VidyoIOPlugin

- (void)pluginInitialize {
    // Register the application default settings from the Settings.bundle to the NSUserDefaults object.
    // Here, the user defaults are loaded only the first time the app is loaded and run.
    
    NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];
    
    NSString *settingsBundle = [[NSBundle mainBundle] pathForResource:@"Settings" ofType:@"bundle"];
    if (!settingsBundle) {
        NSLog(@"Could not find Settings.bundle");
    } else {
        NSDictionary *settings = [NSDictionary dictionaryWithContentsOfFile:[settingsBundle stringByAppendingPathComponent:@"Root.plist"]];
        NSArray *preferences = [settings objectForKey:@"PreferenceSpecifiers"];
        
        for (NSDictionary *prefSpecification in preferences) {
            NSString *key = [prefSpecification objectForKey:@"Key"];
            if (key) {
                // Check if this key was already registered
                if (![standardUserDefaults objectForKey:key]) {
                    [standardUserDefaults setObject:[prefSpecification objectForKey:@"DefaultValue"] forKey:key];
                    
                    NSLog( @"writing as default %@ to the key %@", [prefSpecification objectForKey:@"DefaultValue"], key );
                }
            }
        }
    }
}

- (void)passConnectEvent:(NSString*)event reason: (NSString*)reason  {
    NSDictionary *payload = [NSDictionary dictionaryWithObjectsAndKeys: event, @"event", reason, @"reason", nil];
    [self reportEvent: payload];
}

- (void)passDeviceStateEvent:(NSString*)event muted: (NSString*)muted  {
    NSDictionary *payload = [NSDictionary dictionaryWithObjectsAndKeys: event, @"event", muted, @"muted", nil];
    [self reportEvent: payload];
}

- (void)reportEvent:(NSDictionary*)payload {
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:payload];
    [pluginResult setKeepCallbackAsBool:YES];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:pluginCommand.callbackId];
}

- (void)launchVidyoIO:(CDVInvokedUrlCommand *)command {
    /* Store command for further reference */
    pluginCommand = command;
    
    NSString* token = [command.arguments objectAtIndex:0];
    NSString* host = [command.arguments objectAtIndex:1];
    NSString* displayName = [command.arguments objectAtIndex:2];
    NSString* resourceId = [command.arguments objectAtIndex:3];
    
    NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];
    
    if (token != nil) {
        [standardUserDefaults setObject:token forKey:@"token"];
    }
    
    if (host != nil) {
        [standardUserDefaults setObject:host forKey:@"host"];
    }
    
    if (displayName != nil) {
        [standardUserDefaults setObject:displayName forKey:@"displayName"];
    }
    
    if (resourceId != nil) {
        [standardUserDefaults setObject:resourceId forKey:@"resourceId"];
    }
    
    [standardUserDefaults setBool:YES forKey:@"autoJoin"];
    [standardUserDefaults setBool:YES forKey:@"hideConfig"];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Vidyo" bundle:nil];
    self.vidyoViewController = [storyboard instantiateViewControllerWithIdentifier:@"VidyoViewController"];
        
    if(self.vidyoViewController == nil) {
        self.vidyoViewController = [[VidyoViewController alloc] init];
    }
    
    /* Disable slide down */
    if (@available(iOS 13.0, *)) {
        self.vidyoViewController.modalInPresentation = TRUE;
    }
    
    self.vidyoViewController.plugin = self;
    
    [self.viewController presentViewController:self.vidyoViewController animated:YES completion:nil];
}

- (void)disconnect:(CDVInvokedUrlCommand *)command {
    if (self.vidyoViewController != nil) {
        [self.vidyoViewController disconnect];
    }
}

- (void)release:(CDVInvokedUrlCommand *)command {
    if (self.vidyoViewController != nil) {
        [self.vidyoViewController close];
    }
}

- (void)destroy {
    self.vidyoViewController.plugin = nil;
    self.vidyoViewController = nil;
}

@end

