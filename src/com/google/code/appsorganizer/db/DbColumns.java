package com.google.code.appsorganizer.db;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class DbColumns<T> {

	protected final String name;
	private final String description;

	public DbColumns(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public abstract void populateObject(T obj, Cursor c);

	public abstract void populateContent(T obj, ContentValues c);

	protected Integer getInt(Cursor c) {
		int i = c.getColumnIndexOrThrow(name);
		if (c.isNull(i)) {
			return null;
		}
		return c.getInt(i);
	}

	protected Long getLong(Cursor c) {
		int i = c.getColumnIndexOrThrow(name);
		if (c.isNull(i)) {
			return null;
		}
		return c.getLong(i);
	}

	protected String getString(Cursor c) {
		int i = c.getColumnIndexOrThrow(name);
		if (c.isNull(i)) {
			return null;
		}
		return c.getString(i);
	}

}