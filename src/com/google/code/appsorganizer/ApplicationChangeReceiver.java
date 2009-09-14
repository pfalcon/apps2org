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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.code.appsorganizer.db.DatabaseHelper;

/**
 * @author fabio
 * 
 */
public class ApplicationChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String packageName = intent.getDataString();
		Log.i("ApplicationChangeReceiver", intent.getAction());
		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
			Log.i("ApplicationChangeReceiver", packageName + " added");
			if (!ApplicationInfoManager.isSingletonNull()) {
				DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);
				ApplicationInfoManager.singleton(null).reloadAll(dbHelper, null, false);
				ApplicationInfoManager.notifyDataSetChanged(this);
			}
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			Log.i("ApplicationChangeReceiver", packageName + " removed");
			if (!ApplicationInfoManager.isSingletonNull()) {
				DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);
				ApplicationInfoManager.singleton(null).reloadAll(dbHelper, null, false);
				ApplicationInfoManager.notifyDataSetChanged(this);
			}
		}
	}
}