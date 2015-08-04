# GCM: Google Cloud Messaging support

**The code is inspired from https://github.com/morinel/gcmpush and edited for our purposes.**

We have maintaned the same syntax of `Ti.Network` for iOS notifications, hope you like this choice :)

The behaviour is the same of iOS:

* if the app is in **background**, the standard OS view for incoming notifications is shown. If you click on that banner, the callback is invoked with the notification payload + the property `inBackground = true`.
* if the app is in **foreground**, nothing is shown and you have to handle manually in the app.

#### Register for Push notifications

```js
require('it.caffeina.gcm').registerForPushNotifications({

	// you take this from the Google Developer Console, is the project ID
	senderId: 'XXXXXXXXXXXX',

	// The callback to invoke when a notification arrives.
	callback: function(e) {
		// Here you have the payload `e` of the upcoming notification
		// Handle how you want.
	},

	// The callback invoked when you have the device token.
	success: function(e) {

		// Send the e.deviceToken variable to your PUSH server
		Ti.API.log('Notifications: device token is ' + e.deviceToken);

	},

	// The callback invoked on some errors.
	error: function(err) {
		Ti.API.error('Notifications: Retrieve device token failed', err);
	}
});
```

#### Unregister

```js
require('it.caffeina.gcm').unregisterForPushNotifications();
```

#### Send the notification from your server

The payload of the notification JSON object `data` can contain:

* `title`: The title to show in the notification center.
* `alert`: The message to show in the notification center and in the status bar.
* `sound`: A sound relative to the drawable or `default`.
* `priority`: A integer from `-2` to `2` indicating the priority. If you set values greater than 0, an *heads up* notification is shown.
* `vibrate`: A boolean value indicating if the phone should vibrate.

**Remember, all custom properties must be inside the `data` key**

##### A PHP Example

```php
<?php

$json = '{
  "registration_ids": ["DEVICETOKEN1", "DEVICETOKEN2"],
  "data": {
    "data":{
      "alert":"ALERT",
      "title": "My awesome app",
      "sound": "default",
      "priority": 2,
      "vibrate": true
    }
  }
}';

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://android.googleapis.com/gcm/send');
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [ 'Authorization: key=YOUR_GOOGLE_KEY', 'Content-Type: application/json' ]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4);
curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
echo curl_exec($ch);
curl_close($ch);
```

#### Handle the notification on the app

The payload of the notifications is the same that comes from your server, with the addition of:

* `inBackground`: A boolean value indicating if the notification has come when the app was in background, and the user has explicited clicked on the banner.
