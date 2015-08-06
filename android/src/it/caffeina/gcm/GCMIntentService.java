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
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiRHelper;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.BufferedInputStream;

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

	private Bitmap getBitmapFromURL(String src) throws Exception {
		HttpURLConnection connection = (HttpURLConnection)(new URL(src)).openConnection();
		connection.setDoInput(true);
		connection.setUseCaches(false); // Android BUG
		connection.connect();
		return BitmapFactory.decodeStream( new BufferedInputStream( connection.getInputStream() ) );
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onMessage(Context context, Intent intent) {
		Log.d(LCAT, "Push notification received");
		TiApplication instance = TiApplication.getInstance();

		String _data = intent.getStringExtra("data");
		if (_data == null) {
			Log.e(LCAT, "No data found in the payload");
			return;
		}

		Type type = new TypeToken< HashMap<String, String> >(){}.getType();
		HashMap<String, String> data = null;

		try {
			data = new Gson().fromJson(_data, type);
		} catch (Exception ex) {
			Log.e(LCAT, "No valid payload for this notifications");
			return;
		}

		/////////////////////////
		// Badge on the splash //
		/////////////////////////

		int badge = data.containsKey("badge") ? Integer.parseInt(data.get("badge")) : 0;
		if (data.containsKey("badge")) {
			if (badge == 0) {
				BadgeUtils.clearBadge(context);
			} else {
				BadgeUtils.setBadge(context, badge);
			}
		}

		////////////////////////////////////////////////////
		// Get the alert property and define the behavior //
		////////////////////////////////////////////////////

		if (TiApplication.isCurrentActivityInForeground() || instance == null) {
			Log.d(LCAT, "Message received but the app is on foreground, so you have to handle this in the app.");
		} else if ( ! data.containsKey("alert")) {
			Log.d(LCAT, "Message received but alert is empty.");
		} else {

			String pkg = instance.getApplicationContext().getPackageName();

			Intent launcherIntent = instance.getApplicationContext().getPackageManager().getLaunchIntentForPackage(pkg);
			launcherIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			launcherIntent.putExtra("notification", _data);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launcherIntent, PendingIntent.FLAG_ONE_SHOT);

			/////////////////////////////////
			// Start building notification //
			/////////////////////////////////

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			int builder_defaults = 0;
			builder.setContentIntent(contentIntent);

			///////////
			// Alert //
			///////////

			String alert = data.get("alert");
			builder.setContentText(alert);
			builder.setTicker(alert);

			///////////
			// Icons //
			///////////

			int smallIcon = getResource("drawable", "notificationicon");
			builder.setSmallIcon(smallIcon);


			////////////////
			// Large icon //
			////////////////

			if (data.containsKey("largeicon")) {
				try {
					builder.setLargeIcon( getBitmapFromURL(data.get("largeicon")) );
				} catch (Exception ex) {
					Log.e(LCAT, ex.getMessage());
				}
			}

			//////////////
			// Priority //
			//////////////

			int priority = data.containsKey("priority") ? Integer.parseInt(data.get("priority")) : 0;
			builder.setPriority(priority);

			///////////
			// Title //
			///////////

			builder.setContentTitle( data.containsKey("title") ? data.get("title") : instance.getAppInfo().getName() );

			///////////
			// Badge //
			///////////

			if (data.containsKey("badge")) {
				builder.setNumber(badge);
			}

			////////////////
			// Autocancel //
			////////////////

			if (data.containsKey("autocancel") && ( "0".equals(data.get("autocancel")) || "false".equals(data.get("autocancel")) )) {
				builder.setAutoCancel(false);
			} else {
				builder.setAutoCancel(true);
			}

			///////////
			// Sound //
			///////////

			if (data.containsKey("sound")) {
				if ("default".equals(data.get("sound"))) {
					builder_defaults |= Notification.DEFAULT_SOUND;
				} else {
					builder.setSound( Uri.parse("android.resource://" + pkg + "/" + getResource("raw", data.get("sound"))) );
				}
			}

			///////////////
			// Vibration //
			///////////////

			if (data.containsKey("vibrate") && ( "true".equals(data.get("vibrate")) || "1".equals(data.get("vibrate")) ) ) {
				builder_defaults |= Notification.DEFAULT_VIBRATE;
			}

			///////////
			// Build //
			///////////

			builder_defaults |= Notification.DEFAULT_LIGHTS;
			builder.setDefaults(builder_defaults);

			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, builder.build());
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
