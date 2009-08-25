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

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.code.appsorganizer.model.AppLabel;

public class AppLabelDao extends ObjectWithIdDao<AppLabel> {

	public static final String NAME = "apps_labels";

	public static final DbColumns<AppLabel> APP = new DbColumns<AppLabel>("app", "text not null") {
		@Override
		public void populateObject(AppLabel obj, android.database.Cursor c) {
			obj.setApp(getString(c));
		}

		@Override
		public void populateContent(AppLabel obj, ContentValues c) {
			c.put(name, obj.getApp());
		}
	};
	public static final DbColumns<AppLabel> LABEL_ID = new DbColumns<AppLabel>("id_label", "integer not null") {
		@Override
		public void populateObject(AppLabel obj, android.database.Cursor c) {
			obj.setLabelId(getLong(c));
		}

		@Override
		public void populateContent(AppLabel obj, ContentValues c) {
			c.put(name, obj.getLabelId());
		}
	};

	AppLabelDao() {
		super(NAME);
		addColumn(APP);
		addColumn(LABEL_ID);
	}

	@Override
	public AppLabel createNewObject() {
		return new AppLabel();
	}

	public long insert(String app, long labelId) {
		AppLabel obj = new AppLabel();
		obj.setApp(app);
		obj.setLabelId(labelId);
		return insert(obj);
	}

	public List<String> getAppsWithLabel() {
		return queryForStringList(true, APP, null, null, null, null);
	}

	public List<AppLabel> getApps(Long labelId) {
		return queryForList(columns, LABEL_ID, labelId.toString(), null, null, null);
	}

	public List<AppLabel> getApps(String app) {
		return queryForList(columns, APP, app, null, null, null);
	}

	public Cursor getAppsCursor(Long labelId) {
		return query(columns, LABEL_ID, labelId.toString(), null, null, null);
	}
}
