package com.heliasar.tools;

import com.heliasar.noteandgolib.AppController;
import com.heliasar.noteandgolib.Config;
import com.heliasar.noteandgolib.Preferences;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class Utils {

	static final String TAG = "AC - LOG";

	private Context context;
	private int duration;
	private Toast toast;

	public static boolean isAboveHoneycomb() {
		if (Build.VERSION.SDK_INT < 11)
			return false;
		else
			return true;
	}

	@SuppressWarnings("deprecation")
	public static boolean isTablet(SherlockFragmentActivity activity) {
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		if (width > 800) {
			return true;
		} else {
			return false;
		}
	}
	
	public static float PixelToDp(float px) {
		Context context = AppController.getContext();
		DisplayMetrics dp = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, dp);
	}

	public Utils() {
		this.duration = Toast.LENGTH_LONG;
	}

	public Utils(Context cont) {
		this.context = cont;
		this.duration = Toast.LENGTH_LONG;
	}

	public void showToast(CharSequence text) {
		this.toast = Toast.makeText(this.context, text, this.duration);
		toast.show();
	}

	public void logt(CharSequence text) {
		this.toast = Toast.makeText(this.context, text, this.duration);
		toast.show();
	}

	@SuppressWarnings("unused")
	public static void l(String text) {
		if (Config.LOGGING || Config.DEBUG) {
			Log.d(TAG, text);
		}
	}
	
	public static void loadAds(AdView adView, Preferences prefs) {
		if (!Utils.isAppPaid(prefs)) {
			class AdThread implements Runnable {
				private AdView v;
				
				public AdThread(AdView view) {
					v = view;
				}

				@Override
				public void run() {
					AdRequest adRequest = new AdRequest();
					adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
					adRequest.addTestDevice("F2F0DF1E4835F0434F127A51EC9F93EE");
					v.loadAd(adRequest);
				}
			}
			
			new Handler().postDelayed(new AdThread(adView), 1000);
			
		} else {
			adView.destroyDrawingCache();
			adView.destroy();
		}
	}
	
	public static boolean isAppPaid(Preferences prefs) {
		if (Config.IsPaid(prefs.getContext())) return true;
		boolean paidAppPurchased = prefs.prefs.getBoolean("paidApp", false);
		return paidAppPurchased;
	}
	
}
