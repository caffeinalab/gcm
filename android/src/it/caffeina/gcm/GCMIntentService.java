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
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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

		String dataAsString = intent.getStringExtra("data");
		if (dataAsString == null) {
			Log.e(LCAT, "No data found in the payload");
			return;
		}

		JsonObject data = null;

		try {
			data = (JsonObject) new Gson().fromJson(dataAsString, JsonObject.class);
		} catch (Exception ex) {
			Log.e(LCAT, "No valid payload for this notifications");
			return;
		}

		/////////////////////////
		// Badge on the splash //
		/////////////////////////

		int badge = 0;
		try {
			if (data.has("badge")) {
				badge = data.getAsJsonPrimitive("badge").getAsInt();
				BadgeUtils.setBadge(context, badge);
			}
		} catch (Exception ex) {
			Log.e(LCAT, ex.getMessage());
		}

		////////////////////////////////////////////////////
		// Get the alert property and define the behavior //
		////////////////////////////////////////////////////

		Boolean appIsInForeground = TiApplication.isCurrentActivityInForeground();

		if (appIsInForeground) {
			Log.d(LCAT, "Message received but the app is on foreground, so you have to handle this in the app.");
		} else if (data.has("alert") == false) {
			Log.d(LCAT, "Message received but alert is empty.");
		} else {

			String pkg = instance.getApplicationContext().getPackageName();

			Intent launcherIntent = instance.getApplicationContext().getPackageManager().getLaunchIntentForPackage(pkg);
			launcherIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			launcherIntent.putExtra("notification", dataAsString);

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launcherIntent, PendingIntent.FLAG_ONE_SHOT);

			/////////////////////////////////
			// Start building notification //
			/////////////////////////////////

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			int builder_defaults = 0;
			builder.setContentIntent(contentIntent);
			builder.setAutoCancel(true);

			///////////
			// Alert //
			///////////

			String alert = data.getAsJsonPrimitive("alert").getAsString();
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

			if (data.has("largeicon")) {
				try {
					builder.setLargeIcon( getBitmapFromURL(data.getAsJsonPrimitive("largeicon").getAsString()) );
				} catch (Exception ex) {
					Log.e(LCAT, ex.getMessage());
				}
			}

			//////////////
			// Priority //
			//////////////

			int priority = 0;
			try {
				if (data.has("priority")) {
					priority = data.getAsJsonPrimitive("priority").getAsInt();
				}
			} catch (Exception ex) {
				Log.e(LCAT, ex.getMessage());
			}

			builder.setPriority(priority);

			///////////
			// Title //
			///////////

			builder.setContentTitle( data.has("title") ? data.getAsJsonPrimitive("title").getAsString() : instance.getAppInfo().getName() );

			///////////
			// Badge //
			///////////

			if (badge != 0) {
				builder.setNumber(badge);
			}

			///////////
			// Sound //
			///////////

			if (data.has("sound")) {
				if ("default".equals(data.getAsJsonPrimitive("sound").getAsString())) {
					builder_defaults |= Notification.DEFAULT_SOUND;
				} else {
					builder.setSound( Uri.parse("android.resource://" + pkg + "/" + getResource("raw", data.getAsJsonPrimitive("sound").getAsString())) );
				}
			}

			///////////////
			// Vibration //
			///////////////

			try {
				if (data.has("vibrate")) {
					JsonPrimitive vibrate = data.getAsJsonPrimitive("vibrate");
					if ( (vibrate.isBoolean() && vibrate.getAsBoolean() == true) || (vibrate.getAsInt() == 1) ) {
						builder_defaults |= Notification.DEFAULT_VIBRATE;
					}
				}
			} catch(Exception ex) {
				Log.e(LCAT, ex.getMessage());
			}

			///////////
			// Build //
			///////////

			builder_defaults |= Notification.DEFAULT_LIGHTS;
			builder.setDefaults(builder_defaults);

			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, builder.build());
		}

		if (CaffeinaGCMModule.getInstance() != null) {
			CaffeinaGCMModule.getInstance().sendMessage(dataAsString, !appIsInForeground);
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
