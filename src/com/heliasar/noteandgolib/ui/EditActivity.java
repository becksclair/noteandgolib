package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.AppController;
import com.heliasar.noteandgolib.R;
import com.heliasar.tools.Utils;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class EditActivity extends SherlockFragmentActivity {

	private EditFragment editFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!Utils.isAboveHoneycomb())
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		else
			requestWindowFeature(Window.FEATURE_ACTION_BAR);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.view_edit);
		
		editFragment = new EditFragment();
		Bundle extras = getIntent().getExtras();
		if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null) {
			extras.putString("content", getIntent().getStringExtra(Intent.EXTRA_TEXT));	
			extras.putBoolean("shared", true);
			
		}
		
		editFragment.setArguments(extras);

		getSupportFragmentManager().beginTransaction().replace(R.id.editFrame, editFragment).commit();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!Utils.isAppPaid(((AppController) getApplication()).getPreferences())) {
			return super.onCreateOptionsMenu(menu);
		}
		
		// Menu definitions
		MenuItem tagsMenu = menu.add(R.string.tags);
		MenuItem shareMenu = menu.add(R.string.share);

		// Setup menu icons
		tagsMenu.setIcon(R.drawable.tag);
		shareMenu.setIcon(R.drawable.abs__ic_menu_share_holo_dark);

		// Setup menu action configs
		tagsMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		shareMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		// Menu Item Actions
		tagsMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				editFragment.getTagsPanel().toggle();
				return true;
			}
		});
		
		shareMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				if (editFragment.currentNote == null) {
					// TODO: Show a alert window
					return true;
				}
				String noteContent = editFragment.currentNote.content;

				Intent shareIntent = new Intent();
				shareIntent.setType("text/plain");
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, noteContent);
				startActivity(Intent.createChooser(shareIntent, "Share via"));
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
