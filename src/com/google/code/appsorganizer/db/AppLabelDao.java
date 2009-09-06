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

import com.google.code.appsorganizer.model.AppLabel;

public class AppLabelDao extends ObjectWithIdDao<AppLabel> {

	private static final String APP_COL_NAME = "app";

	private static final String LABEL_ID_COL_NAME = "id_label";

	public static final String NAME = "apps_labels";

	private static final String[] COLS_STRING = new String[] { ID_COL_NAME, APP_COL_NAME, LABEL_ID_COL_NAME };

	public static final DbColumns APP = new DbColumns(APP_COL_NAME, "text not null");
	public static final DbColumns LABEL_ID = new DbColumns(LABEL_ID_COL_NAME, "integer not null");

	AppLabelDao() {
		super(NAME);
		columns = new DbColumns[] { ID, APP, LABEL_ID };
	}

	public long insert(String app, long labelId) {
		ContentValues v = new ContentValues();
		v.put(APP_COL_NAME, app);
		v.put(LABEL_ID_COL_NAME, labelId);
		return db.insert(name, null, v);
	}

	@Override
	protected AppLabel createObject(Cursor c) {
		AppLabel t = new AppLabel();
		t.setId(c.getLong(0));
		t.setApp(c.getString(1));
		t.setLabelId(c.getLong(2));
		return t;
	}

	public AppLabel[] getApps(String app) {
		Cursor c = db.query(name, COLS_STRING, APP_COL_NAME + "=?", new String[] { app }, null, null, null);
		return convertCursorToArray(c, new AppLabel[c.getCount()]);
	}

	public Cursor getAppsCursor(long labelId, boolean starred) {
		// return
		// db.rawQuery("select a._id, null image, a.label, a.package, a.name from apps a inner join apps_labels al "
		// + "on a.name = al.app order by name", null);
		return db.rawQuery("select a.label, a.package, a.name from apps a inner join apps_labels al "
				+ "on a.name = al.app where id_label = ? " + (starred ? "and a.starred = 1" : "") + " order by a.label",
				new String[] { Long.toString(labelId) });
	}

	public int delete(String appName, Long labelId) {
		return db.delete(name, LABEL_ID_COL_NAME + " = ? and " + APP_COL_NAME + " = ?", new String[] { labelId.toString(), appName });
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
		return v;
	}

}
