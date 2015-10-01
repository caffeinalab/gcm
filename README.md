# Caffeina GCM
### Google Cloud Messaging support for Titanium - Parse format compatible

We have maintaned the same syntax of `Ti.Network` for iOS notifications, hope you like this choice :)

The behaviour is the same of iOS:

* if the app is in **background**, the standard OS view for incoming notifications is shown. If you click on that banner, the callback is invoked with the notification payload + the property `inBackground = true`.
* if the app is in **foreground**, nothing is shown and you have to handle manually in the app.

#### Register for Push notifications

```js
var gcm = require('it.caffeina.gcm');
gcm.registerForPushNotifications({

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
gcm.unregisterForPushNotifications();
```

#### Set the badge

*Due system limitations, currently the badge over the icon is supported only on Samsung and Sony devices. This is why there's no an "Android official method" to draw that badge, but only via private API.*

```js
gcm.setAppBadge(2);
```

#### Setting the icon

The module sets the notification tray icon taking it from `/platform/android/res/drawable-*`.

It should be flat (no gradients), white and face-on perspective. You have to generate the icon with all resolutions.

```
22 × 22 area in 24 × 24 (mdpi)
33 × 33 area in 36 × 36 (hdpi)
44 × 44 area in 48 × 48 (xhdpi)
66 × 66 area in 72 × 72 (xxhdpi)
88 × 88 area in 96 × 96 (xxxhdpi)
```

You can use this script to generate it once you put the icon in `drawable-xxxhdpi/notificationicon.png`

```sh
convert drawable-xxxhdpi/notificationicon.png -resize 72x72 drawable-xxhdpi/notificationicon.png
convert drawable-xxxhdpi/notificationicon.png -resize 48x48 drawable-xhdpi/notificationicon.png
convert drawable-xxxhdpi/notificationicon.png -resize 36x36 drawable-hdpi/notificationicon.png
convert drawable-xxxhdpi/notificationicon.png -resize 24x24 drawable-mdpi/notificationicon.png
```

**If you don't set an icon, no notification is shown.**

#### Send the notification from your server

The payload of the notification JSON object `data` should contain:

* `alert`: The message to show in the notification center and in the status bar.

Option values:

* `title`: The title to show in the notification center. Default to app title.
* `sound`: A sound relative to the drawable or `default`. Default is no sound.
* `priority`: A integer from `-2` to `2` indicating the priority. If you set values greater than 0, an *heads up* notification is shown. Default is `0`.
* `vibrate`: A boolean (`true` or `1`) value indicating if the phone should vibrate. Default is `false`.
* `badge`: An integer value for the badge. The icon on the launchscreen will display this number on the right-top corner. Default is no badge.
* `largeicon`: A URL represting a large icon to show. Default is null.

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

#### Send the notification from Parse console

Simply configure the GCM sender id in the Parse app settings, and send the notification.

#### License

*The code is inspired from https://github.com/morinel/gcmpush and massively edited for our purposes.*
