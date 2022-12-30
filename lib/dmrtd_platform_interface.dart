import 'package:dmrtd_plugin/models/document..dart';
import 'package:dmrtd_plugin/result.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'dmrtd_method_channel.dart';

abstract class DmrtdPlatform extends PlatformInterface {
  /// Constructs a DmrtdPlatform.
  DmrtdPlatform() : super(token: _token);

  static final Object _token = Object();

  static DmrtdPlatform _instance = MethodChannelDmrtd();

  /// The default instance of [DmrtdPlatform] to use.
  ///
  /// Defaults to [MethodChannelDmrtd].
  static DmrtdPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [DmrtdPlatform] when
  /// they register themselves.
  static set instance(DmrtdPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<Document> read(String mrzData, Function(String) onStatusChange);
}
