package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.R;
import com.heliasar.tools.TrackTrials;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class RedeemCouponDialog extends SherlockDialogFragment {
	
	private EditText textField;
	private String code;
	
	public static RedeemCouponDialog newInstance() {
		return new RedeemCouponDialog();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, 0);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		View v = getSherlockActivity().getLayoutInflater().inflate(R.layout.dialog_coupon, null);
		textField = (EditText) v.findViewById(R.id.couponField);
		
		builder.setView(v);
		builder.setIcon(R.drawable.icon);
		builder.setTitle(R.string.dialog_coupon_title);
		builder.setPositiveButton(R.string.okbtn, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				code = textField.getText().toString();
				TrackTrials.redeemCode(code, (MainActivity) getSherlockActivity());
				dialog.dismiss();
			}
		});
		return builder.create();
	}
	
}
