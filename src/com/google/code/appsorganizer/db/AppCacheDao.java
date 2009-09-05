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

import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.code.appsorganizer.model.AppCache;

public class AppCacheDao extends ObjectWithIdDao<AppCache> {

	private static final String LABEL_COL_NAME = "label";

	private static final String NAME_COL_NAME = "name";

	public static final String IGNORED_COL_NAME = "ignored";

	public static final String STARRED_COL_NAME = "starred";

	public static final String TABLE_NAME = "apps";

	public static final DbColumns NAME = new DbColumns(NAME_COL_NAME, "text not null");
	public static final DbColumns LABEL = new DbColumns(LABEL_COL_NAME, "text not null");
	public static final DbColumns STARRED = new DbColumns(STARRED_COL_NAME, "integer not null default 0");
	public static final DbColumns IGNORED = new DbColumns(IGNORED_COL_NAME, "integer not null default 0");

	AppCacheDao() {
		super(TABLE_NAME);
		columns = new DbColumns[] { ID, NAME, LABEL, STARRED, IGNORED };
	}

	public HashMap<String, AppCache> queryForCacheMap() {
		Cursor c = db.query(false, name, new String[] { NAME_COL_NAME, LABEL_COL_NAME, STARRED_COL_NAME, IGNORED_COL_NAME }, null, null,
				null, null, null, null);
		return convertCursorToCacheMap(c);
	}

	protected HashMap<String, AppCache> convertCursorToCacheMap(Cursor c) {
		HashMap<String, AppCache> m = new HashMap<String, AppCache>(c.getCount());
		try {
			while (c.moveToNext()) {
				AppCache a = new AppCache();
				String name = c.getString(0);
				a.setName(name);
				a.setLabel(c.getString(1));
				a.setStarred(c.getInt(2) == 1);
				a.setIgnored(c.getInt(3) == 1);
				m.put(name, a);
			}
		} finally {
			c.close();
		}
		return m;
	}

	public void updateStarred(String app, boolean starred) {
		ContentValues v = new ContentValues();
		v.put(STARRED_COL_NAME, starred);
		db.update(name, v, NAME_COL_NAME + " = ?", new String[] { app });
	}

	public void updateIgnored(String app, boolean ignored) {
		ContentValues v = new ContentValues();
		v.put(IGNORED_COL_NAME, ignored);
		db.update(name, v, NAME_COL_NAME + " = ?", new String[] { app });
	}

	@Override
	protected AppCache createObject(Cursor c) {
		AppCache t = new AppCache();
		t.setId(c.getLong(0));
		t.setName(c.getString(1));
		t.setLabel(c.getString(2));
		t.setStarred(c.getInt(3) == 1);
		t.setIgnored(c.getInt(4) == 1);
		return t;
	}

	@Override
	protected ContentValues createContentValue(AppCache obj) {
		ContentValues v = new ContentValues();
		v.put(ID_COL_NAME, obj.getId());
		v.put(NAME_COL_NAME, obj.getName());
		v.put(LABEL_COL_NAME, obj.getLabel());
		v.put(STARRED_COL_NAME, obj.isStarred() ? 1 : 0);
		v.put(IGNORED_COL_NAME, obj.isIgnored() ? 1 : 0);
		return v;
	}
}
