package com.heliasar.noteandgolib.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.DateUtils;

public class Note {

	public static final String[] projection = {
		    DbHelper.ROWID, DbHelper.CHANGED, DbHelper.KEY,
			DbHelper.CONTENT, DbHelper.TAGS, DbHelper.CREATE_DATE,
			DbHelper.MODIFY_DATE, DbHelper.DELETED, DbHelper.VERSION,
			DbHelper.SYNC_NUM };

	public int changed;
	public long rowId;
	public String content = "";
	public String tags = "";
	public String createDate = "";
	public String modifyDate = "";
	public long deleted = 0;
	public int version = 1;
	public int syncnum = 1;
	public String key = "";
	
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
		setModifyDate();
	}
	
	public void setTags(String tags) {
		this.tags = tags.replace(" ", "").replace("\n", "");
	}

	public String getTitle() {
		if (content.indexOf("\n") != -1) {
			return content.substring(0, content.indexOf("\n"));
		} else {
			return content;
		}
	}

	static public String getCreationDate() {
		return String.valueOf(System.currentTimeMillis() / 1000L);
	}

	static public CharSequence getModifyDate(String timestamp) {
		if (timestamp.indexOf(".") != -1) {
			timestamp = timestamp.substring(0, timestamp.indexOf("."));
		}
		long time = Long.parseLong(timestamp);
		CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(time *1000L,
				System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_TIME);
		return relativeTime;
	}

	private void setModifyDate() {
		modifyDate = getDateTime();
	}

	public Note() {
	}

	public Note(String cont) {
		changed = 1;
		content = cont;
		createDate = getDateTime();
		modifyDate = createDate;
	}

	public Note(Cursor cursor) {
		rowId = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.ROWID));
		changed = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.CHANGED));
		content = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.CONTENT));
		tags = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.TAGS));
		createDate = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.CREATE_DATE));
		modifyDate = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.MODIFY_DATE));
		deleted = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.DELETED));
		version = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.VERSION));
		syncnum = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.SYNC_NUM));
		key = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.KEY));
	}

	public ContentValues getContentValues() {
		ContentValues obj = new ContentValues();

		obj.put(DbHelper.CHANGED, changed);
		obj.put(DbHelper.CONTENT, content);
		obj.put(DbHelper.TAGS, tags);
		obj.put(DbHelper.CREATE_DATE, createDate);
		obj.put(DbHelper.MODIFY_DATE, modifyDate);
		obj.put(DbHelper.DELETED, deleted);
		obj.put(DbHelper.VERSION, version);
		obj.put(DbHelper.SYNC_NUM, syncnum);
		obj.put(DbHelper.KEY, key);

		return obj;
	}

	private String getDateTime() {
		return String.valueOf(System.currentTimeMillis() / 1000L).concat(".000000");
	}
}
