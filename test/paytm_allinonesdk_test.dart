import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
// import 'package:allinonesdk/allinonesdk.dart';

void main() {
  const MethodChannel channel = MethodChannel('allinonesdk');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  // test('getPlatformVersion', () async {
  //   expect(await Allinonesdk.platformVersion, '42');
  // });
}
