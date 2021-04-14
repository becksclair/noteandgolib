package com.heliasar.noteandgolib;

import android.app.Application;
import android.content.Context;

public class AppController extends Application {
	
	// Static members
	private static AppController instance;
	private Preferences prefs;
	
	public AppController() {
		instance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = new Preferences();
	}
	
	public static Context getContext() {
		return instance;
	}
	
	public Preferences getPreferences() {
		return prefs;
	}
	
}
