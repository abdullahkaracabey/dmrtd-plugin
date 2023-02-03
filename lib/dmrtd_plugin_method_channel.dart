import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'dmrtd_plugin_platform_interface.dart';

/// An implementation of [DmrtdPluginPlatform] that uses method channels.
class MethodChannelDmrtdPlugin extends DmrtdPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('dmrtd_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
