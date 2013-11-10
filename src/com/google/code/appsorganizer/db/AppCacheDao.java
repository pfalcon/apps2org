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

import java.util.ArrayList;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.code.appsorganizer.ApplicationInfoManager;
import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.model.AppCache;

public class AppCacheDao extends ObjectWithIdDao<AppCache> {

	public static final String LABEL_COL_NAME = "label";

	public static final String NAME_COL_NAME = "name";

	public static final String PACKAGE_NAME_COL_NAME = "package";

	public static final String STARRED_COL_NAME = "starred";

	public static final String IMAGE_COL_NAME = "image";

	public static final String DISABLED_COL_NAME = "disabled";

	private static final String[] COLUMNS_WITH_ID = new String[] { NAME_COL_NAME, LABEL_COL_NAME, STARRED_COL_NAME, PACKAGE_NAME_COL_NAME,
			IMAGE_COL_NAME, DISABLED_COL_NAME, ID_COL_NAME };

	public static final String TABLE_NAME = "apps";

	public static final DbColumns NAME = new DbColumns(NAME_COL_NAME, "text not null");
	public static final DbColumns LABEL = new DbColumns(LABEL_COL_NAME, "text not null");
	public static final DbColumns STARRED = new DbColumns(STARRED_COL_NAME, "integer not null default 0");
	public static final DbColumns PACKAGE_NAME = new DbColumns(PACKAGE_NAME_COL_NAME, "text");
	public static final DbColumns IMAGE = new DbColumns(IMAGE_COL_NAME, "blob");
	public static final DbColumns DISABLED = new DbColumns(DISABLED_COL_NAME, "integer not null default 0");

	private static final DbColumns[] DB_COLUMNS = new DbColumns[] { ID, NAME, LABEL, STARRED, PACKAGE_NAME, IMAGE, DISABLED };

	public static final long OTHER_LABEL_ID = -1l;

	AppCacheDao() {
		super(TABLE_NAME);
		columns = DB_COLUMNS;
	}

	public AppCacheMap queryForCacheMap(boolean hideDisabled) {
		Cursor c = db.query(name, COLUMNS_WITH_ID, hideDisabled ? "disabled = 0" : null, null, null, null, PACKAGE_NAME_COL_NAME + ","
				+ NAME_COL_NAME);
		AppCache[] v = new AppCache[c.getCount()];
		try {
			int i = 0;
			while (c.moveToNext()) {
				v[i++] = createAppCache(c);
			}
		} finally {
			c.close();
		}
		return new AppCacheMap(v);
	}

	private AppCache createAppCache(Cursor c) {
		AppCache a = new AppCache(c.getString(3), c.getString(0), c.getString(1));
		a.starred = c.getInt(2) == 1;
		a.image = c.getBlob(4);
		a.disabled = c.getInt(5) == 1;
		a.setId(c.getLong(6));
		return a;
	}

	public AppCache queryForAppCache(String packageName, String name, boolean hideDisabled, boolean loadIcon) {
		String filter = PACKAGE_NAME_COL_NAME + "=? and " + NAME_COL_NAME + "=?";
		if (hideDisabled) {
			filter += " and " + DISABLED_COL_NAME + "=0";
		}
		if (loadIcon) {
			Cursor c = db.query(TABLE_NAME, COLUMNS_WITH_ID, filter, new String[] { packageName, name }, null, null, null);
			try {
				if (c.moveToNext()) {
					return createAppCache(c);
				}
			} finally {
				c.close();
			}
		} else {
			Cursor c = db.query(TABLE_NAME, new String[] { NAME_COL_NAME, LABEL_COL_NAME, STARRED_COL_NAME, PACKAGE_NAME_COL_NAME, DISABLED_COL_NAME,
					ID_COL_NAME }, filter, new String[] { packageName, name }, null, null, null);
			try {
				if (c.moveToNext()) {
					AppCache a = new AppCache(c.getString(3), c.getString(0), c.getString(1));
					a.starred = c.getInt(2) == 1;
					a.disabled = c.getInt(4) == 1;
					a.setId(c.getLong(5));
					return a;
				}
			} finally {
				c.close();
			}
		}
		return null;
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
		AppCache t = new AppCache(c.getString(4), c.getString(1), c.getString(2));
		t.setId(c.getLong(0));
		t.starred = c.getInt(3) == 1;
		t.disabled = c.getInt(6) == 1;
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
		v.put(DISABLED_COL_NAME, obj.disabled ? 1 : 0);
		return v;
	}

	public static String getCreateTableScript() {
		return getCreateTableScript(TABLE_NAME, DB_COLUMNS);
	}

	public void updateLabel(String p, String n, String l, byte[] img, boolean disabled) {
		ContentValues v = new ContentValues();
		v.put(LABEL_COL_NAME, l);
		v.put(IMAGE_COL_NAME, img);
		v.put(DISABLED_COL_NAME, disabled ? 1 : 0);
		db.update(name, v, PACKAGE_NAME_COL_NAME + " = ? and " + NAME_COL_NAME + "=?", new String[] { p, n });
	}

	public void removeUninstalledApps(StringBuffer installedIds) {
		ContentValues v = new ContentValues();
		v.put(DISABLED_COL_NAME, 1);
		db.update(TABLE_NAME, v, DISABLED_COL_NAME + "=0 and " + ID_COL_NAME + " not in (" + installedIds + ")", null);
	}

	public void removeUninstalledApps(boolean[] installedApps, AppCacheMap nameCache) {
		String[] keys = nameCache.keys();
		for (int i = 0; i < installedApps.length; i++) {
			if (!installedApps[i]) {
				AppCache app = nameCache.getAt(i);
				if (!app.disabled) {
					String a = keys[i];
					int ind = a.indexOf(AppCacheMap.SEPARATOR);
					String packageName = a.substring(0, ind);
					String appName = a.substring(ind + 1);
					disablePackage(packageName, appName, true);
				}
			}
		}
	}

	public int enablePackage(Context context, String packageName) {
		String filter = PACKAGE_NAME_COL_NAME + "=?";
		String[] args = new String[] { packageName };
		Cursor cur = db.query(TABLE_NAME, new String[] { ID_COL_NAME, NAME_COL_NAME }, filter, args, null, null, null);
		try {
			int count = cur.getCount();
			if (count > 0) {
				ArrayList<String> activities = ApplicationInfoManager.getAllActivityNames(context.getPackageManager(), packageName);
				int tot = 0;
				while (cur.moveToNext()) {
					String name = cur.getString(1);
					if (activities.contains(name)) {
						tot++;
						ContentValues c = new ContentValues(1);
						c.put(DISABLED_COL_NAME, 0);
						db.update(TABLE_NAME, c, PACKAGE_NAME_COL_NAME + "=? and " + NAME_COL_NAME + "=?", new String[] { packageName, name });
					}
				}
				return tot;
			}
		} finally {
			cur.close();
		}
		return 0;
	}

	public int disablePackage(String packageName, boolean d) {
		ContentValues c = new ContentValues(1);
		c.put(DISABLED_COL_NAME, d);
		return db.update(TABLE_NAME, c, PACKAGE_NAME_COL_NAME + "=?", new String[] { packageName });
	}

	public int disablePackage(String packageName, String appName, boolean d) {
		ContentValues c = new ContentValues(1);
		c.put(DISABLED_COL_NAME, d);
		return db.update(TABLE_NAME, c, PACKAGE_NAME_COL_NAME + "=? and " + NAME_COL_NAME + "=?", new String[] { packageName, appName });
	}

	public static Cursor getAppsOfLabelCursor(SQLiteDatabase db, long labelId, boolean starredFirst, boolean onlyStarred) {
		return db.rawQuery("select a._id, a.label, a.image, a.package, a.name from apps a inner join apps_labels al "
				+ "on a.name = al.app and a.package = al.package where a.disabled = 0 and id_label = ? " + (onlyStarred ? "and a.starred = 1" : "")
				+ " order by " + (starredFirst ? "a.starred desc," : "") + "upper(a.label)", new String[] { Long.toString(labelId) });
	}

	public Cursor getAppsOfLabel(long labelId) {
		return db.rawQuery("select a._id, a.label, a.package, a.name, case when al._id is null then 0 else 1 end as checked"
				+ " from apps a left outer join apps_labels al on a.name = al.app and a.package = al.package and id_label = ? "
				+ " where a.disabled = 0 order by checked desc, upper(a.label)", new String[] { Long.toString(labelId) });
	}

	public HashSet<Long> getAppsOfLabelSet(long labelId) {
		HashSet<Long> set = new HashSet<Long>();
		Cursor c = db.rawQuery("select a._id from apps a inner join apps_labels al "
				+ "on a.name = al.app and a.package = al.package where id_label = ? and a.disabled = 0", new String[] { Long.toString(labelId) });
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
				+ "on a.name = al.app and a.package = al.package where a.disabled = 0 ";
		String orderBy = " order by upper(a.label)";
		if (label == OTHER_LABEL_ID) {
			return db.rawQuery(select + "and id_label is null" + orderBy, null);
		} else {
			return db.rawQuery(select + "and id_label=?" + orderBy, new String[] { label.toString() });
		}
	}

	public Cursor getAppsNoLabelCursor() {
		return db.rawQuery("select a.name, a.package, a.label from apps a left outer join apps_labels al "
				+ "on a.name = al.app and a.package = al.package where a.disabled = 0 and id_label is null order by upper(a.label)", null);
	}

	public Cursor getAllApps(String[] cols) {
		return db.query(TABLE_NAME, cols, "disabled=0", null, null, null, "upper(label)");
	}

	public void fixDuplicateApps() {
		Cursor c = db.rawQuery("select _id from apps a where a.disabled = 1 and "
				+ "exists(select 1 from apps a2 where a.package = a2.package and a.name = a2.name and a._id != a2._id)", null);
		if (c != null) {
			StringBuilder b = new StringBuilder();
			try {
				while (c.moveToNext()) {
					if (b.length() > 0) {
						b.append(',');
					}
					b.append(c.getLong(0));
				}
			} finally {
				c.close();
			}
			if (b.length() > 0) {
				db.delete(TABLE_NAME, "_id in (" + b.toString() + ")", null);
			}
		}
	}

}
