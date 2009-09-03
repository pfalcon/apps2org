/*
 * Copyright (C) 2009 Apps Organizer
 *
 * This file is part of Apps Organizer
 *
 * Apps Organizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apps Organizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Apps Organizer.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.google.code.appsorganizer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.SplashScreenActivity;
import com.google.code.appsorganizer.model.Label;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseHelper";

	public final AppLabelDao appsLabelDao;
	public final LabelDao labelDao;
	public final AppCacheDao appCacheDao;

	private static final int DATABASE_VERSION = 14;

	private static DatabaseHelper singleton;

	public static void init(Context context) {
		singleton = new DatabaseHelper(context);
	}

	public static DatabaseHelper initOrSingleton(Context context) {
		if (singleton == null) {
			init(context);
		}
		return singleton;
	}

	public static DatabaseHelper singleton() {
		return singleton;
	}

	private final SQLiteDatabase db;

	public DatabaseHelper(Context context) {
		super(context, "data", null, DATABASE_VERSION);
		labelDao = new LabelDao();
		appsLabelDao = new AppLabelDao();
		appCacheDao = new AppCacheDao();
		db = getWritableDatabase();
		labelDao.setDb(db);
		appsLabelDao.setDb(db);
		appCacheDao.setDb(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(labelDao.getCreateTableScript());
		db.execSQL(appsLabelDao.getCreateTableScript());
		db.execSQL(appCacheDao.getCreateTableScript());

		long internetId = insertLabel(db, null, "Internet", Label.convertToIconDb(R.drawable.globe));
		long androidId = insertLabel(db, null, "Android", Label.convertToIconDb(R.drawable.pda_black));
		long multimediaId = insertLabel(db, null, "Multimedia", Label.convertToIconDb(R.drawable.multimedia));
		long utilityId = insertLabel(db, null, "Utility", Label.convertToIconDb(R.drawable.service_manager));
		insertLabel(db, null, "Games", Label.convertToIconDb(R.drawable.joystick));

		insertInterneApps(db, internetId);
		insertAndroidApps(db, androidId);
		insertMultimediaApps(db, multimediaId);
		insertUtilityApps(db, utilityId);
	}

	private void insertUtilityApps(SQLiteDatabase db, long id) {
		insertApp(db, SplashScreenActivity.class.getName(), id);
	}

	private void insertMultimediaApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.music.MusicBrowserActivity", id);
		insertApp(db, "com.android.music.VideoBrowserActivity", id);
		insertApp(db, "com.android.camera.Camera", id);
		insertApp(db, "com.android.camera.VideoCamera", id);
		insertApp(db, "com.android.camera.GalleryPicker", id);
	}

	private void insertAndroidApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.alarmclock.AlarmClock", id);
		insertApp(db, "com.android.calendar.LaunchActivity", id);
		insertApp(db, "com.android.vending.AssetBrowserActivity", id);
		insertApp(db, "com.android.settings.Settings", id);
		insertApp(db, "com.android.contacts.DialtactsActivity", id);
		insertApp(db, "com.android.contacts.DialtactsContactsEntryActivity", id);
		insertApp(db, "com.android.mms.ui.ConversationList", id);
		insertApp(db, "com.android.calculator2.Calculator", id);
	}

	private void insertInterneApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.browser.BrowserActivity", id);
		insertApp(db, "com.google.android.talk.SigningInActivity", id);
		insertApp(db, "com.google.android.maps.MapsActivity", id);
		insertApp(db, "com.google.android.youtube.HomePage", id);
		insertApp(db, "com.google.android.gm.ConversationListActivityGmail", id);
		insertApp(db, "com.android.email.activity.Welcome", id);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
		if (oldVersion <= 11) {
			db.execSQL(appCacheDao.getCreateTableScript());
		}
		if (oldVersion <= 13) {
			db.execSQL("alter table " + appCacheDao.getName() + " add " + AppCacheDao.STARRED_COL_NAME + ' '
					+ AppCacheDao.STARRED.getDescription());
			db.execSQL("alter table " + appCacheDao.getName() + " add " + AppCacheDao.IGNORED_COL_NAME + ' '
					+ AppCacheDao.IGNORED.getDescription());
		}
		// db.execSQL(appsLabelDao.getDropTableScript());
		// db.execSQL(labelDao.getDropTableScript());
		// onCreate(db);
	}

	private long insertLabel(SQLiteDatabase db, Long id, String value, Integer icon) {
		ContentValues v = new ContentValues();
		v.put(LabelDao.LABEL.getName(), value);
		v.put(LabelDao.ICON.getName(), icon);
		if (id != null) {
			v.put(LabelDao.ID_COL_NAME, id);
		}
		return db.insert(LabelDao.NAME, null, v);
	}

	private void insertApp(SQLiteDatabase db, String value, long labelId) {
		ContentValues v = new ContentValues();
		v.put(AppLabelDao.APP.getName(), value);
		v.put(AppLabelDao.LABEL_ID.getName(), labelId);
		db.insert(AppLabelDao.NAME, null, v);
	}

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		db.setTransactionSuccessful();
	}

	public void endTransaction() {
		db.endTransaction();
	}

}