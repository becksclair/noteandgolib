package com.heliasar.noteandgolib;

import com.heliasar.noteandgolib.data.Storage;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class Preferences {

	private Context context;
	//private MainActivity parentActivity;
	public SharedPreferences prefs;

	public boolean trialValid;
	public int currentVersion;
	
	public String orderBy;
	public boolean linkifyEnabled;

	public boolean simpleNoteEnabled;
	public String simpleNoteUsername;
	public String simpleNotePassword;
	public boolean passwordChecked;

	public boolean isConnected;
	public boolean isMobile;

	public boolean debugMode;
	public boolean forceTablet;
	public boolean bypassOnlineChecks;
	
	public Context getContext() {
		return context;
	}
	
	public Preferences() {
	}
	
	public void initialize(AppController context/*, SherlockFragmentActivity activity*/) {
		this.context = context.getApplicationContext();
		/*if (activity instanceof MainActivity) {
			this.parentActivity = (MainActivity) activity;
		}*/

		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		trialValid = prefs.getBoolean("trialValid", true);
		
		orderBy = prefs.getString("order_by", "modify_date");
		linkifyEnabled = prefs.getBoolean("linkifyEnabled", false);

		passwordChecked = prefs.getBoolean("passwordchecked", false);
		simpleNoteEnabled = prefs.getBoolean("simpleNoteEnabled", false);
		simpleNoteUsername = prefs.getString("simpleNoteUsername", "");
		simpleNotePassword = prefs.getString("simpleNotePassword", "");

		forceTablet = prefs.getBoolean("forceTablet", false);
		bypassOnlineChecks = prefs.getBoolean("bypassOnlineChecks", false);
		debugMode = prefs.getBoolean("debugMode", false);
	}
	
	public boolean firstRun() {
		boolean ret = true;
		
		// Get App version
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			int versionCode = packageInfo.versionCode;
			
			currentVersion = prefs.getInt("currentVersion", 0);
			
			if (currentVersion > 0) {
				ret = false;
			}
			
			if (currentVersion == 0 || currentVersion < versionCode) {
				Storage.deleteCacheFile();
				
				prefs.edit().putInt("currentVersion", versionCode).commit();
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public boolean isSNenabled() {
		if (isOnline() && simpleNoteEnabled) {
			if ((simpleNoteUsername.length() > 0) && (simpleNotePassword.length() > 0))
				return true;
		}
		return false;
	}
	
	public boolean isOnline() {
		if (bypassOnlineChecks)
			return true;

		boolean ret = false;
		// If we are connected to internet, then dial home for sync
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNet = cm.getActiveNetworkInfo();
		if (activeNet == null) {
			ret = false;
			return ret;
		}

		isConnected = activeNet.isConnectedOrConnecting();
		isMobile = activeNet.getType() == ConnectivityManager.TYPE_MOBILE;

		return isConnected;
	}
	
	public void setTrialValid() {
		prefs.edit().putBoolean("trialValid", true).commit();
	}
	
	public void setTrialExpired() {
		prefs.edit().putBoolean("trialValid", false).commit();
	}
	
	public void setSNEnabled(boolean enabled) {
		simpleNoteEnabled = true;
		prefs.edit().putBoolean("simpleNoteEnabled", enabled).commit();
	}
	
	public void setUsername(String name) {
		simpleNoteUsername = name;
		prefs.edit().putString("simpleNoteUsername", name).commit();
	}
	
	public void setPassword(String password) {
		simpleNotePassword = password;
		prefs.edit().putString("simpleNotePassword", password).commit();
	}
	
	public void setPasswordChecked(boolean checked) {
		passwordChecked = checked;
		prefs.edit().putBoolean("passwordchecked", checked).commit();
	}

}
