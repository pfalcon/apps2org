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
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.code.appsorganizer.AppLabelBinding;
import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.model.Label;

public class LabelDao extends ObjectWithIdDao<Label> {

	public static final String ICON_COL_NAME = "icon";

	public static final String LABEL_COL_NAME = "label";

	public static final String TABLE_NAME = "labels";

	public static final String IMAGE_COL_NAME = "image";

	public static final String[] COLS_STRING = new String[] { ID_COL_NAME, LABEL_COL_NAME, ICON_COL_NAME, IMAGE_COL_NAME };

	public static final DbColumns LABEL = new DbColumns(LABEL_COL_NAME, "text not null unique");

	public static final DbColumns ICON = new DbColumns(ICON_COL_NAME, "integer");

	public static final DbColumns IMAGE = new DbColumns(IMAGE_COL_NAME, "blob");

	private static final DbColumns[] DB_COLUMNS = new DbColumns[] { ID, LABEL, ICON, IMAGE };

	LabelDao() {
		super(TABLE_NAME);
		columns = DB_COLUMNS;
	}

	public DoubleArray getAppsLabels() {
		Cursor c = db
				.rawQuery(
						"select al.app, l.label, al.package from labels l inner join apps_labels al on l._id = al.id_label order by al.package, al.app, l.label",
						new String[] {});
		int tot = c.getCount();
		String[] keys = new String[tot];
		String[] values = new String[tot];
		int pos = 0;
		try {
			while (c.moveToNext()) {
				keys[pos] = c.getString(2) + AppCacheMap.SEPARATOR + c.getString(0);
				values[pos++] = c.getString(1);
			}
		} finally {
			c.close();
		}
		return new DoubleArray(keys, values, null);
	}

	public ArrayList<Label> getLabels() {
		Cursor c = getLabelCursor();
		return convertCursorToList(c);
	}

	public Label[] getLabelsArray() {
		Cursor c = getLabelCursor();
		return convertCursorToArray(c, new Label[c.getCount()]);
	}

	public Map<String, Long> getLabelsMap() {
		Cursor c = db.query(TABLE_NAME, new String[] { ID_COL_NAME, LABEL_COL_NAME }, null, null, null, null, null);
		Map<String, Long> map = new HashMap<String, Long>(c.getCount());
		try {
			while (c.moveToNext()) {
				map.put(c.getString(1), c.getLong(0));
			}
		} finally {
			c.close();
		}
		return map;
	}

	public Cursor getLabelCursor() {
		return db.query(TABLE_NAME, COLS_STRING, null, null, null, null, ("upper(" + LABEL_COL_NAME + ")"));
	}

	public ArrayList<AppLabelBinding> getAppsLabelList(String packageName, String name) {
		Cursor c = db.rawQuery("select l._ID, l.label, case when b._id is null then 0 else 1 end as checked from labels l"
				+ " left outer join apps_labels b on l._id = b.id_label and b.package = ? and b.app = ? " + "order by checked desc, upper(l.label)",
				new String[] { packageName, name });
		ArrayList<AppLabelBinding> l = new ArrayList<AppLabelBinding>(c.getCount());
		try {
			while (c.moveToNext()) {
				boolean checked = c.getInt(2) == 1;
				AppLabelBinding a = new AppLabelBinding(c.getString(1), c.getLong(0), checked);
				a.checked = checked;
				l.add(a);
			}
		} finally {
			c.close();
		}
		return l;
	}

	public long insert(String label) {
		ContentValues v = new ContentValues();
		v.put(LABEL_COL_NAME, label);
		return db.insert(name, null, v);
	}

	public long insert(String label, int icon) {
		ContentValues v = new ContentValues();
		v.put(LABEL_COL_NAME, label);
		v.put(ICON_COL_NAME, icon);
		return db.insert(name, null, v);
	}

	@Override
	protected Label createObject(Cursor c) {
		Label t = new Label();
		t.setId(c.getLong(0));
		t.setName(c.getString(1));
		t.setIconDb(c.getInt(2));
		t.setImageBytes(c.getBlob(3));
		return t;
	}

	@Override
	protected ContentValues createContentValue(Label obj) {
		ContentValues v = new ContentValues();
		v.put(ID_COL_NAME, obj.getId());
		v.put(LABEL_COL_NAME, obj.getLabel());
		v.put(ICON_COL_NAME, obj.getIconDb());
		v.put(IMAGE_COL_NAME, obj.getImageBytes());
		return v;
	}

	public static String getCreateTableScript() {
		return getCreateTableScript(TABLE_NAME, DB_COLUMNS);
	}

	public long updateName(Long id, String name) {
		ContentValues c = new ContentValues();
		c.put(LABEL_COL_NAME, name);
		return db.update(TABLE_NAME, c, "_id = ?", new String[] { id.toString() });
	}

	public long updateIcon(Long id, Integer icon, byte[] image) {
		ContentValues c = new ContentValues();
		c.put(ICON_COL_NAME, icon);
		c.put(IMAGE_COL_NAME, image);
		return db.update(TABLE_NAME, c, "_id = ?", new String[] { id.toString() });
	}

	public boolean labelAlreadyExists(String name) {
		Cursor c = db.query(TABLE_NAME, new String[] { ID_COL_NAME }, LABEL_COL_NAME + "=?", new String[] { name }, null, null, null);
		try {
			return c.moveToNext();
		} finally {
			c.close();
		}
	}
}
