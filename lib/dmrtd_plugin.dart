import 'package:dmrtd_plugin/models/document..dart';
import 'dmrtd_platform_interface.dart';

class DmrtdPlugin {
  Future<Document> read(String mrzData, Function(String) onStatusChange) {
    return DmrtdPlatform.instance.read(mrzData, onStatusChange);
  }
}
