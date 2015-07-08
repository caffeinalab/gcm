package it.caffeina.gcm;

import android.app.Activity;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

@Kroll.module(name="CaffeinaGCM", id="it.caffeina.gcm")
public class CaffeinaGCMModule extends KrollModule
{

	// Standard Debugging variables
	private static final String LCAT = "CaffeinaGCMModule";
	private static final boolean DBG = TiConfig.LOGD;

	private static CaffeinaGCMModule instance = null;

	private KrollFunction successCallback = null;
	private KrollFunction errorCallback = null;
	private KrollFunction messageCallback = null;

	public static final String LAST_DATA = "it.caffeina.gcm.last_data";

	public CaffeinaGCMModule() {
		super();
		instance = this;
	}

	public static CaffeinaGCMModule getInstance() {
		return instance;
	}

	@Kroll.method
	@SuppressWarnings("unchecked")
	public void registerForPushNotifications(HashMap options) {
		Log.d(LCAT, "registerForPushNotifications called");

		String senderId = (String) options.get("senderId");
		successCallback = (KrollFunction) options.get("success");
		errorCallback = (KrollFunction) options.get("error");
		messageCallback = (KrollFunction) options.get("callback");

		if (senderId != null) {
			GCMRegistrar.register(TiApplication.getInstance(), senderId);

			String registrationId = getRegistrationId();
			if (registrationId != null && registrationId.length() > 0) {
				sendSuccess(registrationId);
			} else {
				Log.e(LCAT, "Error while gettings registrationId from GCM");
				sendError("Error while gettings registrationId from GCM");
			}

		} else {
			Log.e(LCAT, "No GCM senderId specified; get it from the Google Play Developer Console");
			sendError("No GCM senderId specified; get it from the Google Play Developer Console");
		}
	}

	@Kroll.method
	public void unregisterForPushNotifications() {
		Log.d(LCAT, "unregister called (" + (instance != null) + ")");
		GCMRegistrar.unregister(TiApplication.getInstance());
	}


	@Kroll.method
	@Kroll.getProperty
	public String getRegistrationId() {
		return GCMRegistrar.getRegistrationId(TiApplication.getInstance());
	}

	@Kroll.method
	@Kroll.getProperty
	@SuppressWarnings("unchecked")
	public KrollDict getLastData() {
		Map map = new Gson().fromJson(TiApplication.getInstance().getAppProperties().getString(LAST_DATA, null), Map.class);
		return map != null ? new KrollDict(map) : null;
	}

	@Kroll.method
	public void clearLastData() {
		TiApplication.getInstance().getAppProperties().removeProperty(LAST_DATA);
	}

	public void sendSuccess(String registrationId) {
		if (successCallback != null) {
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("registrationId", registrationId);
			data.put("deviceToken", registrationId);

			successCallback.callAsync(getKrollObject(), data);
		}
	}

	public void sendError(String error) {
		if (errorCallback != null) {
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("error", error);

			errorCallback.callAsync(getKrollObject(), data);
		}
	}

	public void sendMessage(HashMap<String, Object> messageData) {
		if (messageCallback != null) {
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("data", messageData);

			messageCallback.call(getKrollObject(), data);
		}
	}


	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		Log.d(LCAT, "onAppCreate " + app + " (" + (instance != null) + ")");
	}

	@Override
	protected void initActivity(Activity activity) {
		Log.d(LCAT, "initActivity " + activity + " (" + (instance != null) + ")");
		super.initActivity(activity);
	}

	@Override
	public void onResume(Activity activity) {
		Log.d(LCAT, "onResume " + activity + " (" + (instance != null) + ")");
		super.onResume(activity);
	}

	@Override
	public void onPause(Activity activity) {
		Log.d(LCAT, "onPause " + activity + " (" + (instance != null) + ")");
		super.onPause(activity);
	}

	@Override
	public void onDestroy(Activity activity) {
		Log.d(LCAT, "onDestroy " + activity + " (" + (instance != null) + ")");
		super.onDestroy(activity);
	}

	@Override
	public void onStart(Activity activity) {
		Log.d(LCAT, "onStart " + activity + " (" + (instance != null) + ")");
		super.onStart(activity);
	}

	@Override
	public void onStop(Activity activity) {
		Log.d(LCAT, "onStop " + activity + " (" + (instance != null) + ")");
		super.onStop(activity);
	}

}

