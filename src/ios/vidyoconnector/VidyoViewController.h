#ifndef VIDYOVIEWCONTROLLER_H_INCLUDED
#define VIDYOVIEWCONTROLLER_H_INCLUDED
//
//  VidyoViewController.h
//
//  Copyright © 2017 Vidyo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <VidyoClientIOS/Lmi/VidyoClient/VidyoConnector_Objc.h>
#import "VidyoIOPlugin.h"
#import "Logger.h"

enum VIDYO_CONNECTOR_STATE {
    VC_CONNECTED,
    VC_DISCONNECTED,
    VC_DISCONNECTED_UNEXPECTED,
    VC_CONNECTION_FAILURE
};

@interface VidyoViewController : UIViewController <UITextFieldDelegate, VCConnectorIConnect, VCConnectorIRegisterLocalCameraEventListener, VCConnectorIRegisterLogEventListener> {
@private
    VCConnector *vc;
    VCLocalCamera *lastSelectedCamera;
    Logger    *logger;
    UIImage   *callStartImage;
    UIImage   *callEndImage;
    BOOL      microphonePrivacy;
    BOOL      cameraPrivacy;
    BOOL      devicesSelected;
    BOOL      hideConfig;
    BOOL      autoJoin;
    BOOL      allowReconnect;
    BOOL      enableDebug;
    NSString  *returnURL;
    enum VIDYO_CONNECTOR_STATE vidyoConnectorState;
    CGFloat   keyboardOffset;
    NSString  *experimentalOptions;
}
    
@property (weak, nonatomic) IBOutlet UITextField *host;
@property (weak, nonatomic) IBOutlet UITextField *displayName;
@property (weak, nonatomic) IBOutlet UITextField *token;
@property (weak, nonatomic) IBOutlet UITextField *resourceId;
@property (weak, nonatomic) IBOutlet UILabel     *toolbarStatusText;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *connectionSpinner;

@property (weak, nonatomic) IBOutlet UIButton *closeButton;
@property (weak, nonatomic) IBOutlet UIButton *toggleConnectButton;
@property (weak, nonatomic) IBOutlet UIButton *microphonePrivacyButton;
@property (weak, nonatomic) IBOutlet UIButton *cameraPrivacyButton;
@property (weak, nonatomic) IBOutlet UIButton *cameraSwapButton;

@property (weak, nonatomic) IBOutlet UIView  *controlsView;
@property (weak, nonatomic) IBOutlet UIView  *videoView;
@property (weak, nonatomic) IBOutlet UIView  *toolbarView;
@property (weak, nonatomic) IBOutlet UIView  *toggleToolbarView;
@property (weak, nonatomic) IBOutlet UILabel *bottomControlSeparator;

@property (weak, nonatomic) VidyoIOPlugin* plugin;
    
- (IBAction)closeButtonPressed:(id)sender;
- (IBAction)toggleConnectButtonPressed:(id)sender;
- (IBAction)cameraPrivacyButtonPressed:(id)sender;
- (IBAction)microphonePrivacyButtonPressed:(id)sender;
- (IBAction)cameraSwapButtonPressed:(id)sender;
- (IBAction)toggleToolbar:(UITapGestureRecognizer *)sender;

/* Open for plugin extension */
- (void) disconnect;
- (void) close;
    
@end
#endif // VIDYOVIEWCONTROLLER_H_INCLUDED
