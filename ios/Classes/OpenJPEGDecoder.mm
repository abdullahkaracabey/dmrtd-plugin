//
//  OpenJPEGDecoder.mm
//  dmrtd_plugin
//
//  JPEG 2000 decoder implementation using OpenJPEG
//

#import "OpenJPEGDecoder.h"
#import "openjpeg/openjpeg.h"
#import <Foundation/Foundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <ImageIO/ImageIO.h>
#import <string.h>

// UIImage forward declaration for iOS
#ifdef __OBJC__
#import <UIKit/UIImage.h>
#endif

@implementation OpenJPEGDecoder

+ (BOOL)isJPEG2000Codestream:(NSData *)data {
    if (data.length < 4) {
        return NO;
    }
    
    const uint8_t *bytes = (const uint8_t *)data.bytes;
    // JPEG 2000 codestream signature: FF 4F FF 51
    return bytes[0] == 0xFF && bytes[1] == 0x4F && bytes[2] == 0xFF && bytes[3] == 0x51;
}

// Memory stream user data structure
typedef struct {
    OPJ_UINT8 *data;
    OPJ_SIZE_T size;
    OPJ_SIZE_T offset;
} MemoryStreamData;

// Memory stream callbacks
static OPJ_SIZE_T memoryStreamRead(void *buffer, OPJ_SIZE_T bytes, void *userData) {
    MemoryStreamData *streamData = (MemoryStreamData *)userData;
    OPJ_SIZE_T bytesRemaining = streamData->size - streamData->offset;
    OPJ_SIZE_T bytesToRead = bytes < bytesRemaining ? bytes : bytesRemaining;
    
    if (bytesToRead > 0) {
        memcpy(buffer, streamData->data + streamData->offset, bytesToRead);
        streamData->offset += bytesToRead;
    }
    
    return bytesToRead;
}

static OPJ_SIZE_T memoryStreamWrite(void *buffer, OPJ_SIZE_T bytes, void *userData) {
    // Not supported for read-only stream
    return 0;
}

static OPJ_OFF_T memoryStreamSkip(OPJ_OFF_T bytes, void *userData) {
    MemoryStreamData *streamData = (MemoryStreamData *)userData;
    OPJ_SIZE_T bytesRemaining = streamData->size - streamData->offset;
    OPJ_OFF_T bytesToSkip = (OPJ_OFF_T)bytes < (OPJ_OFF_T)bytesRemaining ? bytes : (OPJ_OFF_T)bytesRemaining;
    
    streamData->offset += bytesToSkip;
    return bytesToSkip;
}

static OPJ_BOOL memoryStreamSeek(OPJ_OFF_T bytes, void *userData) {
    MemoryStreamData *streamData = (MemoryStreamData *)userData;
    
    if (bytes < 0 || (OPJ_SIZE_T)bytes > streamData->size) {
        return OPJ_FALSE;
    }
    
    streamData->offset = (OPJ_SIZE_T)bytes;
    return OPJ_TRUE;
}

static void memoryStreamFree(void *userData) {
    if (userData) {
        free(userData);
    }
}

+ (nullable UIImage *)decodeJPEG2000Codestream:(NSData *)data {
    if (![self isJPEG2000Codestream:data]) {
        NSLog(@"[OpenJPEG] Invalid JPEG 2000 codestream signature");
        return nil;
    }
    
    NSLog(@"[OpenJPEG] Starting decode of %lu bytes", (unsigned long)data.length);
    
    // Setup OpenJPEG decoder
    opj_codec_t *codec = opj_create_decompress(OPJ_CODEC_J2K);
    if (!codec) {
        NSLog(@"[OpenJPEG] Failed to create decoder");
        return nil;
    }
    
    // Set decoder parameters
    opj_dparameters_t parameters;
    opj_set_default_decoder_parameters(&parameters);
    
    if (!opj_setup_decoder(codec, &parameters)) {
        NSLog(@"[OpenJPEG] Failed to setup decoder");
        opj_destroy_codec(codec);
        return nil;
    }
    
    // Create memory stream
    MemoryStreamData *streamData = (MemoryStreamData *)malloc(sizeof(MemoryStreamData));
    streamData->data = (OPJ_UINT8 *)data.bytes;
    streamData->size = data.length;
    streamData->offset = 0;
    
    opj_stream_t *stream = opj_stream_create(OPJ_J2K_STREAM_CHUNK_SIZE, OPJ_TRUE);
    if (!stream) {
        NSLog(@"[OpenJPEG] Failed to create stream");
        free(streamData);
        opj_destroy_codec(codec);
        return nil;
    }
    
    opj_stream_set_read_function(stream, memoryStreamRead);
    opj_stream_set_write_function(stream, memoryStreamWrite);
    opj_stream_set_skip_function(stream, memoryStreamSkip);
    opj_stream_set_seek_function(stream, memoryStreamSeek);
    opj_stream_set_user_data(stream, streamData, memoryStreamFree);
    opj_stream_set_user_data_length(stream, data.length);
    
    // Read header
    opj_image_t *image = NULL;
    if (!opj_read_header(stream, codec, &image)) {
        NSLog(@"[OpenJPEG] Failed to read header");
        opj_stream_destroy(stream);
        opj_destroy_codec(codec);
        return nil;
    }
    
    // Decode image
    if (!opj_decode(codec, stream, image)) {
        NSLog(@"[OpenJPEG] Failed to decode image");
        opj_image_destroy(image);
        opj_stream_destroy(stream);
        opj_destroy_codec(codec);
        return nil;
    }
    
    NSLog(@"[OpenJPEG] Decoded: %dx%d, components: %d, precision: %d",
          image->x1 - image->x0,
          image->y1 - image->y0,
          image->numcomps,
          image->comps[0].prec);
    
    // Convert OpenJPEG image to UIImage
    UIImage *uiImage = [self convertOpenJPEGImageToUIImage:image];
    
    // Cleanup
    opj_image_destroy(image);
    opj_stream_destroy(stream);
    opj_destroy_codec(codec);
    
    if (uiImage) {
        NSLog(@"[OpenJPEG] Successfully created UIImage: %.0fx%.0f", uiImage.size.width, uiImage.size.height);
    } else {
        NSLog(@"[OpenJPEG] Failed to convert to UIImage");
    }
    
    return uiImage;
}

+ (nullable UIImage *)convertOpenJPEGImageToUIImage:(opj_image_t *)image {
    if (!image || image->numcomps == 0) {
        return nil;
    }
    
    int width = image->x1 - image->x0;
    int height = image->y1 - image->y0;
    
    NSLog(@"[OpenJPEG] Converting %dx%d image with %d components", width, height, image->numcomps);
    
    // Create bitmap context
    size_t bitsPerComponent = 8;
    size_t bytesPerRow;
    CGColorSpaceRef colorSpace;
    CGBitmapInfo bitmapInfo;
    
    if (image->numcomps == 1) {
        // Grayscale
        colorSpace = CGColorSpaceCreateDeviceGray();
        bytesPerRow = width;
        bitmapInfo = kCGImageAlphaNone;
    } else if (image->numcomps >= 3) {
        // RGB or RGBA
        colorSpace = CGColorSpaceCreateDeviceRGB();
        bytesPerRow = width * 4;
        bitmapInfo = kCGImageAlphaPremultipliedLast | kCGBitmapByteOrder32Big;
    } else {
        NSLog(@"[OpenJPEG] Unsupported number of components: %d", image->numcomps);
        return nil;
    }
    
    CGContextRef context = CGBitmapContextCreate(NULL,
                                                  width,
                                                  height,
                                                  bitsPerComponent,
                                                  bytesPerRow,
                                                  colorSpace,
                                                  bitmapInfo);
    CGColorSpaceRelease(colorSpace);
    
    if (!context) {
        NSLog(@"[OpenJPEG] Failed to create bitmap context");
        return nil;
    }
    
    uint8_t *pixelData = (uint8_t *)CGBitmapContextGetData(context);
    
    // Copy pixel data
    if (image->numcomps == 1) {
        // Grayscale
        opj_image_comp_t *comp = &image->comps[0];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int value = comp->data[index];
                
                // Adjust for bit depth
                if (comp->prec < 8) {
                    value = value << (8 - comp->prec);
                } else if (comp->prec > 8) {
                    value = value >> (comp->prec - 8);
                }
                
                // Clamp value
                value = MIN(255, MAX(0, value));
                pixelData[index] = (uint8_t)value;
            }
        }
    } else {
        // RGB or RGBA
        opj_image_comp_t *rComp = &image->comps[0];
        opj_image_comp_t *gComp = &image->comps[1];
        opj_image_comp_t *bComp = &image->comps[2];
        opj_image_comp_t *aComp = image->numcomps > 3 ? &image->comps[3] : NULL;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcIndex = y * width + x;
                int dstIndex = srcIndex * 4;
                
                // Get RGB values and adjust for bit depth
                int r = rComp->data[srcIndex];
                int g = gComp->data[srcIndex];
                int b = bComp->data[srcIndex];
                int a = aComp ? aComp->data[srcIndex] : 255;
                
                if (rComp->prec < 8) {
                    r = r << (8 - rComp->prec);
                    g = g << (8 - gComp->prec);
                    b = b << (8 - bComp->prec);
                    if (aComp && aComp->prec < 8) a = a << (8 - aComp->prec);
                } else if (rComp->prec > 8) {
                    r = r >> (rComp->prec - 8);
                    g = g >> (gComp->prec - 8);
                    b = b >> (bComp->prec - 8);
                    if (aComp && aComp->prec > 8) a = a >> (aComp->prec - 8);
                }
                
                // Clamp values
                pixelData[dstIndex + 0] = (uint8_t)MIN(255, MAX(0, r));
                pixelData[dstIndex + 1] = (uint8_t)MIN(255, MAX(0, g));
                pixelData[dstIndex + 2] = (uint8_t)MIN(255, MAX(0, b));
                pixelData[dstIndex + 3] = (uint8_t)MIN(255, MAX(0, a));
            }
        }
    }
    
    // Create CGImage
    CGImageRef cgImage = CGBitmapContextCreateImage(context);
    CGContextRelease(context);
    
    if (!cgImage) {
        NSLog(@"[OpenJPEG] Failed to create CGImage");
        return nil;
    }
    
    UIImage *uiImage = [UIImage imageWithCGImage:cgImage];
    CGImageRelease(cgImage);
    
    return uiImage;
}

@end
