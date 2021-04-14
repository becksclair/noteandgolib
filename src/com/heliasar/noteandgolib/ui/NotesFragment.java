package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.AppController;
import com.heliasar.noteandgolib.Preferences;
import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.data.DbHelper;
import com.heliasar.noteandgolib.data.Note;
import com.heliasar.noteandgolib.data.NotesData;
import com.heliasar.noteandgolib.data.NotesProvider;
import com.heliasar.toolkit.ConfirmDialog;
import com.heliasar.toolkit.ToolkitUtils;
import com.heliasar.toolkit.quickaction.ActionItem;
import com.heliasar.toolkit.quickaction.QuickAction;
import com.heliasar.toolkit.quickaction.QuickAction.OnActionItemClickListener;
import com.heliasar.tools.Utils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.ads.AdView;

public class NotesFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int NOTES_LOADER = 0x100;

	private Preferences prefs;
	private boolean dualPane;
	private NotesData notesData;
	private SimpleCursorAdapter notesAdapter;
	private String searchQuery;

	private QuickAction quickActions;
	private static final int IDA_EDIT = 1;
	private static final int IDA_REMOVE = 2;
	private static final int IDA_SHARE = 3;

	private int selectedItem;
	private long selectedId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.view_notes, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		prefs = ((AppController) getActivity().getApplication()).getPreferences();
		Utils.loadAds((AdView) view.findViewById(R.id.adView), prefs);
		ToolkitUtils.animateListView(getListView());
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
		
		AdapterView.AdapterContextMenuInfo aMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
		selectedItem = aMenuInfo.position;
		selectedId = aMenuInfo.id;

		quickActions.show(aMenuInfo.targetView);
		quickActions.setOnActionItemClickListener(quickActionsEventHandler);
	}

	private OnActionItemClickListener quickActionsEventHandler = new OnActionItemClickListener() {
		public void onItemClick(QuickAction source, int pos, int actionId) {
			switch (actionId) {
			case IDA_EDIT:
				showEdit(selectedItem, selectedId);
				break;

			case IDA_REMOVE:
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				Fragment prev = getFragmentManager().findFragmentByTag("confirm");
				if (prev != null) {
					ft.remove(prev);
				}

				ConfirmDialog confirm = ConfirmDialog.newInstance(
						(FragmentActivity) getActivity(), R.string.app_name,
						R.drawable.icon, R.string.delete_message,
						confirmOkAction, confirmCancelAction);
				confirm.show(ft, "confirm");
				break;

			case IDA_SHARE:
				Note shareNote = new Note(notesData.get(selectedId));

				Intent shareIntent = new Intent();
				shareIntent.setType("text/plain");
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						shareNote.content);
				startActivity(Intent.createChooser(shareIntent, "Share via"));
				break;
			}
		}
	};

	private DialogInterface.OnClickListener confirmOkAction = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			ToolkitUtils.animateListViewRow(getSherlockActivity(), getListView(), selectedItem);
			
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Note note = new Note(notesData.get(selectedId));
					note.deleted = 1;
					note.changed = 1;
					notesData.update(note);
					notesAdapter.notifyDataSetChanged();
					if (dualPane) {
						showEdit(0, 0);
					}
				}
			}, 500);
		}
	};

	private DialogInterface.OnClickListener confirmCancelAction = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {

		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Context context = getActivity().getApplicationContext();
		notesData = new NotesData(context, prefs.simpleNoteUsername, prefs.simpleNotePassword);

		String[] bindFrom = { DbHelper.CONTENT };
		int[] bindTo = { R.id.title };
		getLoaderManager().initLoader(NOTES_LOADER, null, this);
		notesAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
				R.layout.view_list_row, null, bindFrom, bindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		notesAdapter.setViewBinder(VIEW_BINDER);
		setListAdapter(notesAdapter);

		View editFrame = getActivity().findViewById(R.id.editFrame);
		dualPane = editFrame != null ? true : false;

		if (dualPane)
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		// Initialize Context menus
		quickActions = new QuickAction(getActivity());
		quickActions.setAnimStyle(QuickAction.ANIM_AUTO);

		ActionItem editItem = new ActionItem(IDA_EDIT, "Edit", getResources().getDrawable(R.drawable.ic_menu_compose_holo_light));
		ActionItem removeItem = new ActionItem(IDA_REMOVE, "Remove", getResources().getDrawable(R.drawable.ic_launcher_trashcan_normal_holo));
		ActionItem shareItem = new ActionItem(IDA_SHARE, "Share", getResources().getDrawable(R.drawable.ic_menu_share_holo_light));

		quickActions.addActionItem(editItem);
		quickActions.addActionItem(removeItem);
		quickActions.addActionItem(shareItem);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		FragmentActivity activity = (FragmentActivity) getActivity();

		// If we're not in our main activity means we're searching
		// so we replace the cursor for the search one
		if (!(activity instanceof MainActivity)) {
			searchQuery = ((SearchableActivity) getActivity()).getSearchQuery();

			return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
					Note.projection, DbHelper.CONTENT + " LIKE '%" + searchQuery + "%' OR " +
					DbHelper.TAGS + " LIKE '%" + searchQuery + "%'", null, DbHelper.CONTENT);

		} else {
			if (prefs.orderBy.compareTo("modify_date") == 0) {
				return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
						Note.projection, null, null, DbHelper.MODIFY_DATE + " DESC");
			} else {
				return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
						Note.projection, null, null, DbHelper.CONTENT + " ASC");
			}
		}
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		notesAdapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		notesAdapter.swapCursor(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showEdit(position, id);
	}

	private void showEdit(int pos, long index) {
		if (dualPane) {
			EditFragment edit = null;
			Fragment f = getFragmentManager().findFragmentById(R.id.editFrame);

			if (f != null
					&& (getFragmentManager().findFragmentById(R.id.editFrame).getClass() == EditFragment.class)) {

				edit = (EditFragment) getFragmentManager().findFragmentById(R.id.editFrame);
			}

			if (edit == null || edit.getCurrentIndex() != index) {
				edit = EditFragment.newInstance(index);

				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.editFrame, edit);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
		} else {
			Intent i = new Intent();
			i.setClass(getActivity(), EditActivity.class);
			i.putExtra("index", index);
			startActivity(i);
		}
	}

	public void openSearch(String query) {
		EditFragment edit = null;
		Fragment f = getFragmentManager().findFragmentById(R.id.editFrame);

		if (f != null && (getFragmentManager().findFragmentById(R.id.editFrame).getClass() == EditFragment.class)) {
			edit = (EditFragment) getFragmentManager().findFragmentById(R.id.editFrame);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.remove(edit);
			ft.commit();
		}
	}

	static final ViewBinder VIEW_BINDER = new ViewBinder() {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (cursor.getColumnIndex(DbHelper.CONTENT) == columnIndex) {
				String title = cursor.getString(columnIndex);

				if (view.getId() == R.id.title) {
					if (title.indexOf("\n") != -1) {
						title = title.substring(0, title.indexOf("\n"));
					}
					((TextView) view).setText(title);
				}
				return true;
			}
			return false;
		}
	};
}
