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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.AppLabelDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.model.AppCache;
import com.google.code.appsorganizer.model.Application;

public class ApplicationInfoManager {

	private final PackageManager pm;

	private static ApplicationInfoManager singleton;

	private ApplicationInfoManager(PackageManager pm) {
		this.pm = pm;
	}

	public void reloadAll(DatabaseHelper dbHelper, Handler handler, boolean discardCache) {
		loadAppsMap(dbHelper.appCacheDao, dbHelper.labelDao, dbHelper.appsLabelDao, handler, discardCache);
	}

	private void loadAppsMap(AppCacheDao appCacheDao, LabelDao labelDao, AppLabelDao appsLabelDao, Handler handler, boolean discardCache) {
		synchronized (this) {
			AppCacheMap nameCache = appCacheDao.queryForCacheMap();
			boolean[] installedApps = new boolean[nameCache.size()];
			List<ResolveInfo> installedApplications = getAllResolveInfo();
			int arrayPos = 0;
			for (ResolveInfo resolveInfo : installedApplications) {
				ComponentInfo a = resolveInfo.activityInfo;
				if (a.enabled) {
					String name = a.packageName + Application.SEPARATOR + a.name;
					int appCachePosition = nameCache.getPosition(name);
					AppCache appCache = nameCache.getAt(appCachePosition);
					if (appCache != null) {
						installedApps[appCachePosition] = true;
					}
					loadAppLabel(a, discardCache, appCacheDao, appCache);
					if (handler != null) {
						handler.sendEmptyMessage(arrayPos++);
					}
				}
			}

			appCacheDao.removeUninstalledApps(installedApps, nameCache.keys());
			appsLabelDao.removeUninstalledApps(installedApps, nameCache.keys());
		}
	}

	private void loadAppLabel(ComponentInfo a, boolean discardCache, AppCacheDao appCacheDao, AppCache loadedObj) {
		boolean changed = false;
		String label = null;
		byte[] image = null;
		if (loadedObj != null) {
			label = loadedObj.label;
			image = loadedObj.image;
		}
		if (label == null || discardCache) {
			CharSequence l = a.loadLabel(pm);
			if (l != null) {
				label = l.toString();
				changed = true;
			}
		}
		if (image == null || discardCache) {
			Drawable drawable = a.loadIcon(pm);
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			boolean compressed = bitmap.compress(CompressFormat.PNG, 100, os);
			if (!compressed) {
				os = new ByteArrayOutputStream();
				compressed = bitmap.compress(CompressFormat.JPEG, 100, os);
			}
			image = os.toByteArray();
			changed = true;
		}
		if (changed) {
			// if label is not in cache table
			if (loadedObj == null) {
				// retrieve and store label
				AppCache obj = new AppCache(a.packageName, a.name, label);
				obj.image = image;
				appCacheDao.insert(obj);
			} else {
				appCacheDao.updateLabel(a.packageName, a.name, label, image);
			}
		}
	}

	public void reloadAppsLabel(LabelDao labelDao) {
		notifyDataSetChanged(this);
	}

	private List<ResolveInfo> getAllResolveInfo() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		return pm.queryIntentActivities(mainIntent, 0);
	}

	public static ApplicationInfoManager singleton(PackageManager pm) {
		if (singleton == null) {
			synchronized (ApplicationInfoManager.class) {
				if (singleton == null) {
					singleton = new ApplicationInfoManager(pm);
				}
			}
		}
		return singleton;
	}

	public static boolean isSingletonNull() {
		return singleton == null;
	}

	private static final ArrayList<DbChangeListener> listeners = new ArrayList<DbChangeListener>();

	public static boolean addListener(DbChangeListener object) {
		return listeners.add(object);
	}

	public static boolean removeListener(DbChangeListener object) {
		return listeners.remove(object);
	}

	public static void notifyDataSetChanged(Object source, short type) {
		for (DbChangeListener a : listeners) {
			a.dataSetChanged(source, type);
		}
	}

	public static void notifyDataSetChanged(Object source) {
		notifyDataSetChanged(source, DbChangeListener.CHANGED_ALL);
	}

}
