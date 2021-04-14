package com.heliasar.noteandgolib.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.data.Storage;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class SavePhotoDialog extends SherlockDialogFragment {

	private TextView filenameTextView;

	public static SavePhotoDialog newInstance() {
		return new SavePhotoDialog();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.dialog_savefile, container, false);
		filenameTextView = (TextView) v.findViewById(R.id.photoTitleField);
		filenameTextView.setText("");

		getDialog().setTitle(R.string.dialog_set_title_text);
		getDialog().setCancelable(false);

		v.findViewById(R.id.renameOk).setOnClickListener(okBtnAction);
		return v;
	}

	private OnClickListener okBtnAction = new OnClickListener() {
		public void onClick(View v) {
			String text = filenameTextView.getText().toString();
			
			if (text.length() == 0) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
				Date now = new Date();
				text = df.format(now);
			}
			
			Storage.renameFile(text);
			MainActivity activity = (MainActivity) getSherlockActivity();
			activity.refreshPhotos();
			getDialog().dismiss();
		}
	};

}
