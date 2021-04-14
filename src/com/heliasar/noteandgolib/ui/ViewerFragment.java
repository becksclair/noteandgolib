package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.data.Photo;
import com.heliasar.toolkit.imageviewer.ImageViewerFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.flurry.android.FlurryAgent;

public class ViewerFragment extends ImageViewerFragment {

	//private ActionBar ab;
	private Photo currentPhoto;
	private int position;
	
	public Photo getPhoto() {
		return currentPhoto;
	}

	public void setPhoto(Photo currentPhoto) {
		this.currentPhoto = currentPhoto;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public ViewerFragment() {
		return;
	}
	
	public ViewerFragment(Context context, Photo photo, int position) {
		if (context == null || photo == null || position == -1) return;
		
		setContext(context);
		setPhoto(photo);
		setPosition(position);
		
		Bundle args = new Bundle();
		args.putString("fileUri", Uri.fromFile(photo.fileRef).toString());
		setArguments(args);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		FlurryAgent.logEvent("Image viewer opened");
		//ab = getSupportActivity().getSupportActionBar();
		//ab.setSubtitle(currentPhoto.getName());
	}
	
}
