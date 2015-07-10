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
import org.json.JSONObject;
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

		HashMap<String, Object> data = new HashMap<String, Object>();
		for (String key : intent.getExtras().keySet()) {
			String eventKey = key.startsWith("data.") ? key.substring(5) : key;
			data.put(eventKey, intent.getExtras().getString(key));
		}


		////////////////////////////////////////////////////
		// Get the alert property and define the behavior //
		////////////////////////////////////////////////////

		String alert = (String)data.get("alert");

		if (TiApplication.isCurrentActivityInForeground()) {
			Log.d(LCAT, "Message received but App is no foreground, so no alert");
		} else if (alert == null || alert.length() == 0) {
			Log.d(LCAT, "Message received but alert is empty");
		} else {

			String pkg = instance.getApplicationContext().getPackageName();
			Intent launcherIntent = instance.getApplicationContext().getPackageManager().getLaunchIntentForPackage(pkg);
			launcherIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			launcherIntent.putExtra("notification", (new JSONObject(data)).toString());

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launcherIntent, PendingIntent.FLAG_ONE_SHOT);

			int appIcon = getResource("drawable", "appicon.png");

			///////////
			// Badge //
			///////////

			int badge = 0;
			if (data.get("badge") != null) {
				badge = Integer.parseInt((String)data.get("badge"));
			}

			//////////////
			// Priority //
			//////////////

			int priority = 0;
			if (data.get("priority") != null) {
				priority = Integer.parseInt((String)data.get("priority"));
			}

			///////////
			// Title //
			///////////

			String title = instance.getAppInfo().getName();
			if (data.get("title") != null && ((String)data.get("title")).length() > 0) {
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

			String sound = (String)data.get("sound");
			if ("default".equals(sound)) {
				notification.defaults |= Notification.DEFAULT_SOUND;
			} else if (sound != null) {
				notification.sound = Uri.parse("android.resource://" + pkg + "/" + getResource("raw", sound));
			}

			///////////////
			// Vibration //
			///////////////

			if (data.get("vibrate") != null && Boolean.valueOf((String)data.get("vibrate"))) {
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
			CaffeinaGCMModule.getInstance().sendMessage(data);
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
