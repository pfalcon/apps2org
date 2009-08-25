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

import com.google.code.appsorganizer.model.AppCache;

public class AppCacheDao extends ObjectWithIdDao<AppCache> {

	public static final String TABLE_NAME = "apps";

	public static final DbColumns<AppCache> NAME = new DbColumns<AppCache>("name", "text not null") {
		@Override
		public void populateObject(AppCache obj, android.database.Cursor c) {
			obj.setName(getString(c));
		}

		@Override
		public void populateContent(AppCache obj, ContentValues c) {
			c.put(name, obj.getName());
		}
	};
	public static final DbColumns<AppCache> LABEL = new DbColumns<AppCache>("label", "text not null") {
		@Override
		public void populateObject(AppCache obj, android.database.Cursor c) {
			obj.setLabel(getString(c));
		}

		@Override
		public void populateContent(AppCache obj, ContentValues c) {
			c.put(name, obj.getLabel());
		}
	};

	AppCacheDao() {
		super(TABLE_NAME);
		addColumn(NAME);
		addColumn(LABEL);
	}

	@Override
	public AppCache createNewObject() {
		return new AppCache();
	}

}
