//
//  CDVVideo.h
//  
//
//  Updated by Arjan Scherpenisse 2015-03-26
//  Updated by Tom Krones 2013-09-30.
//  Created by Peter Robinett on 2012-10-15.
//
//

#import <Cordova/CDV.h>
#import "MovieViewController.h"

@interface VideoPlayback : CDVPlugin {
  MovieViewController *player;
  NSString *movie;
}

- (void) playVideo:(CDVInvokedUrlCommand*)command;
- (void) ensureDownloaded:(CDVInvokedUrlCommand*)command;

@end
