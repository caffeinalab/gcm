require('it.caffeina.gcm').registerForPushNotifications({
	senderId: 'XXXXXXXXXXXX',
	callback: function(e) {

		// Here you have the payload

	},
	success: function(e) {
		console.log(e);
	},
	error: function(err) {
		Ti.API.error('Notifications: Retrieve device token failed', err);
	}
});