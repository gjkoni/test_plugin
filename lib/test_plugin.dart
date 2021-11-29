import 'dart:async';
import 'dart:ffi';

import 'package:flutter/services.dart';

class TestPlugin {
  static const MethodChannel _channel = MethodChannel('test_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static void startRecognize() {
    _channel.invokeMethod('startRecognize');
    return;
  }

  static Future<String?> stopRecognize() async {
    final String? msg = await _channel.invokeMethod('stopRecognize');
    return msg;
  }
}
