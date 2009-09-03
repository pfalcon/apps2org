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

	public static final DbColumns<Label> LABEL = new DbColumns<Label>(LABEL_COL_NAME, "text not null unique") {
		@Override
		public void populateObject(Label obj, Cursor c) {
			obj.setName(getString(c));
		}

		@Override
		public void populateContent(Label obj, ContentValues c) {
			c.put(name, obj.getName());
		}
	};

	public static final DbColumns<Label> ICON = new DbColumns<Label>(ICON_COL_NAME, "integer") {
		@Override
		public void populateObject(Label obj, Cursor c) {
			obj.setIconDb(getInt(c));
		}

		@Override
		public void populateContent(Label obj, ContentValues c) {
			c.put(name, obj.getIconDb());
		}
	};

	LabelDao() {
		super(NAME);
		addColumn(LABEL);
		addColumn(ICON);
	}

	@Override
	public Label createNewObject() {
		return new Label();
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

	public TreeMap<Long, Label> getLabelsTreeMap() {
		TreeMap<Long, Label> m = new TreeMap<Long, Label>();
		ArrayList<Label> labels = getLabels();
		for (Label label : labels) {
			m.put(label.getId(), label);
		}
		return m;
	}

	public ArrayList<Label> getLabels() {
		Cursor c = db.query(name, COLS_STRING, null, null, null, null, ("upper(" + LABEL.getName() + ")"));
		ArrayList<Label> l = new ArrayList<Label>(c.getColumnCount());
		try {
			while (c.moveToNext()) {
				l.add(createObject(c));
			}
		} finally {
			c.close();
		}
		return l;
	}

	public Cursor getLabelsCursor() {
		return query(columns, null, "upper(" + LABEL + ")", null, null);
	}

	public long insert(String label) {
		Label obj = new Label();
		obj.setName(label);
		return insert(obj);
	}

	public Label getLabel(String name) {
		return queryForObject(columns, LABEL, name, null, null);
	}

	@Override
	public Label queryById(Long id) {
		Cursor c = db.query(name, COLS_STRING, ID_COL_NAME + "=?", new String[] { id.toString() }, null, null, null);
		try {
			if (c.moveToNext()) {
				return createObject(c);
			}
		} finally {
			c.close();
		}
		return null;
	}

	@Override
	protected Label createObject(Cursor c) {
		Label t = new Label();
		t.setId(c.getLong(0));
		t.setName(c.getString(1));
		t.setIconDb(c.getInt(2));
		return t;
	}

}
