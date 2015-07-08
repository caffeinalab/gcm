# CaffeinaGCM

### Google Cloud Messaging support

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


* The code is inspired from https://github.com/morinel/gcmpush and edited for our purposes *