package it.caffeina.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiRHelper;
import java.util.HashMap;
import java.util.Map;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String LCAT = "it.caffeina.gcm.GCMIntentService";

	public GCMIntentService() {
		super("");
	}

	@Override
	public void onRegistered(Context context, String registrationId) {
		CaffeinaGCMModule.getInstance().sendSuccess(registrationId);
	}

	@Override
	public void onUnregistered(Context context, String registrationId) {
		CaffeinaGCMModule.getInstance().fireEvent("unregister", new HashMap<String, Object>());
	}

	private int getResource(String type, String name) {
		int icon = 0;
		if (name != null) {

			int index = name.lastIndexOf(".");
			if (index > 0) name = name.substring(0, index);

			try {
				icon = TiRHelper.getApplicationResource(type + "." + name);
			} catch (TiRHelper.ResourceNotFoundException ex) {
				Log.e(LCAT, type + "." + name + " not found; make sure it's in platform/android/res/" + type);
			}
		}

		return icon;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onMessage(Context context, Intent intent) {
		Log.d(LCAT, "Push notification received");
		TiApplication instance = TiApplication.getInstance();

		///////////////////////////////////
		// Build the object notification //
		///////////////////////////////////

		String _data = intent.getExtras().getString("data");
		HashMap data = null;

		try {
			data = new Gson().fromJson(_data, HashMap.class);
		} catch (Exception ex) {
			Log.e(LCAT, "No payload for this notifications, ignoring");
			return;
		}

		////////////////////////////////////////////////////
		// Get the alert property and define the behavior //
		////////////////////////////////////////////////////

		if (TiApplication.isCurrentActivityInForeground()) {
			Log.d(LCAT, "Message received but the app is on foreground, so you have to handle this in the app.");
		} else if ( ! data.containsKey("alert")) {
			Log.d(LCAT, "Message received but alert is empty.");
		} else {

			String alert = (String)data.get("alert");

			String pkg = instance.getApplicationContext().getPackageName();
			Intent launcherIntent = instance.getApplicationContext().getPackageManager().getLaunchIntentForPackage(pkg);
			launcherIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			launcherIntent.putExtra("notification", _data);

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launcherIntent, PendingIntent.FLAG_ONE_SHOT);

			int appIcon = getResource("drawable", "appicon.png");

			///////////
			// Badge //
			///////////

			int badge = data.containsKey("badge") ? Integer.parseInt((String)data.get("badge")) : 0;

			//////////////
			// Priority //
			//////////////

			int priority = data.containsKey("priority") ? Integer.parseInt((String)data.get("priority")) : 0;

			///////////
			// Title //
			///////////

			String title = instance.getAppInfo().getName();
			if (data.containsKey("title")) {
				title = (String)data.get("title");
			}

			///////////
			// Build //
			///////////

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			builder.setContentTitle(title);
			builder.setContentText(alert);
			builder.setTicker(alert);
			builder.setContentIntent(contentIntent);
			builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), appIcon));
			builder.setSmallIcon(appIcon);
			builder.setNumber(badge);
			builder.setAutoCancel(true);
			builder.setPriority(priority);

			Notification notification = builder.build();

			///////////
			// Sound //
			///////////

			if (data.containsKey("sound")) {
				String sound = (String)data.get("sound");
				if ("default".equals(sound)) {
					notification.defaults |= Notification.DEFAULT_SOUND;
				} else {
					notification.sound = Uri.parse("android.resource://" + pkg + "/" + getResource("raw", sound));
				}
			}


			///////////////
			// Vibration //
			///////////////

			if (data.containsKey("vibrate") && (Boolean)data.get("vibrate") == true) {
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}

			////////////
			// Lights //
			////////////

			notification.defaults |= Notification.DEFAULT_LIGHTS;


			///////////////////////////
			// Send the notification //
			///////////////////////////

			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notification);
		}

		if (CaffeinaGCMModule.getInstance() != null) {
			CaffeinaGCMModule.getInstance().sendMessage(_data, false);
		}
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.e(LCAT, "Error: " + errorId);

		if (CaffeinaGCMModule.getInstance() != null) {
			CaffeinaGCMModule.getInstance().sendError(errorId);
		}
	}

	@Override
	public boolean onRecoverableError(Context context, String errorId) {
		Log.e(LCAT, "RecoverableError: " + errorId);

		if (CaffeinaGCMModule.getInstance() != null) {
			CaffeinaGCMModule.getInstance().sendError(errorId);
		}

		return true;
	}
}
