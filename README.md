# paytm_allinonesdk

## Use this package as a library

1. Depend on it

```
Add this to your package's pubspec.yaml file:

dependencies:
  paytm_allinonesdk: ^1.0.2
```

2. Install it

```
You can install packages from the command line:

with Flutter:

$ flutter pub get
Alternatively, your editor might support flutter pub get. Check the docs for your editor to learn more.
```

3. Import it

```
Now in your Dart code, you can use:

import 'package:paytm_allinonesdk/paytm_allinonesdk.dart';
```

4. Call transaction method

```
    try {
      var response = AllInOneSdk.startTransaction(
          mid, orderId, amount, txnToken, null, isStaging, restrictAppInvoke);
      response.then((value) {
        print(value);
        setState(() {
          result = value.toString();
        });
      }).catchError((onError) {
        if (onError is PlatformException) {
          setState(() {
            result = onError.message + " \n  " + onError.details.toString();
          });
        } else {
          setState(() {
            result = onError.toString();
          });
        }
      });
    } catch (err) {
      result = err.message;
    }
```
