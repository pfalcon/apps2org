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
import android.database.Cursor;

import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.model.AppLabel;

public class AppLabelDao extends ObjectWithIdDao<AppLabel> {

	public static final String APP_COL_NAME = "app";

	private static final String LABEL_ID_COL_NAME = "id_label";

	public static final String PACKAGE_NAME_COL_NAME = "package";

	public static final String TABLE_NAME = "apps_labels";

	private static final String[] COLS_STRING = new String[] { ID_COL_NAME, APP_COL_NAME, LABEL_ID_COL_NAME, PACKAGE_NAME_COL_NAME };

	public static final DbColumns APP = new DbColumns(APP_COL_NAME, "text not null");
	public static final DbColumns LABEL_ID = new DbColumns(LABEL_ID_COL_NAME, "integer not null");
	public static final DbColumns PACKAGE = new DbColumns(PACKAGE_NAME_COL_NAME, "text null");

	private static final DbColumns[] DB_COLUMNS = new DbColumns[] { ID, APP, LABEL_ID, PACKAGE };

	AppLabelDao() {
		super(TABLE_NAME);
		columns = DB_COLUMNS;
	}

	public long merge(String packageName, String app, long labelId) {
		Cursor c = db.query(TABLE_NAME, new String[] { ID_COL_NAME }, APP_COL_NAME + "=? and " + PACKAGE_NAME_COL_NAME + "=? and "
				+ LABEL_ID_COL_NAME + "=?", new String[] { app, packageName, Long.toString(labelId) }, null, null, null);
		try {
			if (!c.moveToNext()) {
				return insert(packageName, app, labelId);
			} else {
				return -1;
			}
		} finally {
			c.close();
		}
	}

	public long insert(String packageName, String app, long labelId) {
		ContentValues v = new ContentValues();
		v.put(APP_COL_NAME, app);
		v.put(LABEL_ID_COL_NAME, labelId);
		v.put(PACKAGE_NAME_COL_NAME, packageName);
		return db.insert(name, null, v);
	}

	@Override
	protected AppLabel createObject(Cursor c) {
		AppLabel t = new AppLabel();
		t.setId(c.getLong(0));
		t.setApp(c.getString(1));
		t.setLabelId(c.getLong(2));
		t.setPackageName(c.getString(3));
		return t;
	}

	public int delete(String packageName, String appName, Long labelId) {
		return db.delete(name, LABEL_ID_COL_NAME + " = ? and " + APP_COL_NAME + " = ? and " + PACKAGE_NAME_COL_NAME + "=?", new String[] {
				labelId.toString(), appName, packageName });
	}

	public int deleteAppsOfLabel(Long labelId) {
		return db.delete(name, LABEL_ID_COL_NAME + " = ?", new String[] { labelId.toString() });
	}

	@Override
	protected ContentValues createContentValue(AppLabel obj) {
		ContentValues v = new ContentValues();
		v.put(ID_COL_NAME, obj.getId());
		v.put(APP_COL_NAME, obj.getApp());
		v.put(LABEL_ID_COL_NAME, obj.getLabelId());
		v.put(PACKAGE_NAME_COL_NAME, obj.getPackageName());
		return v;
	}

	public static String getCreateTableScript() {
		return getCreateTableScript(TABLE_NAME, DB_COLUMNS);
	}

	public void removeUninstalledApps(boolean[] installedApps, String[] appNames) {
		for (int i = 0; i < installedApps.length; i++) {
			if (!installedApps[i]) {
				String a = appNames[i];
				int ind = a.indexOf(AppCacheMap.SEPARATOR);
				db.delete(TABLE_NAME, APP_COL_NAME + " = ? and " + PACKAGE_NAME_COL_NAME + "=?", new String[] { a.substring(ind + 1),
						a.substring(0, ind) });
			}
		}
	}

	public void removePackage(String packageName) {
		db.delete(TABLE_NAME, PACKAGE_NAME_COL_NAME + "=?", new String[] { packageName });
	}

	public String getLabelListString(String packageName, String name) {
		Cursor c = db.rawQuery("select l.label from labels l inner join apps_labels al "
				+ "on l._id = al.id_label where al.package = ? and al.app = ? order by upper(l.label)", new String[] { packageName, name });
		StringBuilder b = new StringBuilder();
		try {
			while (c.moveToNext()) {
				if (b.length() != 0) {
					b.append(", ");
				}
				b.append(c.getString(0));
			}
		} finally {
			c.close();
		}
		return b.toString();
	}
}
