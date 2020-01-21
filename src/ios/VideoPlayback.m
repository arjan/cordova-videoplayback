//
//  CDVVideo.m
//  
//
//  Updated by Arjan Scherpenisse 2015-03-26
//  Updated by Tom Krones 2013-09-30.
//  Created by Peter Robinett on 2012-10-15.
//
//

#import "VideoPlayback.h"
#import <AVKit/AVKit.h>
#import <Cordova/CDV.h>
#import <CommonCrypto/CommonDigest.h>

@implementation VideoPlayback
- (void) playVideo:(CDVInvokedUrlCommand*)command
{
  NSString *movie = [command.arguments objectAtIndex:0];

  [self doEnsureDownload:movie withCallback:^(BOOL result){
    if (!result) {
      return;
    }
    NSString *cacheFile = [self cacheFile:movie];
    NSURL *fileURL = [NSURL fileURLWithPath:cacheFile];
      
      AVPlayerViewController *moviePlayer = [[AVPlayerViewController alloc] init];
      moviePlayer.player = [AVPlayer playerWithURL:fileURL];
      [self.viewController presentViewController:moviePlayer animated:YES completion:nil];
      moviePlayer.view.frame = self.viewController.view.frame;
      
      [moviePlayer.player play];
  }];
}

- (void) ensureDownloaded:(CDVInvokedUrlCommand*)command
{
  NSString *movie = [command.arguments objectAtIndex:0];
  [self doEnsureDownload:movie withCallback:^(BOOL result){
    CDVPluginResult *pluginResult = [ CDVPluginResult
                                     resultWithStatus    : CDVCommandStatus_OK];
    if (!result) {
      pluginResult = [ CDVPluginResult
                      resultWithStatus: CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

  }];
}

- (void) doEnsureDownload: (NSString *)movie withCallback: ( void ( ^ )( bool ) )callback {

  NSString *cacheFile = [self cacheFile:movie];
  
  BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:cacheFile];
  if (fileExists) {
    NSLog(@"Cache file exists.");
    // just fire the callback
    callback(YES);
    
  } else {
    [self showIndicator];
    [self showToastWithMessage: @"Please wait while downloading the movie..."];
    
    // dl the file
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
      
      NSData *downloadedData = [NSData dataWithContentsOfURL:[NSURL URLWithString:movie]];
      
      bool result = YES;
      
      if (downloadedData) {
        [downloadedData writeToFile:cacheFile atomically:YES];
        NSLog(@"DownloadOK!!!!!!!!!!!!!!!!");
        
      } else {
        result = NO;
      }
      
      dispatch_async(dispatch_get_main_queue(), ^{
        [indicator stopAnimating];
        callback(result);
      });
      
      NSLog(@"Done.");
    });
  }

}

- (void) showIndicator {
  indicator = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
  indicator.center = self.viewController.view.center;
  indicator.hidesWhenStopped = YES;
  [self.viewController.view addSubview:indicator];
  [indicator startAnimating];
}

- (void)dealloc {
  //[player release];
  //[movie release];
  //[super dealloc];
}

- (NSString*)md5HexDigest:(NSString*)input {
  const char* str = [input UTF8String];
  unsigned char result[CC_MD5_DIGEST_LENGTH];
  CC_MD5(str, (CC_LONG)strlen(str), result);
  
  NSMutableString *ret = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH*2];
  for(int i = 0; i<CC_MD5_DIGEST_LENGTH; i++) {
    [ret appendFormat:@"%02x",result[i]];
  }
  return ret;
}

- (NSString *)cachePath {
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
  NSString *cachePath = [paths objectAtIndex:0];
  BOOL isDir = NO;
  NSError *error;
  if (! [[NSFileManager defaultManager] fileExistsAtPath:cachePath isDirectory:&isDir] && isDir == NO) {
    [[NSFileManager defaultManager] createDirectoryAtPath:cachePath withIntermediateDirectories:NO attributes:nil error:&error];
  }

  return cachePath;
}

- (NSString *)cacheFile: (NSString *)movie {
  NSString *cachePath = [self cachePath];
  NSString *fn = [[self md5HexDigest:movie] stringByAppendingString: @".mp4"];
  return [cachePath stringByAppendingPathComponent:fn];
}


- (void) showToastWithMessage: (NSString *)message {
  UIAlertView *toast = [[UIAlertView alloc] initWithTitle:nil
                                                  message:message
                                                 delegate:nil
                                        cancelButtonTitle:nil
                                        otherButtonTitles:nil, nil];
  [toast show];
  
  int duration = 2; // duration in seconds
  
  dispatch_after(dispatch_time(DISPATCH_TIME_NOW, duration * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
    [toast dismissWithClickedButtonIndex:0 animated:YES];
  });

}

@end
