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
package com.google.code.appsorganizer.model;

import java.util.ArrayList;
import java.util.List;

import android.test.ActivityInstrumentationTestCase2;

import com.google.code.appsorganizer.AppLabelBinding;
import com.google.code.appsorganizer.Application;
import com.google.code.appsorganizer.ApplicationInfoManager;
import com.google.code.appsorganizer.AppsListActivity;
import com.google.code.appsorganizer.MockApplication;
import com.google.code.appsorganizer.db.DatabaseHelper;

public class AppLabelSaverTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public AppLabelSaverTest() {
		super("com.google.code.appsorganizer", AppsListActivity.class);
	}

	private static final Application appId = new MockApplication("aaaaaa");

	public void testGetLabelsString() throws Exception {
		DatabaseHelper dbHelper = DatabaseHelper.singleton();

		AppLabelSaver appLabelSaver = new AppLabelSaver(dbHelper, ApplicationInfoManager.singleton(getActivity().getPackageManager()));

		List<AppLabelBinding> modifiedLabels = new ArrayList<AppLabelBinding>();
		modifiedLabels.add(new AppLabelBinding("lab1", true));
		modifiedLabels.add(new AppLabelBinding("lab2", true));
		try {
			appLabelSaver.save(appId, modifiedLabels);
		} finally {
			modifiedLabels.get(0).setChecked(false);
			modifiedLabels.get(1).setChecked(false);
			appLabelSaver.save(appId, modifiedLabels);
			dbHelper.labelDao.delete(modifiedLabels.get(0).getLabelId());
			dbHelper.labelDao.delete(modifiedLabels.get(1).getLabelId());
		}
	}

}
