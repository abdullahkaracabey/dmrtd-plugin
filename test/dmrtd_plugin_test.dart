import 'package:flutter_test/flutter_test.dart';
import 'package:dmrtd_plugin/dmrtd_plugin.dart';
import 'package:dmrtd_plugin/dmrtd_plugin_platform_interface.dart';
import 'package:dmrtd_plugin/dmrtd_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockDmrtdPluginPlatform
    with MockPlatformInterfaceMixin
    implements DmrtdPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final DmrtdPluginPlatform initialPlatform = DmrtdPluginPlatform.instance;

  test('$MethodChannelDmrtdPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelDmrtdPlugin>());
  });

  test('getPlatformVersion', () async {
    DmrtdPlugin dmrtdPlugin = DmrtdPlugin();
    MockDmrtdPluginPlatform fakePlatform = MockDmrtdPluginPlatform();
    DmrtdPluginPlatform.instance = fakePlatform;

    expect(await dmrtdPlugin.getPlatformVersion(), '42');
  });
}
