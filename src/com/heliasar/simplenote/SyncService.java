package com.heliasar.simplenote;

import com.heliasar.noteandgolib.data.NotesData;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class SyncService extends IntentService {
	
	private String username;
	private String password;
	private NotesData notesData;

	public SyncService() {
		super("SyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		username = extras.getString("username");
		password = extras.getString("password");

		notesData = new NotesData(this, username, password);
		notesData.sync();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
