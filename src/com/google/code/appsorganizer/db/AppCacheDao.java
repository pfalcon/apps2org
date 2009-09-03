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

	public static final DbColumns<AppCache> NAME = new DbColumns<AppCache>(NAME_COL_NAME, "text not null") {
		@Override
		public void populateObject(AppCache obj, android.database.Cursor c) {
			obj.setName(getString(c));
		}

		@Override
		public void populateContent(AppCache obj, ContentValues c) {
			c.put(name, obj.getName());
		}
	};
	public static final DbColumns<AppCache> LABEL = new DbColumns<AppCache>(LABEL_COL_NAME, "text not null") {
		@Override
		public void populateObject(AppCache obj, android.database.Cursor c) {
			obj.setLabel(getString(c));
		}

		@Override
		public void populateContent(AppCache obj, ContentValues c) {
			c.put(name, obj.getLabel());
		}
	};
	public static final DbColumns<AppCache> STARRED = new DbColumns<AppCache>(STARRED_COL_NAME, "integer not null default 0") {
		@Override
		public void populateObject(AppCache obj, android.database.Cursor c) {
			obj.setStarred(getInt(c) != 0);
		}

		@Override
		public void populateContent(AppCache obj, ContentValues c) {
			c.put(name, obj.isStarred());
		}
	};
	public static final DbColumns<AppCache> IGNORED = new DbColumns<AppCache>(IGNORED_COL_NAME, "integer not null default 0") {
		@Override
		public void populateObject(AppCache obj, android.database.Cursor c) {
			obj.setIgnored(getInt(c) != 0);
		}

		@Override
		public void populateContent(AppCache obj, ContentValues c) {
			c.put(name, obj.isIgnored());
		}
	};

	AppCacheDao() {
		super(TABLE_NAME);
		addColumn(NAME);
		addColumn(LABEL);
	}

	@Override
	public AppCache createNewObject() {
		return new AppCache();
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

	public String[] getIgnoredApps() {
		Cursor c = db.query(true, name, new String[] { NAME_COL_NAME }, IGNORED_COL_NAME + "=1", null, null, null, null, null);
		String[] l = new String[c.getCount()];
		try {
			int i = 0;
			while (c.moveToNext()) {
				l[i++] = c.getString(0);
			}
		} finally {
			c.close();
		}
		return l;
	}
}
