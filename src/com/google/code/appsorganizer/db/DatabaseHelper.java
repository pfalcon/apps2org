/**
 * 
 */
package com.google.code.appsorganizer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String INTERNET = "Internet";

	private static final String ANDROID = "Android";

	private static final String TAG = "DatabaseHelper";

	public final AppLabelDao appsLabelDao;
	public final LabelDao labelDao;

	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, "data", null, DATABASE_VERSION);
		labelDao = LabelDao.getSingleton();
		appsLabelDao = AppLabelDao.getSingleton();
		SQLiteDatabase db = getWritableDatabase();
		labelDao.setDb(db);
		appsLabelDao.setDb(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(labelDao.getCreateTableScript());
		db.execSQL(appsLabelDao.getCreateTableScript());

		long internetId = insertLabel(db, INTERNET);
		long androidId = insertLabel(db, ANDROID);

		insertInterneApps(db, internetId);
		insertAndroidApps(db, androidId);
	}

	private void insertAndroidApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.contacts", id);
		insertApp(db, "com.android.mms", id);
		insertApp(db, "com.android.vending", id);

		insertApp(db, "com.android.launcher", id);
		insertApp(db, "com.android.music", id);

		insertApp(db, "com.android.calculator2", id);
		insertApp(db, "com.android.settings", id);
		insertApp(db, "com.android.camera", id);
		insertApp(db, "com.android.alarmclock", id);
	}

	private void insertInterneApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.browser", id);
		insertApp(db, "com.google.android.apps.maps", id);
		insertApp(db, "com.google.android.gm", id);
		insertApp(db, "com.google.android.talk", id);
		insertApp(db, "com.google.android.youtube", id);
		insertApp(db, "com.android.calendar", id);
		insertApp(db, "com.android.email", id);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
		// if (oldVersion <= 20) {
		// db.execSQL("alter table " + labelDao.getName() + " add " +
		// LabelDao.ICON.getName() + " " + LabelDao.ICON.getDescription());
		// }
		db.execSQL(appsLabelDao.getDropTableScript());
		db.execSQL(labelDao.getDropTableScript());
		onCreate(db);
	}

	private long insertLabel(SQLiteDatabase db, String value) {
		ContentValues v = new ContentValues();
		v.put(LabelDao.LABEL.getName(), value);
		return db.insert(LabelDao.NAME, null, v);
	}

	private void insertApp(SQLiteDatabase db, String value, long labelId) {
		ContentValues v = new ContentValues();
		v.put(AppLabelDao.APP.getName(), value);
		v.put(AppLabelDao.LABEL_ID.getName(), labelId);
		db.insert(AppLabelDao.NAME, null, v);
	}
}