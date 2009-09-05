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

public class DbColumns {

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

	public Integer getInt(Cursor c) {
		int i = c.getColumnIndexOrThrow(name);
		if (c.isNull(i)) {
			return null;
		}
		return c.getInt(i);
	}

	public Long getLong(Cursor c) {
		int i = c.getColumnIndexOrThrow(name);
		if (c.isNull(i)) {
			return null;
		}
		return c.getLong(i);
	}

	public String getString(Cursor c) {
		int i = c.getColumnIndexOrThrow(name);
		if (c.isNull(i)) {
			return null;
		}
		return c.getString(i);
	}

}