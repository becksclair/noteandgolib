package com.heliasar.noteandgolib.data;

import com.heliasar.tools.Utils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "noteandgo_data.db";
	public static final String DB_TABLE = "notes";
	private static final int DB_VERSION = 2;

	public static final String ROWID = "_id";
	public static final String CHANGED = "changed";
	public static final String CONTENT = "content";
	public static final String TAGS = "tags";
	public static final String CREATE_DATE = "createDate";
	public static final String MODIFY_DATE = "modifyDate";
	public static final String DELETED = "deleted";
	public static final String VERSION = "version";
	public static final String SYNC_NUM = "syncnum";
	public static final String KEY = "key";

	public static final String[] columns = { ROWID, CHANGED, CONTENT, TAGS, CREATE_DATE,
			MODIFY_DATE, DELETED, VERSION, SYNC_NUM, KEY };

	private static final String sql = "CREATE TABLE " + DB_TABLE + " ("
			+ ROWID	+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ CHANGED + " INTEGER NOT NULL,"
			+ CONTENT + " TEXT NOT NULL,"
			+ TAGS + " TEXT NOT NULL,"
			+ CREATE_DATE + " TEXT NOT NULL,"
			+ MODIFY_DATE + " TEXT NOT NULL,"
			+ DELETED + " INTEGER NOT NULL,"
			+ VERSION + " TEXT NOT NULL,"
			+ SYNC_NUM + " TEXT NOT NULL,"
			+ KEY + " TEXT NOT NULL)";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Utils.l("onCreate SQL: " + sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Utils.l("Upgrading database from version " + oldVersion
				+ " to version " + newVersion);

		switch (oldVersion) {
		case 1:
			Utils.l("** now upgrading from v1 to v2;");
			final String updateSql = "ALTER TABLE " + DB_TABLE + " ADD COLUMN " + CHANGED + " INTEGER DEFAULT 0 NOT NULL";
			db.execSQL(updateSql);
		default:
			Utils.l("** upgrade steps complete.");
			break;
		}
	}
}