package com.heliasar.tools;

import com.heliasar.noteandgolib.Preferences;
import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.ui.MainActivity;
import com.heliasar.tools.TrackStopApi.Response;

import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;

public class TrackTrials {

	public static void isTrialRunning(MainActivity context, Preferences prefs) {
		if (Utils.isAppPaid(prefs)) return;
		if (!prefs.isOnline()) return;
		
		@SuppressWarnings("unused")
		class RequestThread implements Runnable {
			MainActivity context;

			@Override
			public void run() {
				final String deviceId = TrackTrials.getDeviceId(context);
				final String url = TrackStopApi.API_TRACKING + "?action=" + TrackStopApi.encode("start_trial", true) + "&deviceid=" + TrackStopApi.encode(deviceId);
				Response response = TrackStopApi.get(url);
				Utils.l(url);
				
				if (response.statusCode == 200) {
					try {
						JSONObject json = new JSONObject(TrackStopApi.decode(response.resp));
						boolean valid = json.getBoolean("validTrial");
						if (valid) {
							context.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Utils.l("App in trial period");
									context.trialValid();
								}
							});
						}
						else {
							context.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Utils.l("Trial period expired");
									context.trialExpired();
								}
							});
						}
						
					} catch (JSONException e) {
						Utils.l(url);
						Utils.l(response.resp);
						e.printStackTrace();
					}
					
				} else {
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							context.trialValid();
						}
					});
				}
			}
		}
		
		RequestThread task = new RequestThread();
		task.context = context;
		
		new Thread(task).start();
	}
	
	private static String getDeviceId(FragmentActivity context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}
	
	public static void redeemCode(String code, MainActivity context) {
		if (!context.prefs.isOnline()) return;
		class RequestThread implements Runnable {
			MainActivity context;
			String code;

			@Override
			public void run() {
				final String deviceId = TrackTrials.getDeviceId(context);
				final String url = TrackStopApi.API_TRACKING + "?action=" + 
						TrackStopApi.encode("redeem", true) + "&deviceid=" + 
						TrackStopApi.encode(deviceId) + "&code=" + code;
				Response response = TrackStopApi.get(url);
				Utils.l(url);
				
				if (response.statusCode == 200) {
					try {
						JSONObject json = new JSONObject(TrackStopApi.decode(response.resp));
						boolean valid = json.getBoolean("validCode");
						if (valid) {
							context.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Utils.l("Promo code valid");
									context.setAppPaid();
								}
							});
						}
						else {
							context.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Utils.l("Redeem code invalid");
									context.showInfoDialog(context.getString(R.string.redeem_code_failed));
								}
							});
						}
						
					} catch (JSONException e) {
						Utils.l(url);
						Utils.l(response.resp);
						e.printStackTrace();
					}
					
				} else {
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							context.showInfoDialog(context.getString(R.string.server_error));
						}
					});
				}
			}
		}
		
		RequestThread task = new RequestThread();
		task.code = code;
		task.context = context;
		
		new Thread(task).start();
	}
	
	public static void checkCouponRedeemed(MainActivity context) {
		if (!context.prefs.isOnline()) return;
		class RequestThread implements Runnable {
			MainActivity context;

			@Override
			public void run() {
				final String deviceId = TrackTrials.getDeviceId(context);
				final String url = TrackStopApi.API_TRACKING + "?action=" + 
						TrackStopApi.encode("check_redeem", true) + "&deviceid=" + 
						TrackStopApi.encode(deviceId);
				Response response = TrackStopApi.get(url);
				Utils.l(url);
				
				if (response.statusCode == 200) {
					try {
						JSONObject json = new JSONObject(TrackStopApi.decode(response.resp));
						boolean valid = json.getBoolean("validCode");
						if (valid) {
							context.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Utils.l("Promo code valid");
									context.setAppPaid();
								}
							});
						}
					} catch (JSONException e) {
						Utils.l(url);
						Utils.l(response.resp);
						e.printStackTrace();
					}
				}
			}
		}
		
		RequestThread task = new RequestThread();
		task.context = context;
		
		new Thread(task).start();
	}
	
}
