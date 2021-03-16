import 'dart:async';

import 'package:flutter/services.dart';

class AllInOneSdk {
  static const MethodChannel _channel = const MethodChannel('allinonesdk');

  static Future<Map<dynamic, dynamic>?> startTransaction(
      String mid,
      String orderId,
      String amount,
      String txnToken,
      String callbackUrl,
      bool isStaging,
      bool restrictAppInvoke) async {
    var sendMap = <String, dynamic>{
      "mid": mid,
      "orderId": orderId,
      "amount": amount,
      "txnToken": txnToken,
      "callbackUrl": callbackUrl,
      "isStaging": isStaging,
      "restrictAppInvoke": restrictAppInvoke
    };
    Map<dynamic, dynamic>? version =
        await _channel.invokeMethod('startTransaction', sendMap);
    return version;
  }
}
