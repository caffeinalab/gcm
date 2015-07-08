# CaffeinaGCM

### Google Cloud Messaging support

**The code is inspired from https://github.com/morinel/gcmpush and edited for our purposes**

#### Register for Push notifications

```js
require('it.caffeina.gcm').registerForPushNotifications({
	senderId: 'XXXXXXXXXXXX',
	callback: function(e) {

		// Here you have the payload of the upcoming notification
		// Handle how you want.

	},
	success: function(e) {

		// Send the e.deviceToken variable to your PUSH server
		Ti.API.log('Notifications: device token is ' + e.deviceToken);

	},
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