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

import android.test.ActivityInstrumentationTestCase2;

import com.google.code.appsorganizer.AppLabelBinding;
import com.google.code.appsorganizer.AppsListActivity;
import com.google.code.appsorganizer.MockApplication;
import com.google.code.appsorganizer.model.Application;

public class LabelDaoTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public LabelDaoTest() {
		super("com.google.code.appsorganizer", AppsListActivity.class);
	}

	private static final Application appId = new MockApplication("aaaaaa");
	private static final String otherAppId = "o_aaaaaa";

	public void testGetLabelsString() throws Exception {
		DatabaseHelper dbHelper = DatabaseHelper.singleton();
		LabelDao labelDao = dbHelper.labelDao;
		AppLabelDao appsLabelDao = dbHelper.appsLabelDao;

		String s = labelDao.getLabelsString(appId);
		assertEquals("", s);

		Long lab1Id = null;
		Long lab2Id = null;
		Long lab3Id = null;
		Long id1 = null;
		Long id2 = null;
		Long id3 = null;
		try {
			lab1Id = labelDao.insert("lab1");
			lab2Id = labelDao.insert("lab2");
			lab3Id = labelDao.insert("lab3");

			id1 = appsLabelDao.insert(appId.getName(), lab1Id);
			id2 = appsLabelDao.insert(appId.getName(), lab2Id);
			id3 = appsLabelDao.insert(otherAppId, lab3Id);
			s = labelDao.getLabelsString(appId);
			assertEquals("lab1, lab2", s);
		} finally {
			deleteAppLabel(appsLabelDao, id1);
			deleteAppLabel(appsLabelDao, id2);
			deleteAppLabel(appsLabelDao, id3);

			deleteLabel(labelDao, lab1Id);
			deleteLabel(labelDao, lab2Id);
			deleteLabel(labelDao, lab3Id);

			s = labelDao.getLabelsString(appId);
			assertEquals("", s);
		}
	}

	// public void testGetAllLabels() throws Exception {
	// DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
	// LabelDao labelDao = dbHelper.labelDao;
	// AppLabelDao appsLabelDao = dbHelper.appsLabelDao;
	//
	// Long lab1Id = null;
	// Long lab2Id = null;
	// Long lab3Id = null;
	// Long id1 = null;
	// Long id2 = null;
	// Long id3 = null;
	// try {
	// lab1Id = labelDao.insert("lab1");
	// lab2Id = labelDao.insert("lab2");
	// lab3Id = labelDao.insert("lab3");
	//
	// id1 = appsLabelDao.insert(appId, lab1Id);
	// id2 = appsLabelDao.insert(appId, lab2Id);
	// id3 = appsLabelDao.insert(otherAppId, lab3Id);
	// List<AppLabelBinding> allLabels = labelDao.getAllLabels(appId);
	// assertEquals(true, getLabel(allLabels, lab1Id).isChecked());
	// assertEquals(true, getLabel(allLabels, lab2Id).isChecked());
	// assertEquals(false, getLabel(allLabels, lab3Id).isChecked());
	// } finally {
	// deleteAppLabel(appsLabelDao, id1);
	// deleteAppLabel(appsLabelDao, id2);
	// deleteAppLabel(appsLabelDao, id3);
	//
	// List<AppLabelBinding> allLabels = labelDao.getAllLabels(appId);
	// assertEquals(false, getLabel(allLabels, lab1Id).isChecked());
	// assertEquals(false, getLabel(allLabels, lab2Id).isChecked());
	// assertEquals(false, getLabel(allLabels, lab3Id).isChecked());
	//
	// deleteLabel(labelDao, lab1Id);
	// deleteLabel(labelDao, lab2Id);
	// deleteLabel(labelDao, lab3Id);
	// }
	// }

	private AppLabelBinding getLabel(List<AppLabelBinding> allLabels, Long o) {
		for (AppLabelBinding l : allLabels) {
			if (l.getLabelId().equals(o)) {
				return l;
			}
		}
		fail("label " + o + " not found");
		throw null;
	}

	private void deleteAppLabel(AppLabelDao appsLabelDao, Long id1) {
		if (id1 != null) {
			appsLabelDao.delete(id1);
		}
	}

	private void deleteLabel(LabelDao labelDao, Long lab1Id) {
		if (lab1Id != null) {
			labelDao.delete(lab1Id);
		}
	}

}
