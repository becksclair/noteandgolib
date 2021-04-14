package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class InfoDialog extends SherlockDialogFragment {

	private String message;
	
	public static InfoDialog newInstance(String text) {
		InfoDialog d = new InfoDialog(); 
		d.message = text;
		return d; 
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, 0);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		View v = getSherlockActivity().getLayoutInflater().inflate(R.layout.dialog_info, null);
		
		TextView messageField = (TextView) v.findViewById(R.id.infoText);
		messageField.setText(message);
		
		builder.setView(v);
		builder.setPositiveButton(R.string.okbtn, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setIcon(R.drawable.icon);
		builder.setTitle(R.string.dialog_info_title);
		return builder.create();
	}
	
}
