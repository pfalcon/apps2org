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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.SplashScreenActivity;
import com.google.code.appsorganizer.model.Label;

public class DatabaseHelperBasic extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseHelper";

	private static final int DATABASE_VERSION = 25;

	protected final SQLiteDatabase db;

	private Context context;

	public DatabaseHelperBasic(Context context) {
		super(context, "data", null, DATABASE_VERSION);
		this.context = context;
		db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LabelDao.getCreateTableScript());
		db.execSQL(AppLabelDao.getCreateTableScript());
		db.execSQL(AppCacheDao.getCreateTableScript());

		long internetId = insertLabel(db, null, context.getString(R.string.label_default_internet), Label.convertToIconDb(R.drawable.globe));
		long androidId = insertLabel(db, null, context.getString(R.string.label_default_android), Label.convertToIconDb(R.drawable.pda_black));
		long multimediaId = insertLabel(db, null, context.getString(R.string.label_default_multimedia), Label.convertToIconDb(R.drawable.multimedia));
		long utilityId = insertLabel(db, null, context.getString(R.string.label_default_tools), Label.convertToIconDb(R.drawable.service_manager));
		insertLabel(db, null, context.getString(R.string.label_default_games), Label.convertToIconDb(R.drawable.joystick));

		insertInterneApps(db, internetId);
		insertAndroidApps(db, androidId);
		insertMultimediaApps(db, multimediaId);
		insertUtilityApps(db, utilityId);
	}

	private void insertUtilityApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.google.code.appsorganizer", SplashScreenActivity.class.getName(), id);
	}

	private void insertMultimediaApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.music", "com.android.music.MusicBrowserActivity", id);
		insertApp(db, "com.android.music", "com.android.music.VideoBrowserActivity", id);
		insertApp(db, "com.android.camera", "com.android.camera.Camera", id);
		insertApp(db, "com.android.camera", "com.android.camera.VideoCamera", id);
		insertApp(db, "com.android.camera", "com.android.camera.GalleryPicker", id);
	}

	private void insertAndroidApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.alarmclock", "com.android.alarmclock.AlarmClock", id);
		insertApp(db, "com.android.calendar", "com.android.calendar.LaunchActivity", id);
		insertApp(db, "com.android.vending", "com.android.vending.AssetBrowserActivity", id);
		insertApp(db, "com.android.settings", "com.android.settings.Settings", id);
		insertApp(db, "com.android.contacts", "com.android.contacts.DialtactsActivity", id);
		insertApp(db, "com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity", id);
		insertApp(db, "com.android.mms", "com.android.mms.ui.ConversationList", id);
		insertApp(db, "com.android.calculator2", "com.android.calculator2.Calculator", id);
	}

	private void insertInterneApps(SQLiteDatabase db, long id) {
		insertApp(db, "com.android.browser", "com.android.browser.BrowserActivity", id);
		insertApp(db, "com.google.android.talk", "com.google.android.talk.SigningInActivity", id);
		insertApp(db, "com.google.android.apps.maps", "com.google.android.maps.MapsActivity", id);
		insertApp(db, "com.google.android.youtube", "com.google.android.youtube.HomePage", id);
		insertApp(db, "com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail", id);
		insertApp(db, "com.android.email", "com.android.email.activity.Welcome", id);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
		if (oldVersion <= 11) {
			db.execSQL(AppCacheDao.getCreateTableScript());
		}
		if (oldVersion <= 13) {
			addColumn(db, AppCacheDao.TABLE_NAME, AppCacheDao.STARRED);
		}
		if (oldVersion <= 14) {
			db.delete(AppCacheDao.TABLE_NAME, null, null);
			addColumn(db, AppCacheDao.TABLE_NAME, AppCacheDao.PACKAGE_NAME);
		}
		if (oldVersion <= 18) {
			addColumn(db, LabelDao.TABLE_NAME, LabelDao.IMAGE);
		}
		if (oldVersion <= 19) {
			addColumn(db, AppLabelDao.TABLE_NAME, AppLabelDao.PACKAGE);
		}
		if (oldVersion <= 20) {
			addPackages(db);
		}
		// if (oldVersion <= 22) {
		// db.delete(AppCacheDao.TABLE_NAME, null, null);
		// }
		if (oldVersion <= 23) {
			addColumn(db, AppCacheDao.TABLE_NAME, AppCacheDao.IMAGE);
		}
		if (oldVersion <= 24) {
			addColumn(db, AppCacheDao.TABLE_NAME, AppCacheDao.DISABLED);
		}
		// db.execSQL(appsLabelDao.getDropTableScript());
		// db.execSQL(labelDao.getDropTableScript());
		// onCreate(db);
	}

	public void addPackages() {
		addPackages(db);
	}

	private void addPackages(SQLiteDatabase db) {
		Cursor query = db.query(AppLabelDao.TABLE_NAME, new String[] { "_id", AppLabelDao.APP_COL_NAME }, AppLabelDao.PACKAGE_NAME_COL_NAME
				+ " is null", null, null, null, null);
		try {
			while (query.moveToNext()) {
				Cursor c = db.query(AppCacheDao.TABLE_NAME, new String[] { AppCacheDao.PACKAGE_NAME_COL_NAME }, AppCacheDao.NAME_COL_NAME
						+ "=?", new String[] { query.getString(1) }, null, null, null);
				try {
					if (c.moveToNext()) {
						ContentValues contentValues = new ContentValues();
						contentValues.put(AppLabelDao.PACKAGE_NAME_COL_NAME, c.getString(0));
						db.update(AppLabelDao.TABLE_NAME, contentValues, "_id=?", new String[] { Long.toString(query.getLong(0)) });
					} else {
						db.delete(AppLabelDao.TABLE_NAME, "_id=?", new String[] { Long.toString(query.getLong(0)) });
					}
				} finally {
					c.close();
				}
			}
		} finally {
			query.close();
		}
		//
		// db.execSQL("update " + AppLabelDao.TABLE_NAME + " set " +
		// AppLabelDao.PACKAGE_NAME_COL_NAME + "=(select min(ac.package) from "
		// + AppCacheDao.TABLE_NAME + " ac where ac." +
		// AppCacheDao.NAME_COL_NAME + "=" + AppLabelDao.APP_COL_NAME +
		// ") where "
		// + AppLabelDao.PACKAGE_NAME_COL_NAME + " is null");
	}

	private boolean addColumn(SQLiteDatabase db, String tableName, DbColumns column) {
		// add column only if does't exists
		Cursor c = null;
		try {
			c = db.query(tableName, new String[] { column.getName() }, null, null, null, null, null);
			c.close();
			return false;
		} catch (Exception e) {
			if (c != null) {
				c.close();
			}
			db.execSQL("alter table " + tableName + " add " + column.getName() + ' ' + column.getDescription());
			return true;
		}
	}

	private long insertLabel(SQLiteDatabase db, Long id, String value, Integer icon) {
		ContentValues v = new ContentValues();
		v.put(LabelDao.LABEL.getName(), value);
		v.put(LabelDao.ICON.getName(), icon);
		if (id != null) {
			v.put(LabelDao.ID_COL_NAME, id);
		}
		return db.insert(LabelDao.TABLE_NAME, null, v);
	}

	private void insertApp(SQLiteDatabase db, String packageName, String value, long labelId) {
		ContentValues v = new ContentValues();
		v.put(AppLabelDao.APP.getName(), value);
		v.put(AppLabelDao.LABEL_ID.getName(), labelId);
		v.put(AppLabelDao.PACKAGE_NAME_COL_NAME, packageName);
		db.insert(AppLabelDao.TABLE_NAME, null, v);
	}

	public SQLiteDatabase getDb() {
		return db;
	}
}