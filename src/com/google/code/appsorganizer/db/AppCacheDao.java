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

import java.util.HashSet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.model.AppCache;

public class AppCacheDao extends ObjectWithIdDao<AppCache> {

	public static final String LABEL_COL_NAME = "label";

	public static final String NAME_COL_NAME = "name";

	public static final String PACKAGE_NAME_COL_NAME = "package";

	public static final String STARRED_COL_NAME = "starred";

	public static final String IMAGE_COL_NAME = "image";

	private static final String[] ALL_COLUMNS = new String[] { NAME_COL_NAME, LABEL_COL_NAME, STARRED_COL_NAME, PACKAGE_NAME_COL_NAME,
			IMAGE_COL_NAME };

	public static final String TABLE_NAME = "apps";

	public static final DbColumns NAME = new DbColumns(NAME_COL_NAME, "text not null");
	public static final DbColumns LABEL = new DbColumns(LABEL_COL_NAME, "text not null");
	public static final DbColumns STARRED = new DbColumns(STARRED_COL_NAME, "integer not null default 0");
	public static final DbColumns PACKAGE_NAME = new DbColumns(PACKAGE_NAME_COL_NAME, "text");
	public static final DbColumns IMAGE = new DbColumns(IMAGE_COL_NAME, "blob");

	private static final DbColumns[] DB_COLUMNS = new DbColumns[] { ID, NAME, LABEL, STARRED, PACKAGE_NAME, IMAGE };

	public static final long OTHER_LABEL_ID = -1l;

	AppCacheDao() {
		super(TABLE_NAME);
		columns = DB_COLUMNS;
	}

	public AppCacheMap queryForCacheMap() {
		Cursor c = db.query(name, ALL_COLUMNS, null, null, null, null, PACKAGE_NAME_COL_NAME + "," + NAME_COL_NAME);
		AppCache[] v = new AppCache[c.getCount()];
		try {
			int i = 0;
			while (c.moveToNext()) {
				AppCache a = new AppCache(c.getString(3), c.getString(0), c.getString(1));
				a.starred = c.getInt(2) == 1;
				a.image = c.getBlob(4);
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
		v.put(IMAGE_COL_NAME, obj.image);
		return v;
	}

	public static String getCreateTableScript() {
		return getCreateTableScript(TABLE_NAME, DB_COLUMNS);
	}

	public void updateLabel(String p, String n, String l, byte[] img) {
		ContentValues v = new ContentValues();
		v.put(LABEL_COL_NAME, l);
		v.put(IMAGE_COL_NAME, img);
		db.update(name, v, PACKAGE_NAME_COL_NAME + " = ? and " + NAME_COL_NAME + "=?", new String[] { p, n });
	}

	public void removeUninstalledApps(boolean[] installedApps, String[] appNames) {
		for (int i = 0; i < installedApps.length; i++) {
			if (!installedApps[i]) {
				String a = appNames[i];
				int ind = a.indexOf(AppCacheMap.SEPARATOR);
				db.delete(TABLE_NAME, NAME_COL_NAME + " = ? and " + PACKAGE_NAME_COL_NAME + "=?", new String[] { a.substring(ind + 1),
						a.substring(0, ind) });
			}
		}
	}

	public void removePackage(String packageName) {
		db.delete(TABLE_NAME, PACKAGE_NAME_COL_NAME + "=?", new String[] { packageName });
	}

	public static Cursor getAppsOfLabelCursor(SQLiteDatabase db, long labelId, boolean starredFirst, boolean onlyStarred) {
		return db.rawQuery("select a._id, a.label, a.image, a.package, a.name from apps a inner join apps_labels al "
				+ "on a.name = al.app and a.package = al.package where id_label = ? " + (onlyStarred ? "and a.starred = 1" : "")
				+ " order by " + (starredFirst ? "a.starred desc," : "") + "upper(a.label)", new String[] { Long.toString(labelId) });
	}

	public Cursor getAppsOfLabel(long labelId) {
		return db.rawQuery("select a._id, a.label, a.package, a.name, case when al._id is null then 0 else 1 end as checked"
				+ " from apps a left outer join apps_labels al on a.name = al.app and a.package = al.package and id_label = ? "
				+ " order by checked desc, upper(a.label)", new String[] { Long.toString(labelId) });
	}

	public HashSet<Long> getAppsOfLabelSet(long labelId) {
		HashSet<Long> set = new HashSet<Long>();
		Cursor c = db.rawQuery("select a._id from apps a inner join apps_labels al "
				+ "on a.name = al.app and a.package = al.package where id_label = ?", new String[] { Long.toString(labelId) });
		try {
			while (c.moveToNext()) {
				set.add(c.getLong(0));
			}
		} finally {
			c.close();
		}
		return set;
	}

	public Cursor getAppsCursor(Long label) {
		String select = "select a._id, a.label, a.name, a.starred, a.image, a.package from apps a left outer join apps_labels al "
				+ "on a.name = al.app and a.package = al.package where ";
		String orderBy = " order by upper(a.label)";
		if (label == OTHER_LABEL_ID) {
			return db.rawQuery(select + "id_label is null" + orderBy, null);
		} else {
			return db.rawQuery(select + "id_label=?" + orderBy, new String[] { label.toString() });
		}
	}

	public Cursor getAllApps(String[] cols) {
		return db.query(TABLE_NAME, cols, null, null, null, null, "upper(label)");
	}
}
