package com.heliasar.noteandgolib.data;

import com.heliasar.noteandgolib.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class PhotosAdapter extends BaseAdapter {

	public Context context;
	private LayoutInflater inflater;
	private PhotosManager photosManager;
	private boolean isCacheEmpty = true;

	public PhotosAdapter(SherlockFragmentActivity activity) {
		context = activity.getApplicationContext();
		photosManager = new PhotosManager(this, activity);
		inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}
	
	@SuppressWarnings("deprecation")
	public View getView(int position, View convertView, ViewGroup parent) {

		View vi = convertView;
		TextView nameView;

		if (vi == null)
			vi = inflater.inflate(R.layout.view_photos_tpl, null);

		nameView = (TextView) vi.findViewById(R.id.title);

		vi.setId(position);

		// If we don't have an image cached yet, just show a placeholder
		if ((photosManager.cache.size() == 0) || (photosManager.cache.size() < position)) {
		} else {
			vi.setBackgroundDrawable(new BitmapDrawable(context.getResources(), photosManager.cache.get(position).image));
			nameView.setText(photosManager.cache.get(position).getName());
		}

		return vi;
	}
	
	public void onLowMemory() {
		isCacheEmpty = true;
		photosManager.emptyCache();
	}

	public void initialize() {
		isCacheEmpty = false;
		photosManager.restoreCache();
	}

	public void refresh() {
		isCacheEmpty = false;
		photosManager.cachePhotos();
	}

	public void delete(int position) {
		photosManager.deletePhoto(position);
	}

	public boolean rename(int position, String newName) {
		return photosManager.renamePhoto(position, newName);
	}

	public int getCount() {
		return photosManager.cache.size();
	}

	public Object getItem(int position) {
		return photosManager.cache.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}
	
	public boolean getIsCacheEmpty() {
		return isCacheEmpty;
	}
	
}
