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
import com.google.code.appsorganizer.model.AppCache;

public class AppCacheDao extends ObjectWithIdDao<AppCache> {

	private static final String LABEL_COL_NAME = "label";

	public static final String NAME_COL_NAME = "name";

	public static final String PACKAGE_NAME_COL_NAME = "package";

	public static final String STARRED_COL_NAME = "starred";

	private static final String[] ALL_COLUMNS = new String[] { NAME_COL_NAME, LABEL_COL_NAME, STARRED_COL_NAME, PACKAGE_NAME_COL_NAME };

	public static final String TABLE_NAME = "apps";

	public static final DbColumns NAME = new DbColumns(NAME_COL_NAME, "text not null");
	public static final DbColumns LABEL = new DbColumns(LABEL_COL_NAME, "text not null");
	public static final DbColumns STARRED = new DbColumns(STARRED_COL_NAME, "integer not null default 0");
	public static final DbColumns PACKAGE_NAME = new DbColumns(PACKAGE_NAME_COL_NAME, "text");

	private static final DbColumns[] DB_COLUMNS = new DbColumns[] { ID, NAME, LABEL, STARRED, PACKAGE_NAME };

	AppCacheDao() {
		super(TABLE_NAME);
		columns = DB_COLUMNS;
	}

	public AppCacheMap queryForCacheMap() {
		Cursor c = db.query(name, ALL_COLUMNS, null, null, null, null, NAME_COL_NAME);
		return convertCursorToCacheMap(c);
	}

	protected AppCacheMap convertCursorToCacheMap(Cursor c) {
		AppCache[] v = new AppCache[c.getCount()];
		try {
			int i = 0;
			while (c.moveToNext()) {
				AppCache a = new AppCache(c.getString(3), c.getString(0), c.getString(1));
				a.starred = c.getInt(2) == 1;
				v[i++] = a;
			}
		} finally {
			c.close();
		}
		return new AppCacheMap(v);
	}

	public void updateStarred(String packageName, String app, boolean starred) {
		ContentValues v = new ContentValues();
		v.put(STARRED_COL_NAME, starred);
		db.update(name, v, NAME_COL_NAME + " = ? and " + PACKAGE_NAME_COL_NAME + "=?", new String[] { app, packageName });
	}

	public void clearStarred() {
		ContentValues v = new ContentValues();
		v.put(STARRED_COL_NAME, false);
		db.update(name, v, null, null);
	}

	@Override
	protected AppCache createObject(Cursor c) {
		AppCache t = new AppCache(c.getString(5), c.getString(1), c.getString(2));
		t.setId(c.getLong(0));
		t.starred = c.getInt(3) == 1;
		return t;
	}

	@Override
	protected ContentValues createContentValue(AppCache obj) {
		ContentValues v = new ContentValues();
		v.put(ID_COL_NAME, obj.getId());
		v.put(NAME_COL_NAME, obj.name);
		v.put(LABEL_COL_NAME, obj.label);
		v.put(STARRED_COL_NAME, obj.starred ? 1 : 0);
		v.put(PACKAGE_NAME_COL_NAME, obj.packageName);
		return v;
	}

	public static String getCreateTableScript() {
		return getCreateTableScript(TABLE_NAME, DB_COLUMNS);
	}

	public void updateLabel(String p, String n, String l) {
		ContentValues v = new ContentValues();
		v.put(LABEL_COL_NAME, l);
		db.update(name, v, PACKAGE_NAME_COL_NAME + " = ? and " + NAME_COL_NAME + "=?", new String[] { p, n });
	}

	public void removeUninstalledApps(boolean[] installedApps, String[] appNames) {
		for (int i = 0; i < installedApps.length; i++) {
			if (!installedApps[i]) {
				db.delete(name, NAME_COL_NAME + " = ?", new String[] { appNames[i] });
			}
		}
	}
}
