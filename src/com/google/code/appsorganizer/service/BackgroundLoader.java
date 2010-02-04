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
package com.google.code.appsorganizer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.code.appsorganizer.AppsOrganizerApplication;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.shortcut.LabelShortcut;

/**
 * @author fabio
 * 
 */
public class BackgroundLoader extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(AppsOrganizerApplication.TAG, "BackgroundLoader on start");
		DatabaseHelper.initOrSingleton(this);
		// ApplicationInfoManager applicationInfoManager =
		// ApplicationInfoManager.singleton(getPackageManager());
		// applicationInfoManager.reloadAll(dbHelper, null, false);
		// Application[] appsArray = applicationInfoManager.getAppsArray();
		// for (int i = 0; i < appsArray.length; i++) {
		// Application a = appsArray[i];
		// Application.loadIcon(getPackageManager(), a.getPackage(), a.name);
		// }
		LabelShortcut.firstTime = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(AppsOrganizerApplication.TAG, "BackgroundLoader on destroy");
	}
}