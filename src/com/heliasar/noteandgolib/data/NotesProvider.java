package com.heliasar.noteandgolib.data;

import com.heliasar.tools.Utils;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class NotesProvider extends ContentProvider {

	private static final int NOTES = 100;
	private static final int NOTE = 110;

	private static final String AUTHORITY = "com.heliasar.noteandgolib.data.NotesProvider";
	private static final String NOTES_BASE_PATH = "notes";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NOTES_BASE_PATH);
	public static final Uri NOTE_URI = Uri.parse("content://" + AUTHORITY + "/" + NOTES_BASE_PATH + "/#");

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/notes";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/notes";

	private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, NOTES_BASE_PATH, NOTES);
		uriMatcher.addURI(AUTHORITY, NOTES_BASE_PATH + "/#", NOTE);
	}

	private DbHelper dbh;

	@Override
	public synchronized boolean onCreate() {
		dbh = new DbHelper(getContext());
		return true;
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DbHelper.DB_TABLE);

		switch (uriMatcher.match(uri)) {
		case NOTES:
			// No filter
			qb.appendWhere(DbHelper.DELETED + "=0");
			break;
		case NOTE:
			qb.appendWhere(DbHelper.ROWID + "=" + uri.getLastPathSegment());

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		Cursor c = qb.query(dbh.getReadableDatabase(),
							projection,
							selection,
							selectionArgs,
							null, null,
							sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase sqlDB;

		sqlDB = dbh.getWritableDatabase();
		try {
			long rowId = sqlDB.insertOrThrow(DbHelper.DB_TABLE, null, values);

			if (rowId > 0) {
				Uri newUri = ContentUris.withAppendedId(uri, rowId);
				getContext().getContentResolver().notifyChange(uri, null);
				return newUri;
			} else {
				throw new SQLException("Failed to insert row into " + uri);
			}
		} catch (SQLiteConstraintException e) {
			Utils.l("Ignoring constraint failure.");
		}

		return null;
	}

	@Override
	public synchronized int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = dbh.getWritableDatabase();
		int count;

		switch (uriMatcher.match(uri)) {
		case NOTES:
			count = db.update(DbHelper.DB_TABLE, values, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public synchronized int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbh.getWritableDatabase();
		int count;
		
		switch (uriMatcher.match(uri)) {
		case NOTES:
			count = db.delete(DbHelper.DB_TABLE, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public synchronized String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case NOTES:
			return CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

}
