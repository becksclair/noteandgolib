package com.heliasar.noteandgolib.ui;

import com.heliasar.toolkit.ConfirmDialog;
import com.heliasar.toolkit.imageviewer.ViewerActivity;
import com.heliasar.toolkit.quickaction.ActionItem;
import com.heliasar.toolkit.quickaction.QuickAction;
import com.heliasar.toolkit.quickaction.QuickAction.OnActionItemClickListener;
import com.heliasar.noteandgolib.AppController;
import com.heliasar.noteandgolib.Preferences;
import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.data.Photo;
import com.heliasar.noteandgolib.data.PhotosAdapter;
import com.heliasar.tools.Utils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.ads.AdView;

public class PhotosFragment extends SherlockFragment {

	private GridView grid;
	private Preferences prefs;

	private QuickAction quickActions;
	private static final int IDA_RENAME = 1;
	private static final int IDA_REMOVE = 2;
	private static final int IDA_SHARE = 3;

	private boolean dualPane;
	private int selectedItem;
	
	public PhotosAdapter getPhotosAdapter() {
		MainActivity activity = (MainActivity) getSherlockActivity();
		if (activity != null) {
			return activity.getPhotosAdapter();			
		}
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		grid.setAdapter(getPhotosAdapter());
		grid.setOnItemClickListener(photoSelectedAction);

		View viewer = getActivity().findViewById(R.id.editFrame);
		dualPane = viewer != null ? true : false;

		// Initialize Context menus
		quickActions = new QuickAction(getActivity());
		quickActions.setAnimStyle(QuickAction.ANIM_AUTO);

		ActionItem renameItem = new ActionItem(IDA_RENAME, "Rename",
				getResources().getDrawable(R.drawable.ic_menu_compose_holo_light));
		
		ActionItem removeItem = new ActionItem(IDA_REMOVE, "Remove",
				getResources().getDrawable(R.drawable.ic_launcher_trashcan_normal_holo));
		
		ActionItem shareItem = new ActionItem(IDA_SHARE, "Share",
				getResources().getDrawable(R.drawable.ic_menu_share_holo_light));

		quickActions.addActionItem(renameItem);
		quickActions.addActionItem(removeItem);
		quickActions.addActionItem(shareItem);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.view_photos, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		prefs = ((AppController) getActivity().getApplication()).getPreferences();
		grid = (GridView) getView().findViewById(R.id.gridview);
		registerForContextMenu(grid);
		Utils.loadAds((AdView) view.findViewById(R.id.adView), prefs);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

		AdapterView.AdapterContextMenuInfo aMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
		selectedItem = aMenuInfo.position;

		quickActions.show(aMenuInfo.targetView);
		quickActions.setOnActionItemClickListener(new OnActionItemClickListener() {
			public void onItemClick(QuickAction source, int pos, int actionId) {
				switch (actionId) {
				case IDA_RENAME:
					Photo photoRename = (Photo) getPhotosAdapter().getItem(selectedItem);
					showRenameDialog(photoRename.getName());
					break;

				case IDA_REMOVE:
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					Fragment prev = getFragmentManager().findFragmentByTag("confirm");
					if (prev != null) {
						ft.remove(prev);
					}

					ConfirmDialog confirm = ConfirmDialog.newInstance(
							(FragmentActivity) getActivity(),
							R.string.app_name, R.drawable.icon,
							R.string.delete_message, confirmOkAction,
							confirmCancelAction);
					
					confirm.show(ft, "confirm");
					break;

				case IDA_SHARE:
					Photo photo = (Photo) getPhotosAdapter().getItem(selectedItem);

					Intent shareIntent = new Intent();
					shareIntent.setAction(Intent.ACTION_SEND);
					shareIntent.setType("image/jpeg");
					shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo.fileRef));
					shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, photo.getName());
					startActivity(Intent.createChooser(shareIntent, "Share via"));
					break;
				}
			}
		});
	}

	private OnItemClickListener photoSelectedAction = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			showPhoto(position);
		}
	};

	private DialogInterface.OnClickListener confirmOkAction = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			getPhotosAdapter().delete(selectedItem);
		}
	};

	private DialogInterface.OnClickListener confirmCancelAction = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {

		}
	};

	private void showPhoto(int position) {
		Context context = getSherlockActivity().getApplicationContext();
		Photo photo = (Photo) getPhotosAdapter().getItem(position);

		if (dualPane) {
			ViewerFragment viewer = null;
			FragmentManager fm = getFragmentManager();
			Fragment f = fm.findFragmentById(R.id.editFrame);
			
			if (f != null && (f.getClass() == ViewerFragment.class)) {
				viewer = (ViewerFragment) f;
				
			} else {
				viewer = new ViewerFragment(context, photo, position);
			}

			if (viewer == null || (viewer.getPosition() != position)) {
				viewer = new ViewerFragment(context, photo, position);
			}
			
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.editFrame, viewer);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();

		} else {
			Intent intent = new Intent();
			intent.setClass(context, ViewerActivity.class);
			intent.putExtra("fileUri", Uri.fromFile(photo.fileRef).toString());
			startActivity(intent);
		}
	}

	private void showRenameDialog(String current) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("rename");
		if (prev != null) {
			ft.remove(prev);
		}

		ft.addToBackStack(null);

		RenameDialog renameDialog = RenameDialog.newInstance(current);
		renameDialog.adapter = getPhotosAdapter();
		renameDialog.selectedItem = selectedItem;
		renameDialog.show(ft, "rename");
	}
}
