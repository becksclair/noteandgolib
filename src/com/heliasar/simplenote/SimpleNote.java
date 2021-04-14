package com.heliasar.simplenote;

import java.util.ArrayList;

import com.heliasar.noteandgolib.data.Note;
import com.heliasar.noteandgolib.data.NotesData;
import com.heliasar.simplenote.WebHelper.Response;
import com.heliasar.tools.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SimpleNote {
	private NotesData notesData;

	private String email;
	private String password;
	private String token;

	public SimpleNote(String username, String password) {
		this.email = username;
		this.password = password;
	}
	
	public SimpleNote(String username, String password, NotesData notesData) {
		this.email = username;
		this.password = password;

		// Keep a reference to notesData so we can update back
		this.notesData = notesData;
	}

	private boolean checkLoggedIn() {
		if (token == null) {
			return login();
		} else {
			return true;
		}
	}

	public boolean login() {
		boolean ret = false;
		String authBody = WebHelper.encode("email=" + email + "&password=" + password, true);
		Utils.l(authBody);
		Response authResponse = WebHelper.post(WebHelper.API_LOGIN_URL, authBody);

		if (authResponse.statusCode == 401)
			return ret;

		if (authResponse.statusCode == 200) {
			ret = true;
			token = new String(authResponse.resp);
			// For some reason the token gets back with a '\n' so we remove it
			token = token.substring(0, (token.length() - 1));
		}
		return ret;
	}
	
	public ArrayList<Note> getIndex() throws JSONException {
		return getIndex(null);
	}

	public ArrayList<Note> getIndex(String passedMark) throws JSONException {
		Utils.l("Fetching Index");
		String url = WebHelper.API_NOTES_URL + "?length=100&auth=" + token + "&email=" + email;
		
		if (passedMark != null) {
			url = url + "&mark=" + passedMark;
		}

		// Get the index
		Response authResponse = WebHelper.get(url);

		if (authResponse.statusCode == 200) {
			JSONObject json = new JSONObject(authResponse.resp);
			String mark = null;
			try {
				mark = json.getString("mark");
			} catch (JSONException e) {
			}
			
			JSONArray notes = json.getJSONArray("data");
			ArrayList<Note> notesStore = new ArrayList<Note>();

			for (int i = 0; i < notes.length(); i++) {
				JSONObject jsNote = notes.getJSONObject(i);

				JSONArray tagsArray = jsNote.getJSONArray("tags");
				String tags = new String();
				for (int x = 0; x < tagsArray.length(); x++) {
					tags += tagsArray.get(x).toString() + ",";
				}
				if (tags.length() > 0) {
					tags = tags.substring(0, (tags.length()-1));
				}

				Note note = new Note();

				note.tags = tags;
				note.syncnum = jsNote.getInt("syncnum");
				note.key = jsNote.getString("key");
				note.modifyDate = jsNote.getString("modifydate");
				note.createDate = jsNote.getString("createdate");
				note.deleted = jsNote.getLong("deleted");

				notesStore.add(note);
			}
			
			if (mark != null) {
				if (notesStore != null)
					notesStore.addAll(getIndex(mark));
			}

			return notesStore;
		} else {
			if (!login()) {
				return null;
			} else {
				return getIndex(passedMark);
			}
		}
	}

	public Note get(Note note) throws JSONException {
		Utils.l("Fetching note from server");
		String url = WebHelper.API_NOTE_URL + note.key + "?auth=" + token + "&email=" + email;
		Response authResponse = WebHelper.get(url);
		
		if (authResponse.statusCode == 200) {
			JSONObject obj = new JSONObject(authResponse.resp);
			note.syncnum = obj.getInt("syncnum");
			note.version = obj.getInt("version");
			note.content = obj.getString("content");
			return note;
			
		} else {
			if (!login()) return null;
			else return get(note);
		}
	}

	public boolean sync() throws JSONException {
		Utils.l("Syncing notes");
		
		if (!checkLoggedIn()) return false;
		
		// Send new and modified notes
		ArrayList<Note> localIndex;
		localIndex = notesData.getIndex();

		for (Note note : localIndex) {
			if (note.changed == 1 && note.key.length() > 0) update(note);
			else if (note.changed == 1 && note.key.length() == 0) create(note);
		}
		
		// Get notes that need to be sync
		ArrayList<Note> remoteIndex;
		remoteIndex = getIndex();

		if (remoteIndex == null) return false;

		for (Note note : remoteIndex) {
			Note localNote = SimpleNote.findInArray(localIndex, note);
			if (localNote != null) {
				// Deleted on server, delete local
				if (note.deleted == 1) {
					notesData.delete(localNote);
					continue;
				}
				
				// Server note newer than local
				if (localNote.syncnum < note.syncnum || localNote.version < note.version) {
					localNote.content = note.getContent();
					localNote.modifyDate = note.modifyDate;
					localNote.tags = note.tags;
					notesData.update(get(localNote));
				}
				
			} else {
				// The note is new and needs to be added to our store
				if (note.deleted == 0) notesData.create(get(note));
			}
		}
		return true;
	}

	public boolean create(Note note) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("content", note.getContent());
		JSONArray tagsArray = new JSONArray();
		for (String tag : note.tags.split(",")) {
			tagsArray.put(tag);
		}
		json.put("tags", tagsArray);
		

		Response authResponse = null;
		String url = WebHelper.API_CREATE_URL + "?auth=" + token + "&email=" + email;
		authResponse = WebHelper.post(url, json.toString());

		if (authResponse.statusCode == 200) {
			JSONObject retJson = new JSONObject(authResponse.resp);

			note.key = retJson.getString("key");
			note.modifyDate = retJson.getString("modifydate");
			note.createDate = retJson.getString("createdate");
			note.syncnum = retJson.getInt("syncnum");
			note.version = retJson.getInt("version");
			
			tagsArray = retJson.getJSONArray("tags");
			String tags = new String();
			for (int x = 0; x < tagsArray.length(); x++) {
				tags += "," + tagsArray.get(x).toString();
			}
			note.tags = tags;

			notesData.update(note);
			return true;

		} else {
			if (!login()) return false;
			else return create(note);
		}
	}

	public boolean update(Note note) throws JSONException {
		Utils.l("Updating note");
		JSONObject json = new JSONObject();
		
		if (note.deleted == 1) json.put("deleted", note.deleted);
		json.put("modifydate", note.modifyDate);
		json.put("content", note.getContent());
		json.put("version", note.version);
		
		// Add tags
		JSONArray tagsArray = new JSONArray();
		for (String tag : note.tags.split(",")) {
			if (tag.compareTo(" ") == 0) continue;
			tagsArray.put(tag);
		}
		json.put("tags", tagsArray);
		
		Response authResponse = null;
		String url = WebHelper.API_NOTE_URL + note.key + "?auth=" + token + "&email=" + email;
		authResponse = WebHelper.post(url, json.toString());

		if (authResponse.statusCode == 200) {
			Utils.l("Update succeed");
			JSONObject retJson = new JSONObject(authResponse.resp);

			if (!retJson.isNull("content")) {
				note.content = retJson.getString("content");
			}

			tagsArray = retJson.getJSONArray("tags");
			String tags = new String();
			for (int x = 0; x < tagsArray.length(); x++) {
				tags += "," + tagsArray.get(x).toString();
			}
			note.tags = tags;

			note.modifyDate = retJson.getString("modifydate");
			note.syncnum = retJson.getInt("syncnum");
			note.version = retJson.getInt("version");
			note.changed = 0;

			Utils.l("Storing syncnum: " + String.valueOf(note.syncnum));
			Utils.l("Storing version: " + String.valueOf(note.version));
			
			notesData.update(note);
			return true;
			
		} else {
			Utils.l("Update failed retrying");
			if (!login()) {
				Utils.l("Update failed, couldn't login");
				return false;
			} else {
				return update(note);
			}
		}
	}

	public boolean trash(Note note) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("deleted", note.deleted);

		Response authResponse = null;
		String url = WebHelper.API_NOTE_URL + note.key + "?auth=" + token + "&email=" + email;
		authResponse = WebHelper.post(url, json.toString());

		return authResponse.statusCode == 200 ? true : false;
	}

	public boolean delete(Note note) throws JSONException {
		Response authResponse = null;
		String url = WebHelper.API_NOTE_URL + note.key + "?auth=" + token + "&email=" + email;
		authResponse = WebHelper.delete(url);

		return authResponse.statusCode == 200 ? true : false;
	}

	public static Note findInArray(ArrayList<Note> notes, Note needle) {
		Note retNote = null;

		for (Note note : notes) {
			if (note.key.compareTo(needle.key) == 0)
				retNote = note;
		}
		return retNote;
	}
}
