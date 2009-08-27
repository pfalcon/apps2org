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

public abstract class ObjectWithIdDao<T extends ObjectWithId> extends DbDao<T> {

	protected static final String ID_COL_NAME = "_id";

	public final DbColumns<T> ID = new DbColumns<T>(ID_COL_NAME, "integer primary key autoincrement") {
		@Override
		public void populateObject(T obj, Cursor c) {
			obj.setId(getLong(c));
		}

		@Override
		public void populateContent(T obj, ContentValues c) {
			c.put(name, obj.getId());
		}
	};

	public ObjectWithIdDao(String name) {
		super(name);
		addColumn(ID);
	}

	@Override
	public long insert(T obj) {
		long ret = super.insert(obj);
		obj.setId(ret);
		return ret;
	}

	public long update(T obj) {
		ContentValues v = new ContentValues();
		for (DbColumns<T> col : columns) {
			col.populateContent(obj, v);
		}
		return db.update(name, v, "_id = ?", new String[] { obj.getId().toString() });
	}

	public int delete(Long id) {
		return db.delete(name, "_id = ?", new String[] { id.toString() });
	}

	public T queryById(Long id) {
		Cursor c = query(columns, ID, id.toString(), null, null, null);
		return convertCursorToObject(c, columns);
	}

}
