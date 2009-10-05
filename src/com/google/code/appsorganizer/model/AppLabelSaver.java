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
import com.google.code.appsorganizer.db.DatabaseHelper;

public class AppLabelSaver {

	public static void save(DatabaseHelper dbHelper, String packageName, String name, List<AppLabelBinding> modifiedLabels) {
		if (!modifiedLabels.isEmpty()) {
			for (AppLabelBinding b : modifiedLabels) {
				Long labelId = b.labelId;
				if (b.checked) {
					if (b.checked && labelId == null) {
						labelId = dbHelper.labelDao.insert(b.label);
					}
					dbHelper.appsLabelDao.insert(packageName, name, labelId);
				} else {
					dbHelper.appsLabelDao.delete(packageName, name, labelId);
				}
			}
		}
	}
}
