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
import java.util.TreeMap;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.code.appsorganizer.model.Label;

public class LabelDao extends ObjectWithIdDao<Label> {

	private static final String ICON_COL_NAME = "icon";

	private static final String LABEL_COL_NAME = "label";

	public static final String NAME = "labels";

	private static final String[] COLS_STRING = new String[] { ID_COL_NAME, LABEL_COL_NAME, ICON_COL_NAME };

	public static final DbColumns LABEL = new DbColumns(LABEL_COL_NAME, "text not null unique");

	public static final DbColumns ICON = new DbColumns(ICON_COL_NAME, "integer");

	LabelDao() {
		super(NAME);
		columns = new DbColumns[] { ID, LABEL, ICON };
	}

	public DoubleArray getAppsLabels() {
		Cursor c = db.rawQuery(
				"select al.app, l.label from labels l inner join apps_labels al on l._id = al.id_label order by al.app, l.label",
				new String[] {});
		int tot = c.getCount();
		String[] keys = new String[tot];
		String[] values = new String[tot];
		int pos = 0;
		try {
			// c.moveToFirst();
			while (c.moveToNext()) {
				keys[pos] = c.getString(0);
				values[pos++] = c.getString(1);
			}
		} finally {
			c.close();
		}
		return new DoubleArray(keys, values);
	}

	public DoubleArray getAppsLabelsConcat() {
		Cursor c = db.rawQuery(
				"select al.app, l.label from labels l inner join apps_labels al on l._id = al.id_label order by al.app, l.label",
				new String[] {});
		int tot = c.getCount();
		String[] keys = new String[tot];
		String[] values = new String[tot];
		int pos = 0;
		StringBuilder b = new StringBuilder();
		String curApp = null;
		try {
			while (c.moveToNext()) {
				String appName = c.getString(0);
				String label = c.getString(1);
				if (appName.equals(curApp)) {
					b.append(", ");
					b.append(label);
				} else {
					if (curApp != null) {
						keys[pos] = curApp;
						values[pos++] = b.toString();
					}
					curApp = appName;
					b = new StringBuilder(label);
				}
			}
			keys[pos] = curApp;
			values[pos++] = b.toString();
		} finally {
			c.close();
		}
		return new DoubleArray(keys, values);
	}

	public TreeMap<Long, Label> getLabelsTreeMap() {
		TreeMap<Long, Label> m = new TreeMap<Long, Label>();
		Label[] labels = getLabelsArray();
		for (int i = 0; i < labels.length; i++) {
			Label label = labels[i];
			m.put(label.getId(), label);
		}
		return m;
	}

	public ArrayList<Label> getLabels() {
		Cursor c = db.query(name, COLS_STRING, null, null, null, null, "upper(" + LABEL.getName() + ")");
		return convertCursorToList(c);
	}

	public Label[] getLabelsArray() {
		Cursor c = db.query(name, COLS_STRING, null, null, null, null, ("upper(" + LABEL.getName() + ")"));
		return convertCursorToArray(c, new Label[c.getCount()]);
	}

	public long insert(String label) {
		ContentValues v = new ContentValues();
		v.put(LABEL_COL_NAME, label);
		return db.insert(name, null, v);
	}

	@Override
	protected Label createObject(Cursor c) {
		Label t = new Label();
		t.setId(c.getLong(0));
		t.setName(c.getString(1));
		t.setIconDb(c.getInt(2));
		return t;
	}

	@Override
	protected ContentValues createContentValue(Label obj) {
		ContentValues v = new ContentValues();
		v.put(ID_COL_NAME, obj.getId());
		v.put(LABEL_COL_NAME, obj.getLabel());
		v.put(ICON_COL_NAME, obj.getIconDb());
		return v;
	}

}
