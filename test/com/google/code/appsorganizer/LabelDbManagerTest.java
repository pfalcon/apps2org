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
package com.google.code.appsorganizer;

import android.test.ActivityInstrumentationTestCase2;

public class LabelDbManagerTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public LabelDbManagerTest() {
		super("com.google.code.appsorganizer", AppsListActivity.class);
	}

	private static final String appId = "aaaaaa";

	// public void testGetAllLabels() throws Exception {
	// LabelDbManager labelDbManager = new LabelDbManager(getActivity());
	// String nomeLabel = "labAAA";
	// labelDbManager.addLabel(appId, nomeLabel);
	//
	// try {
	// List<Label> l = labelDbManager.getAllLabelsList(appId);
	// for (Label label : l) {
	// if (label.getName().equals(nomeLabel)) {
	// assertEquals(true, label.isChecked());
	// assertEquals(true, label.isOriginalChecked());
	// }
	// }
	// } finally {
	// labelDbManager.deleteLabel(appId, nomeLabel);
	// }
	// }
	//
	// public void testGetLabelsString() throws Exception {
	// LabelDbManager labelDbManager = new LabelDbManager(getActivity());
	// String s = labelDbManager.getLabelsString(appId);
	// assertEquals("", s);
	//
	// labelDbManager.addLabel(appId, "lab1");
	// labelDbManager.addLabel(appId, "lab2");
	// s = labelDbManager.getLabelsString(appId);
	// assertEquals("lab1, lab2", s);
	//
	// labelDbManager.deleteLabel(appId, "lab1");
	// labelDbManager.deleteLabel(appId, "lab2");
	// s = labelDbManager.getLabelsString(appId);
	// assertEquals("", s);
	// }
}
