package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.Config;
import com.heliasar.noteandgolib.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class Preferences extends SherlockPreferenceActivity implements OnPreferenceChangeListener {
	
	private static final String PREF_ORDER_BY = "order_by";
	
	private Context context;
	private ListPreference sortingMode;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean trialValid = prefs.getBoolean("trialValid", true);
		
		context = getApplicationContext();
		PreferenceScreen prefScreen = getPreferenceScreen();

		try {
			sortingMode = (ListPreference) prefScreen.findPreference(PREF_ORDER_BY);
			sortingMode.setOnPreferenceChangeListener(this);
		} catch (Exception e) {
		}
		
		if (!trialValid) {
			addPreferencesFromResource(R.xml.preferences_demo);
		} else if (Config.DEBUG) {
			addPreferencesFromResource(R.xml.preferences_debug);
		} else {
			addPreferencesFromResource(R.xml.preferences);
		}
		
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == sortingMode) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			prefs.edit().putString("order_by", (String) newValue).commit();
			return true;
		}
		return false;
	}
}
