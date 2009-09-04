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

import java.util.List;

import com.google.code.appsorganizer.AppLabelBinding;
import com.google.code.appsorganizer.ApplicationInfoManager;
import com.google.code.appsorganizer.db.DatabaseHelper;

public class AppLabelSaver {

	private final DatabaseHelper dbHelper;

	private final ApplicationInfoManager applicationInfoManager;

	public AppLabelSaver(DatabaseHelper dbHelper, ApplicationInfoManager applicationInfoManager) {
		this.dbHelper = dbHelper;
		this.applicationInfoManager = applicationInfoManager;
	}

	public static void saveStarred(DatabaseHelper dbHelper, ApplicationInfoManager applicationInfoManager, Application application,
			boolean starred) {
		dbHelper.appCacheDao.updateStarred(application.getName(), starred);
		applicationInfoManager.notifyDataSetChanged();
	}

	public static void saveIgnored(DatabaseHelper dbHelper, ApplicationInfoManager applicationInfoManager, Application application,
			boolean ignored) {
		application.setIgnored(ignored);
		dbHelper.appCacheDao.updateIgnored(application.getName(), ignored);
		if (ignored) {
			applicationInfoManager.ignoreApp(application);
		} else {
			applicationInfoManager.dontIgnoreApp(application);
		}
		applicationInfoManager.notifyDataSetChanged();
	}

	public void save(Application application, List<AppLabelBinding> modifiedLabels) {
		if (!modifiedLabels.isEmpty()) {
			for (AppLabelBinding b : modifiedLabels) {
				Long labelId = b.getLabelId();
				if (b.isChecked()) {
					if (b.isChecked() && labelId == null) {
						labelId = dbHelper.labelDao.insert(b.getLabel());
					}
					dbHelper.appsLabelDao.insert(application.getName(), labelId);
				} else {
					dbHelper.appsLabelDao.delete(b.getAppLabelId());
				}
			}
			applicationInfoManager.reloadAppsLabel(dbHelper.labelDao);
			applicationInfoManager.notifyDataSetChanged();
		}
	}
}
