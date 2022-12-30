#import "DmrtdPlugin.h"
#if __has_include(<dmrtd/dmrtd-Swift.h>)
#import <dmrtd/dmrtd-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "dmrtd-Swift.h"
#endif

@implementation DmrtdPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftDmrtdPlugin registerWithRegistrar:registrar];
}
@end
