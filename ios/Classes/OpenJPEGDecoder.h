//
//  OpenJPEGDecoder.h
//  dmrtd_plugin
//
//  JPEG 2000 decoder using OpenJPEG library
//  Supports raw codestream format from passport chips
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface OpenJPEGDecoder : NSObject

/**
 * Decode JPEG 2000 raw codestream to UIImage
 * @param data Raw JPEG 2000 codestream data (starting with FF 4F FF 51)
 * @return Decoded UIImage or nil if decoding fails
 */
+ (nullable UIImage *)decodeJPEG2000Codestream:(NSData *)data;

/**
 * Check if data is a valid JPEG 2000 codestream
 * @param data Data to check
 * @return YES if data starts with JPEG 2000 codestream signature (FF 4F FF 51)
 */
+ (BOOL)isJPEG2000Codestream:(NSData *)data;

@end

NS_ASSUME_NONNULL_END
