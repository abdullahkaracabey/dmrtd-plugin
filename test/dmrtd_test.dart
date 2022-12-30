// import 'package:flutter_test/flutter_test.dart';
// import 'package:dmrtd_plugin/dmrtd.dart';
// import 'package:dmrtd_plugin/dmrtd_platform_interface.dart';
// import 'package:dmrtd_plugin/dmrtd_method_channel.dart';
// import 'package:plugin_platform_interface/plugin_platform_interface.dart';

// class MockDmrtdPlatform
//     with MockPlatformInterfaceMixin
//     implements DmrtdPlatform {

//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }

// void main() {
//   final DmrtdPlatform initialPlatform = DmrtdPlatform.instance;

//   test('$MethodChannelDmrtd is the default instance', () {
//     expect(initialPlatform, isInstanceOf<MethodChannelDmrtd>());
//   });

//   test('getPlatformVersion', () async {
//     Dmrtd dmrtdPlugin = Dmrtd();
//     MockDmrtdPlatform fakePlatform = MockDmrtdPlatform();
//     DmrtdPlatform.instance = fakePlatform;

//     expect(await dmrtdPlugin.getPlatformVersion(), '42');
//   });
// }
