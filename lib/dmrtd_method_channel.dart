import 'package:dmrtd_plugin/document_read_exception.dart';
import 'package:dmrtd_plugin/models/document..dart';
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
    debugPrint("MethodChannelDmrtd.read called");
    if (isOnWork) {
      throw DocumentReadException(code: "already-on-read", message: "");
    }

    methodChannel.setMethodCallHandler((MethodCall call) async {
      final args = call.arguments as String;

      onStatusChange(args);
    });

    try {
      debugPrint("MethodChannelDmrtd.read started to read");
      final result = await methodChannel.invokeMethod<dynamic>('read', mrzData);

      debugPrint("MethodChannelDmrtd.read document reading is done");

      if (result != null) {
        final error = result["error"];

        if (error != null) {
          debugPrint(
              "MethodChannelDmrtd.read document error $error ${error.runtimeType}");

          if (error is String) {
            if (error.contains("Tag connection lost")) {
              throw DocumentReadException(code: "tag-lost", message: "");
            }
          }

          throw DocumentReadException(code: "unknown-error", message: "");
        }
        return Document.fromJson(Map<String, dynamic>.from(result));
      } else {
        throw DocumentReadException(
            code: "document-mapping-error", message: "");
      }
    } catch (e) {
      debugPrint("MethodChannelDmrtd.read document reading is not completed");
      if (e is DocumentReadException) {
        debugPrint("MethodChannelDmrtd.read error ${e.code} ${e.message}");
        rethrow;
      }
      if (e is PlatformException) {
        debugPrint("MethodChannelDmrtd.read error ${e.code} ${e.message}");
        throw DocumentReadException(code: e.code, message: e.message ?? "");
      }
      debugPrint("MethodChannelDmrtd.read error unknown-error ${e.toString()}");
      throw DocumentReadException(code: "unknown-error", message: "");
    }
  }
}
