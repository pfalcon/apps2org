package com.google.code.appsorganizer.db;

import java.util.Collections;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class ObjectWithIdDao<T extends ObjectWithId> extends DbDao<T> {

	public final DbColumns<T> ID = new DbColumns<T>("_id", "integer primary key autoincrement") {
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
		Cursor c = query(columns, Collections.singletonMap(ID, id.toString()), null, null, null);
		return convertCursorToObject(c, columns);
	}

}
