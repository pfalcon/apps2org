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

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.code.appsorganizer.Application;
import com.google.code.appsorganizer.model.Label;

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
			obj.setIconDb(getInt(c));
		}

		@Override
		public void populateContent(Label obj, ContentValues c) {
			c.put(name, obj.getIconDb());
		}
	};

	LabelDao() {
		super(NAME);
		addColumn(LABEL);
		addColumn(ICON);
	}

	@Override
	public Label createNewObject() {
		return new Label();
	}

	public String getLabelsString(Application application) {
		StringBuilder b = new StringBuilder();
		List<Label> l = getLabels(application);
		for (Label label : l) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(label.getName());
		}
		return b.toString();
	}

	public List<Label> getLabels(Application application) {
		Cursor c = db
				.rawQuery(
						"select l._id, l.label, l.icon from labels l inner join apps_labels al on l._id = al.id_label where al.app=? order by l.label",
						new String[] { application.getName() });
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
}
