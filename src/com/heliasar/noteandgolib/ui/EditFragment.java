package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.AppController;
import com.heliasar.noteandgolib.Preferences;
import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.data.Note;
import com.heliasar.noteandgolib.data.NotesData;
import com.heliasar.toolkit.SlidingPanel;
import com.heliasar.tools.Utils;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;

public class EditFragment extends SherlockFragment {

	public Preferences prefs;
	private SlidingPanel tagsPanel;
	private EditText tagsField;

	private NotesData notesData;
	private ActionBar ab;
	public Note currentNote;
	private String previousContent;
	private String previousTags;
	private EditText contentText;

	public static EditFragment newInstance(long index) {
		EditFragment fragment = new EditFragment();

		Bundle args = new Bundle();
		args.putLong("index", index);
		fragment.setArguments(args);
		return fragment;
	}
	
	public SlidingPanel getTagsPanel() {
		return tagsPanel;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FlurryAgent.logEvent("Editor opened");

		if (container == null) return null;
		Context context = getActivity().getApplicationContext();

		prefs = ((AppController) getActivity().getApplication()).getPreferences();
		ab = getSherlockActivity().getSupportActionBar();
		ab.setTitle(R.string.untitled);

		notesData = new NotesData(context, prefs.simpleNoteUsername, prefs.simpleNotePassword);

		inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View views = inflater.inflate(R.layout.view_edit_content, null);
		tagsPanel = (SlidingPanel) views.findViewById(R.id.tagsPanel);
		tagsField = (EditText) tagsPanel.findViewById(R.id.tagsField);

		contentText = (EditText) views.findViewById(R.id.contentText);

		if (prefs.linkifyEnabled) {
			contentText.setAutoLinkMask(Linkify.ALL);
		}

		if (getActivity() instanceof EditActivity) {
			Utils.loadAds((AdView) views.findViewById(R.id.adView), prefs);
		}

		populateFields();
		return views;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		saveState();
		outState.putSerializable("currentIndex", getCurrentIndex());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		saveState();
		super.onPause();
	}

	@Override
	public void onResume() {
		if (Utils.isTablet((SherlockFragmentActivity) this.getActivity())) {
			populateFields();
		}
		if (tagsPanel.getVisibility() == View.VISIBLE) {
			tagsPanel.toggle();
		}
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (notesData != null) {
			notesData.close();
		}
		super.onDestroy();
	}

	private void saveState() {
		String content = contentText.getText().toString();
		String tags = tagsField.getText().toString();

		// If there isn't a rowId, create a new note
		if (currentNote == null) {
			if (content.length() <= 0) {
				return;
			}

			currentNote = new Note(content);
			notesData.create(currentNote);
		} else {
			if (previousContent == null) return;
			
			if (previousContent.compareTo(content) == 0 &&
				previousTags.compareTo(tags) == 0) {
				notesData.close();
				return;
			}

			// Update the note
			if (content.length() > 0) {
				currentNote.changed = 1;
				currentNote.setTags(tagsField.getText().toString());
				currentNote.setContent(content);
				notesData.update(currentNote);
				
				if (this.getSherlockActivity() instanceof MainActivity) {
					((MainActivity) this.getSherlockActivity()).startServices();
				}

			} else { // If the note is empty, delete it
				currentNote.setContent(content);
				currentNote.setTags(tagsField.getText().toString());
				currentNote.changed = 1;
				currentNote.deleted = 1;
				notesData.update(currentNote);
			}
		}
		notesData.close();
	}

	public long getCurrentIndex() {
		long ret = 0;

		if (getArguments() == null)
			return ret;
		else
			ret = getArguments().getLong("index", 0);
		return ret;
	}

	private void populateFields() {
		long index = getCurrentIndex();
		
		if (getArguments() != null) {
			String intentContent = getArguments().getString("content");
			if (intentContent != null) {
				contentText.setText(intentContent);
				return;
			}
		}
		
		if (index == 0) {
			return;				
		}
		
		Cursor c = notesData.get(index);
		currentNote = new Note(c);	

		ab.setTitle(currentNote.getTitle());
		ab.setSubtitle(Note.getModifyDate(currentNote.modifyDate));

		tagsField.setText(currentNote.tags);
		previousContent = currentNote.content;
		previousTags = currentNote.tags;
		contentText.setText(currentNote.content);
	}
}
