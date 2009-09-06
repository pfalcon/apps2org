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

import android.database.Cursor;

public abstract class ObjectWithIdDao<T extends ObjectWithId> extends DbDao<T> {

	public static final String ID_COL_NAME = "_id";

	public static final DbColumns ID = new DbColumns(ID_COL_NAME, "integer primary key autoincrement");

	public ObjectWithIdDao(String name) {
		super(name);
	}

	@Override
	public long insert(T obj) {
		long ret = super.insert(obj);
		obj.setId(ret);
		return ret;
	}

	public long update(T obj) {
		return db.update(name, createContentValue(obj), "_id = ?", new String[] { obj.getId().toString() });
	}

	public int delete(Long id) {
		return db.delete(name, "_id = ?", new String[] { id.toString() });
	}

	public T queryById(Long id) {
		Cursor c = db.query(name, columnsToStringArray(columns), ID.getName() + "=?", new String[] { id.toString() }, null, null, null);
		return convertCursorToObject(c);
	}

}
