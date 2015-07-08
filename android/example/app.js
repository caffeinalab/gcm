require('it.caffeina.gcm').registerForPushNotifications({
	senderId: 'XXXXXXXXXXXX',
	callback: onNotificationReceived,
	success: function(e) {
		console.log(e);
	},
	error: function(err) {
		Ti.API.error('Notifications: Retrieve device token failed', err);
	}
});