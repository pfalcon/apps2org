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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.code.appsorganizer.AppsOrganizerApplication;

/**
 * @author fabio
 * 
 */
public class StartupListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(AppsOrganizerApplication.TAG, "Starting service StartupListener");
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			startService(context);
		}
	}

	public static void startService(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("use_service", true)) {
			ComponentName comp = new ComponentName(context.getPackageName(), BackgroundLoader.class.getName());
			ComponentName service = context.startService(new Intent().setComponent(comp));
			if (null == service) {
				Log.e(AppsOrganizerApplication.TAG, "Could not start service " + comp.toString());
			}
		}
	}

	public static void stopService(Context context) {
		ComponentName comp = new ComponentName(context.getPackageName(), BackgroundLoader.class.getName());
		boolean stopped = context.stopService(new Intent().setComponent(comp));
		if (stopped) {
			Log.e(AppsOrganizerApplication.TAG, "Could not stop service " + comp.toString());
		}
	}
}