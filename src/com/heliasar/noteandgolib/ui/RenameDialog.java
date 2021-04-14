package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.data.PhotosAdapter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


public class RenameDialog extends DialogFragment {

	private TextView filenameTextView;

	private String currentName;
	public PhotosAdapter adapter;
	public int selectedItem;

	public static RenameDialog newInstance(String current) {
		RenameDialog f = new RenameDialog();

		Bundle args = new Bundle();
		args.putString("current", current);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentName = getArguments().getString("current");

		setStyle(DialogFragment.STYLE_NORMAL, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.dialog_rename, container, false);
		filenameTextView = (TextView) v.findViewById(R.id.photoTitleField);
		filenameTextView.setText(currentName);

		getDialog().setTitle(R.string.rename_dialog_title);

		v.findViewById(R.id.renameOk).setOnClickListener(okBtnAction);
		v.findViewById(R.id.renameCancel).setOnClickListener(cancelBtnAction);
		return v;
	}

	private OnClickListener okBtnAction = new OnClickListener() {
		public void onClick(View v) {
			final String text = filenameTextView.getText().toString();
			adapter.rename(selectedItem, text);
			getDialog().dismiss();
		}
	};

	private OnClickListener cancelBtnAction = new OnClickListener() {
		public void onClick(View v) {
			getDialog().dismiss();
		}
	};

}
