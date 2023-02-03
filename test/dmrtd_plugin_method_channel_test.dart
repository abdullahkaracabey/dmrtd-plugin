import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dmrtd_plugin/dmrtd_plugin_method_channel.dart';

void main() {
  MethodChannelDmrtdPlugin platform = MethodChannelDmrtdPlugin();
  const MethodChannel channel = MethodChannel('dmrtd_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
