import 'package:dmrtd_plugin/document_read_exception.dart';
import 'package:dmrtd_plugin/models/document..dart';
import 'package:dmrtd_plugin/result.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'dmrtd_platform_interface.dart';

/// An implementation of [DmrtdPlatform] that uses method channels.
class MethodChannelDmrtd extends DmrtdPlatform {
  /// The method channel used to interact with the native platform.
  final methodChannel = const MethodChannel('dmrtd_plugin');

  bool isOnWork = false;

  // MethodChannelDmrtd() {
  //   methodChannel.setMethodCallHandler(_receiverFromHost);
  // }

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<Document> read(String mrzData, Function(String) onStatusChange) async {
    if (isOnWork) DocumentReadException(code: "already-on-read", message: "");

    methodChannel.setMethodCallHandler((MethodCall call) async {
      final args = call.arguments as String;

      onStatusChange(args);
    });

    try {
      final result = await methodChannel.invokeMethod<dynamic>('read', mrzData);

      if (result != null) {
        final error = result["error"];

        if (error != null) {
          throw DocumentReadException(code: "unknown-error", message: "");
        }
        return Document.fromJson(Map<String, dynamic>.from(result));
      } else {
        throw DocumentReadException(
            code: "document-mapping-error", message: "");
      }
    } catch (e) {
      if (e is DocumentReadException) rethrow;
      if (e is PlatformException)
        throw DocumentReadException(code: e.code, message: e.message ?? "");
      throw DocumentReadException(code: "unknown-error", message: "");
    }
  }
}
