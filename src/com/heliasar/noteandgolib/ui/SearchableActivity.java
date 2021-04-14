package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.R;
import com.heliasar.tools.Utils;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class SearchableActivity extends SherlockFragmentActivity {

	public String searchQuery;
	private ActionBar ab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupLayout();

		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			searchQuery = intent.getStringExtra(SearchManager.QUERY);
		}
	}

	private void setupLayout() {
		if (!Utils.isAboveHoneycomb())
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		else
			requestWindowFeature(Window.FEATURE_ACTION_BAR);

		setContentView(R.layout.view_search);

		ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
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

	public String getSearchQuery() {
		return this.searchQuery;
	}
}
