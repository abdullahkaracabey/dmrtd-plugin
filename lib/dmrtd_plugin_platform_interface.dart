import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'dmrtd_plugin_method_channel.dart';

abstract class DmrtdPluginPlatform extends PlatformInterface {
  /// Constructs a DmrtdPluginPlatform.
  DmrtdPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static DmrtdPluginPlatform _instance = MethodChannelDmrtdPlugin();

  /// The default instance of [DmrtdPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelDmrtdPlugin].
  static DmrtdPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [DmrtdPluginPlatform] when
  /// they register themselves.
  static set instance(DmrtdPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
