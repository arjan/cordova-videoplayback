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
#import "MediaPlayer/MPMoviePlayerViewController.h"
#import "MediaPlayer/MPMoviePlayerController.h"
#import "MovieViewController.h"
#import <Cordova/CDV.h>

@implementation VideoPlayback
- (void) playVideo:(CDVInvokedUrlCommand*)command
{
  movie = [command.arguments objectAtIndex:0];
  NSString *orient = [command.arguments objectAtIndex:1];
  NSRange range = [movie rangeOfString:@"http"];
  if(range.length > 0) {
    if ([@"YES" isEqualToString:orient]) {
      player = [[MovieViewController alloc] initWithContentURL:[NSURL URLWithString:movie] andOrientation:YES];
    } else {
      player = [[MovieViewController alloc] initWithContentURL:[NSURL URLWithString:movie] andOrientation:NO];
    }
    
  } else {
    NSArray *fileNameArr = [movie componentsSeparatedByString:@"."];
    NSString *prefix = [fileNameArr objectAtIndex:0];
    NSString *suffix = [fileNameArr objectAtIndex:1];
    NSString *soundFilePath = [[NSBundle mainBundle] pathForResource:prefix ofType:suffix];
    NSURL *fileURL = [NSURL fileURLWithPath:soundFilePath];
    //NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    //NSString *soundFilePath = [paths objectAtIndex:0];
    //NSURL *fileURL = [NSURL fileURLWithPath:[soundFilePath stringByAppendingPathComponent:movie]];
    if ([@"YES" isEqualToString:orient]) {
      player = [[MovieViewController alloc] initWithContentURL:fileURL andOrientation:YES];
    } else {
      player = [[MovieViewController alloc] initWithContentURL:fileURL andOrientation:NO];
    }
  }
  if (player) {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(MovieDidFinish:) name:MPMoviePlayerPlaybackDidFinishNotification object:nil];
    [self.viewController presentMoviePlayerViewControllerAnimated:player];
  }
}

- (void) ensureDownloaded:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Ensure downloaded, FIXME");
}

- (void)MovieDidFinish:(NSNotification *)notification {
  [player.moviePlayer stop];
  player.moviePlayer.initialPlaybackTime = -1.0;
  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:MPMoviePlayerPlaybackDidFinishNotification
                                                object:nil];
  [self writeJavascript:[NSString stringWithFormat:@"window.plugins.CDVVideo.finished(\"%@\");", movie]];
  
}

- (void)dealloc {
  //[player release];
  //[movie release];
  //[super dealloc];
}

@end
