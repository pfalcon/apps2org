package com.google.code.appsorganizer.db;


import java.util.Collections;
import java.util.List;

import com.google.code.appsorganizer.model.AppLabel;

import android.content.ContentValues;
import android.database.Cursor;

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

	private static AppLabelDao singleton = new AppLabelDao();

	private AppLabelDao() {
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

	public List<AppLabel> getApps(Long labelId) {
		return queryForList(columns, Collections.singletonMap(LABEL_ID, labelId.toString()), null, null, null);
	}

	public List<AppLabel> getApps(String app) {
		return queryForList(columns, Collections.singletonMap(APP, app), null, null, null);
	}

	public Cursor getAppsCursor(Long labelId) {
		return query(columns, Collections.singletonMap(LABEL_ID, labelId.toString()), null, null, null);
	}

	public static AppLabelDao getSingleton() {
		return singleton;
	}
}
