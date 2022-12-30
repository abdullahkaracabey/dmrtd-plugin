import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dmrtd_plugin/dmrtd_method_channel.dart';

void main() {
  MethodChannelDmrtd platform = MethodChannelDmrtd();
  const MethodChannel channel = MethodChannel('dmrtd');

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
