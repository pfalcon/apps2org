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
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class DbDao<T> {

	protected final String name;

	protected SQLiteDatabase db;

	protected DbColumns[] columns;

	public DbDao(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public DbColumns[] getColumns() {
		return columns;
	}

	public static String getCreateTableScript(String name, DbColumns[] columns) {
		StringBuilder b = new StringBuilder("create table ");
		b.append(name);
		b.append(" (");
		boolean first = true;
		for (DbColumns c : columns) {
			if (first) {
				first = false;
			} else {
				b.append(',');
			}
			b.append(c.getName());
			b.append(' ');
			b.append(c.getDescription());
		}
		b.append(");");
		return b.toString();
	}

	public String getDropTableScript() {
		return "DROP TABLE IF EXISTS " + name;
	}

	public T queryForObject(DbColumns[] cols, DbColumns filterCol, String filterValue, String groupBy, String having) {
		Cursor c = db.query(name, columnsToStringArray(cols), filterCol.getName() + "=?", new String[] { filterValue }, groupBy, having,
				null);
		return convertCursorToObject(c);
	}

	protected List<String> convertCursorToStringList(Cursor c, DbColumns col) {
		List<String> l = new ArrayList<String>();
		try {
			while (c.moveToNext()) {
				l.add(c.getString(0));
			}
		} finally {
			c.close();
		}
		return l;
	}

	protected HashMap<String, String> convertCursorToStringMap(Cursor c) {
		HashMap<String, String> m = new HashMap<String, String>(c.getCount());
		try {
			while (c.moveToNext()) {
				m.put(c.getString(0), c.getString(1));
			}
		} finally {
			c.close();
		}
		return m;
	}

	protected ArrayList<T> convertCursorToList(Cursor c) {
		ArrayList<T> l = new ArrayList<T>(c.getCount());
		try {
			while (c.moveToNext()) {
				l.add(createObject(c));
			}
		} finally {
			c.close();
		}
		return l;
	}

	protected T convertCursorToObject(Cursor c) {
		try {
			while (c.moveToNext()) {
				T t = createObject(c);
				if (c.moveToNext()) {
					throw new RuntimeException("Query returned more than one object");
				} else {
					return t;
				}
			}
		} finally {
			c.close();
		}
		return null;
	}

	protected String[] columnsToStringArray(DbColumns[] cols) {
		String[] ret = new String[cols.length];
		int pos = 0;
		for (int i = 0; i < cols.length; i++) {
			ret[pos++] = cols[i].getName();
		}
		return ret;
	}

	public long insert(T obj) {
		return db.insert(name, null, createContentValue(obj));
	}

	protected abstract ContentValues createContentValue(T obj);

	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}

	protected T[] convertCursorToArray(Cursor c, T[] l) {
		try {
			int i = 0;
			while (c.moveToNext()) {
				l[i++] = createObject(c);
			}
		} finally {
			c.close();
		}
		return l;
	}

	protected abstract T createObject(Cursor c);

	protected String[] convertToStringArray(Cursor c) {
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
