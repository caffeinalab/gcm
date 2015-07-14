# CaffeinaGCM

### Google Cloud Messaging support

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

The payload of the notification JSON object can contain:

* `title`: The title to show in the notification center.
* `alert`: The message to show in the notification center and in the status bar.
* `sound`: A sound relative to the drawable or `default`.
* `priority`: A integer from `-2` to `2` indicating the priority. If you set values greater than 0, an *heads up* notification is shown.
* `vibrate`: A boolean value indicating if the phone should vibrate.

#### Handle the notification on the app

The payload of the notifications is the same that comes from your server, with the addition of:

* `inBackground`: A boolean value indicating if the notification has come when the app was in background, and the user has explicited clicked on the banner.
