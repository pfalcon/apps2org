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

import android.content.Context;

public class DatabaseHelper extends DatabaseHelperBasic {

	public final AppLabelDao appsLabelDao;
	public final LabelDao labelDao;
	public final AppCacheDao appCacheDao;

	private static DatabaseHelper singleton;

	public static void init(Context context) {
		synchronized (DatabaseHelper.class) {
			singleton = new DatabaseHelper(context);
		}
	}

	public static DatabaseHelper initOrSingleton(Context context) {
		if (singleton == null) {
			init(context);
		}
		return singleton;
	}

	public static DatabaseHelper singleton() {
		return singleton;
	}

	private DatabaseHelper(Context context) {
		super(context);
		labelDao = new LabelDao();
		appsLabelDao = new AppLabelDao();
		appCacheDao = new AppCacheDao();
		labelDao.setDb(db);
		appsLabelDao.setDb(db);
		appCacheDao.setDb(db);
	}

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		db.setTransactionSuccessful();
	}

	public void endTransaction() {
		db.endTransaction();
	}

}