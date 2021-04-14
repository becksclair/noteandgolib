package com.heliasar.noteandgolib.ui;

import java.util.List;
import java.util.Vector;

import com.heliasar.noteandgolib.AppController;
import com.heliasar.noteandgolib.Preferences;
import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.data.PhotosAdapter;
import com.heliasar.noteandgolib.data.Storage;
import com.heliasar.simplenote.SimpleNote;
import com.heliasar.simplenote.SyncService;
import com.heliasar.toolkit.CustomDialog;
import com.heliasar.tools.TrackTrials;
import com.heliasar.tools.Utils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;
import com.flurry.android.FlurryAgent;

public class MainActivity extends SherlockFragmentActivity {
	
	public static final int REQUEST_APP_PURCHASE = 2012;

	private ActionBar ab;
	private ViewPager pager;
	private PageAdapter pagerAdapter;
	private PhotosAdapter photosAdapter;
	private ProgressDialog progressDlg;

	public Preferences prefs;

	private boolean askPasswordVisible = false;
	private boolean restartApp = false;
	private boolean dualPane = false;
	private boolean cameraReturnValue = false;

	private MenuItem purchaseMenu;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPassword(false);
	}
	
	private void askPassword() {
		if (askPasswordVisible) return;
		
		LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = li.inflate(R.layout.dialog_login, null);
		CustomDialog dialog = new CustomDialog(this,
				R.string.app_name, R.drawable.icon,
				getResources().getString(R.string.message), view,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog v = ((AlertDialog) dialog);
						EditText usernameField = (EditText) v.findViewById(R.id.username);
						EditText passwordField = (EditText) v.findViewById(R.id.password);
						
						final String username = usernameField.getText().toString().trim();
						final String password = passwordField.getText().toString().trim();
						
						if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
							prefs.setSNEnabled(true);
							prefs.setUsername(username);
							prefs.setPassword(password);
							
							// Check login
							MainActivity.this.checkPassword(true);
							askPasswordVisible = false;
							return;
						}
						askPasswordVisible = false;
						dialog.dismiss();
						askPassword();
					}
				},
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						askPasswordVisible = false;
					}
				});
		dialog.show(getSupportFragmentManager(), "logindialog");
		askPasswordVisible = true;
	}
	
	private void showProgressDialog() {
		progressDlg = ProgressDialog.show(MainActivity.this, null, "Checking credentials...");
	}
	
	private void hideProgressDialog() {
		progressDlg.hide();
	}
	
	public void checkPassword(final boolean sync) {
		if (prefs.passwordChecked) return;
		
		if (prefs.isSNenabled()) {
			showProgressDialog();
			new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					SimpleNote sn = new SimpleNote(prefs.simpleNoteUsername, prefs.simpleNotePassword);
					return sn.login();
				}
				
				@Override
				protected void onPostExecute(Boolean result) {
					super.onPostExecute(result);
					hideProgressDialog();
					
					prefs.setPasswordChecked(true);
					if (result) {
						if (sync) startServices();
						return;
					}

					askPassword();
					FragmentManager fm = getSupportFragmentManager();
					CustomDialog f = (CustomDialog) fm.findFragmentByTag("wrong_password");
					if (f == null) {
						f = new CustomDialog(MainActivity.this,
								R.string.app_name, R.drawable.icon, "Incorrect password");
					}
					f.show(getSupportFragmentManager(), "wrong_password");
				}
			}.execute();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupLayout();
		Storage.createPhotosStorage();
		
		FlurryAgent.onStartSession(MainActivity.this, getString(R.string.analytics_id));
		FlurryAgent.logEvent("App started");
		// Perform first run checks
		if (prefs.firstRun()) {
			askPassword();
		}

		if (!Utils.isAppPaid(prefs)) {
			if (prefs.firstRun()) {
				TrackTrials.checkCouponRedeemed(this);
				TrackTrials.isTrialRunning(this, prefs);
			} else {
				if (prefs.trialValid) {
					TrackTrials.isTrialRunning(this, prefs);
					FlurryAgent.logEvent("Started in Trial mode");
				} else {
					trialExpired();
					FlurryAgent.logEvent("Started with Trial expired");
				}
			}
		}
	}

	private void setupLayout() {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);

		prefs = ((AppController) getApplication()).getPreferences();
		prefs.initialize((AppController) getApplication());

		if (Utils.isTablet(this) || prefs.forceTablet) {
			setContentView(R.layout.view_tablet);
			dualPane = true;
		} else {
			Fragment edit = getSupportFragmentManager().findFragmentById(R.id.editFrame);
			if (edit != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.remove(edit);
				ft.commit();
			}
			setContentView(R.layout.view_phone);
		}

		ab = getSupportActionBar();
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ab.setDisplayHomeAsUpEnabled(false);

		if (getResources().getBoolean(R.bool.IsPhone)) {
			ab.setDisplayShowTitleEnabled(false);
		}

		photosAdapter = new PhotosAdapter(this);
		photosAdapter.initialize();

		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(NotesFragment.instantiate(this, NotesFragment.class.getName()));
		fragments.add(PhotosFragment.instantiate(this, PhotosFragment.class.getName()));

		pager = (ViewPager) findViewById(R.id.contentFrame);
		pagerAdapter = new PageAdapter(getSupportFragmentManager(), ab, pager, fragments);
		pagerAdapter.dualPane = dualPane;
	}

	public void startServices() {
		if (prefs.isSNenabled()) startSimpleNoteService();
	}

	private void startSimpleNoteService() {
		Intent intent = new Intent(MainActivity.this, SyncService.class);
		intent.putExtra("username", prefs.simpleNoteUsername);
		intent.putExtra("password", prefs.simpleNotePassword);
		startService(intent);
	}

	public PhotosAdapter getPhotosAdapter() {
		return photosAdapter;
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(getString(R.string.search_hint), true, null, false);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Menu definitions
		MenuItem newMenu = menu.add(R.string.new_note);
		SubMenu extraMenu;

		if (prefs.trialValid || Utils.isAppPaid(prefs)) { // Add case for in-app-billed

			MenuItem searchMenu = menu.add(R.string.search);
			MenuItem captureMenu = menu.add(R.string.photo);

			// Setup menu icons
			captureMenu.setIcon(R.drawable.ic_menu_camera_holo_light);
			searchMenu.setIcon(R.drawable.ic_menu_search_holo_light);

			// Setup menu action configs
			captureMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			// Menu Item Actions
			captureMenu.setOnMenuItemClickListener(captureMenuAction);
			searchMenu.setOnMenuItemClickListener(searchMenuAction);
		} else {
			purchaseMenu = menu.add(R.string.purchase);
			purchaseMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			purchaseMenu.setOnMenuItemClickListener(purchaseMenuAction);
		}

		if (!Utils.isAppPaid(prefs)) {
			extraMenu = menu.addSubMenu("Extra");
			MenuItem couponMenu = extraMenu.add(R.string.menu_redeem_coupon);
			couponMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			couponMenu.setOnMenuItemClickListener(couponMenuAction);
		} else {
			extraMenu = menu.addSubMenu("Extra");
		}

		MenuItem syncMenu = extraMenu.add(R.string.sync);
		syncMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		syncMenu.setOnMenuItemClickListener(syncMenuAction);
		
		MenuItem extraMenuItem = extraMenu.getItem();
		MenuItem feedbackMenu = extraMenu.add(R.string.menu_send_feedback);
		MenuItem settingsMenu = extraMenu.add(R.string.settings);

		// Setup menu icons
		newMenu.setIcon(R.drawable.ic_menu_add_field_holo_light);
		extraMenuItem.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);

		// Setup menu action configs
		newMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		extraMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		feedbackMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		settingsMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		// Menu Item Actions
		newMenu.setOnMenuItemClickListener(newMenuAction);
		feedbackMenu.setOnMenuItemClickListener(feedbackMenuAction);
		settingsMenu.setOnMenuItemClickListener(settingsMenuAction);

		return super.onCreateOptionsMenu(menu);
	}

	private OnMenuItemClickListener newMenuAction = new OnMenuItemClickListener() {
		public boolean onMenuItemClick(MenuItem item) {
			if (dualPane) {
				EditFragment edit = new EditFragment();

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.editFrame, edit);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();

			} else {
				Intent newNoteIntent = new Intent(getApplicationContext(), EditActivity.class);
				startActivity(newNoteIntent);
			}
			return true;
		}
	};

	private OnMenuItemClickListener captureMenuAction = new OnMenuItemClickListener() {
		public boolean onMenuItemClick(MenuItem item) {
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Storage.createPhotoFile()));
			cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(cameraIntent, Storage.CAMERA_PIC_REQUEST);
			return true;
		}
	};

	private OnMenuItemClickListener searchMenuAction = new OnMenuItemClickListener() {
		public boolean onMenuItemClick(MenuItem item) {
			onSearchRequested();
			return true;
		}
	};

	private OnMenuItemClickListener purchaseMenuAction = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			FlurryAgent.logEvent("In-app purchase requested");			
			Intent intent = new Intent(MainActivity.this, PurchaseAppActivity.class);
			MainActivity.this.startActivityForResult(intent, REQUEST_APP_PURCHASE);
			return true;
		}
	};

	private OnMenuItemClickListener syncMenuAction = new OnMenuItemClickListener() {
		public boolean onMenuItemClick(MenuItem item) {
			startServices();
			return true;
		}
	};

	private OnMenuItemClickListener feedbackMenuAction = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			try {
				Intent i = new Intent(android.content.Intent.ACTION_SEND);
				i.setType("plain/text");
				i.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { getString(R.string.feedback_email) });
				i.putExtra(android.content.Intent.EXTRA_SUBJECT,
						getString(R.string.feedback_subject));

				startActivity(Intent.createChooser(i,
						"Select email application"));
			} catch (ActivityNotFoundException e) {
				showInfoDialog("You need an email app to send feedback.");
			}
			return true;
		}
	};

	private OnMenuItemClickListener settingsMenuAction = new OnMenuItemClickListener() {
		public boolean onMenuItemClick(MenuItem item) {
			restartApp = true;
			prefs.setPasswordChecked(false);
			Intent i = new Intent(MainActivity.this,
					com.heliasar.noteandgolib.ui.Preferences.class);
			startActivity(i);
			return true;
		}
	};

	private OnMenuItemClickListener couponMenuAction = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			showRedeemCouponDialog();
			return true;
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_APP_PURCHASE == requestCode) {
            if (RESULT_OK == resultCode) {
                setAppPaid();
            } else {
                dealWithFailedPurchase();
            }
        }
		
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case Storage.CAMERA_PIC_REQUEST:
			cameraReturnValue = true;
			break;
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (restartApp) {
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			finish();
		}

		if (cameraReturnValue) {
			showSaveDialog();
			cameraReturnValue = false;
		}
		
		startServices();
	}

	private void showSaveDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		SherlockFragment prev = (SherlockFragment) getSupportFragmentManager().findFragmentByTag("save");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		SavePhotoDialog saveDialog = SavePhotoDialog.newInstance();
		saveDialog.show(ft, "save");
	}

	public void refreshPhotos() {
		photosAdapter.refresh();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		photosAdapter.onLowMemory();
	}

	private void showRedeemCouponDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);

		RedeemCouponDialog couponDialog = RedeemCouponDialog.newInstance();
		couponDialog.show(ft, "coupon");
	}

	public void showInfoDialog(String message) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);

		InfoDialog infoDialog = InfoDialog.newInstance(message);
		infoDialog.show(ft, "info");
	}

	public void trialValid() {
		Utils.l("Trial mode");
		prefs.setTrialValid();
	}

	public void trialExpired() {
		Toast.makeText(this, "Trial expired!", Toast.LENGTH_LONG).show();
		prefs.setTrialExpired();

		if (ab.getTabCount() >= 1) {
			ab.removeTabAt(1);
			pagerAdapter.setItemsCount(1);
		}
	}

	public void setAppPaid() {
		FlurryAgent.logEvent("Marking app as paid");
		Toast.makeText(this, "Enabling features...", Toast.LENGTH_SHORT).show();
		if (purchaseMenu != null) {
			purchaseMenu.setEnabled(false);
		}
		prefs.prefs.edit().putBoolean("paidApp", true).commit();

		// Restart the app
		Intent i = new Intent(getBaseContext(), com.heliasar.noteandgolib.ui.MainActivity.class);
		finish();
		startActivity(i);
	}

    private void dealWithFailedPurchase() {
        Toast.makeText(MainActivity.this, "Failed to purchase passport", Toast.LENGTH_LONG).show();
    }
}
