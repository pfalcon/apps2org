package com.google.code.appsorganizer.db;


import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.google.code.appsorganizer.model.Label;

import android.content.ContentValues;
import android.database.Cursor;

public class LabelDao extends ObjectWithIdDao<Label> {

	public static final String NAME = "labels";

	public static final DbColumns<Label> LABEL = new DbColumns<Label>("label", "text not null unique") {
		@Override
		public void populateObject(Label obj, Cursor c) {
			obj.setName(getString(c));
		}

		@Override
		public void populateContent(Label obj, ContentValues c) {
			c.put(name, obj.getName());
		}
	};

	public static final DbColumns<Label> ICON = new DbColumns<Label>("icon", "integer") {
		@Override
		public void populateObject(Label obj, Cursor c) {
			obj.setIcon(getInt(c));
		}

		@Override
		public void populateContent(Label obj, ContentValues c) {
			c.put(name, obj.getIcon());
		}
	};

	private static LabelDao singleton = new LabelDao();

	private LabelDao() {
		super(NAME);
		addColumn(LABEL);
		addColumn(ICON);
	}

	@Override
	public Label createNewObject() {
		return new Label();
	}

	public String getLabelsString(String appId) {
		StringBuilder b = new StringBuilder();
		List<Label> l = getLabels(appId);
		for (Label label : l) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(label.getName());
		}
		return b.toString();
	}

	public List<Label> getLabels(String appId) {
		Cursor c = db.rawQuery(
				"select l._id, l.label from labels l inner join apps_labels al on l._id = al.id_label where al.app=? order by l.label",
				new String[] { appId });
		return convertCursorToList(c, columns);
	}

	public TreeMap<Long, Label> getLabelsTreeMap() {
		TreeMap<Long, Label> m = new TreeMap<Long, Label>();
		List<Label> labels = getLabels();
		for (Label label : labels) {
			m.put(label.getId(), label);
		}
		return m;
	}

	public List<Label> getLabels() {
		return queryForList(columns, null, "upper(" + LABEL.getName() + ")", null, null);
	}

	public Cursor getLabelsCursor() {
		return query(columns, null, "upper(" + LABEL + ")", null, null);
	}

	public long insert(String label) {
		Label obj = new Label();
		obj.setName(label);
		return insert(obj);
	}

	public Label getLabel(String name) {
		return queryForObject(columns, Collections.singletonMap(LABEL, name), null, null);
	}

	public static LabelDao getSingleton() {
		return singleton;
	}
}
