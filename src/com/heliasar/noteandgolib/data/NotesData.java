package com.heliasar.noteandgolib.data;

import java.util.ArrayList;

import com.heliasar.simplenote.SimpleNote;

import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class NotesData {

	private DbHelper dbh;
	private SimpleNote simpleNote;
	private Context context;

	public NotesData(Context context, String username, String password) {
		this.context = context;
		dbh = new DbHelper(context);
		simpleNote = new SimpleNote(username, password, this);
	}

	public long getCount() {
		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbHelper.DB_TABLE,
				null);
		if (c == null) {
			db.close();
			return 0;
		}

		c.moveToFirst();
		String[] cols = c.getColumnNames();
		final long count = c.getLong(c.getColumnIndexOrThrow(cols[0]));
		db.close();
		return count;
	}

	public ArrayList<Note> getIndex() {
		SQLiteDatabase db = dbh.getReadableDatabase();
		ArrayList<Note> notes = new ArrayList<Note>();

		Cursor c = db.query(DbHelper.DB_TABLE, DbHelper.columns, null, null,
				null, null, DbHelper.ROWID + " DESC");
		if (c == null) {
			db.close();
			return null;
		}

		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			notes.add(new Note(c));
			c.moveToNext();
		}
		db.close();
		return notes;
	}

	public void sync() {
		try {
			simpleNote.sync();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Uri create(Note note) {
		return context.getContentResolver().insert(NotesProvider.CONTENT_URI,
				note.getContentValues());
	}

	public Cursor getAll() {
		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor c = db.query(DbHelper.DB_TABLE, DbHelper.columns,
				DbHelper.DELETED + "=" + 0, null, null, null,
				DbHelper.MODIFY_DATE + " DESC");
		db.close();
		return c;
	}

	public Cursor get(long noteId) {
		Cursor c = context.getContentResolver().query(NotesProvider.CONTENT_URI, Note.projection,
				DbHelper.ROWID + "=" + noteId, null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}

	public int update(Note note) {
		return context.getContentResolver().update(NotesProvider.CONTENT_URI,
				note.getContentValues(), DbHelper.ROWID + "=" + note.rowId,
				null);
	}

	public int delete(Note note) {
		return context.getContentResolver().delete(NotesProvider.CONTENT_URI, DbHelper.ROWID + "=" + note.rowId, null);		
	}
	
	public void deleteAll() {
		SQLiteDatabase db = dbh.getWritableDatabase();
		db.delete(DbHelper.DB_TABLE, null, null);
		db.close();
	}
	
	public void close() {
		dbh.close();
	}

}
