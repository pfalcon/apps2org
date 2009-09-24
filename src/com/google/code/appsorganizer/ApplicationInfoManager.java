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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;

import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.AppLabelDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.db.DoubleArray;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.maps.ApplicationMap;
import com.google.code.appsorganizer.model.AppCache;
import com.google.code.appsorganizer.model.Application;

public class ApplicationInfoManager {

	private final PackageManager pm;

	private static ApplicationInfoManager singleton;

	private ApplicationMap applicationMap = new ApplicationMap(new Application[0]);

	private Application[] apps;

	private ApplicationInfoManager(PackageManager pm) {
		this.pm = pm;
	}

	public void getOrReloadAppsMap(DatabaseHelper dbHelper) {
		if (apps == null) {
			loadAppsMap(dbHelper.appCacheDao, dbHelper.labelDao, dbHelper.appsLabelDao, null, false);
		}
	}

	public void reloadAll(DatabaseHelper dbHelper, Handler handler, boolean discardCache) {
		loadAppsMap(dbHelper.appCacheDao, dbHelper.labelDao, dbHelper.appsLabelDao, handler, discardCache);
	}

	private static final Comparator<Application> appNameComparator = new Comparator<Application>() {
		public int compare(Application a1, Application a2) {
			return a1.name.compareTo(a2.name);
		}
	};

	private void loadAppsMap(AppCacheDao appCacheDao, LabelDao labelDao, AppLabelDao appsLabelDao, Handler handler, boolean discardCache) {
		synchronized (this) {
			DoubleArray appsLabels = labelDao.getAppsLabelsConcat();
			AppCacheMap nameCache = appCacheDao.queryForCacheMap();
			boolean[] installedApps = new boolean[nameCache.size()];
			ApplicationMap oldApps = applicationMap;
			List<ResolveInfo> installedApplications = getAllResolveInfo();
			long pos = 0;
			int arrayPos = 0;
			apps = new Application[installedApplications.size()];
			for (ResolveInfo resolveInfo : installedApplications) {
				ComponentInfo a = resolveInfo.activityInfo;
				if (a.enabled) {
					String name = resolveInfo.activityInfo.packageName + Application.SEPARATOR + resolveInfo.activityInfo.name;
					Application app = oldApps.get(name);
					int appCachePosition = nameCache.getPosition(name);
					AppCache appCache = nameCache.getAt(appCachePosition);
					if (app == null) {
						app = new Application(resolveInfo.activityInfo, pos++);
						if (appCache != null) {
							app.setLabel(appCache.label);
						}
					}
					if (appCache != null) {
						app.setStarred(appCache.starred);
						installedApps[appCachePosition] = true;
					}
					loadAppLabel(a, app, discardCache, appCacheDao);
					loadLabels(appsLabels, app);
					apps[arrayPos++] = app;
					if (handler != null && arrayPos % 10 == 0) {
						handler.sendEmptyMessage(arrayPos);
					}
				}
			}
			apps = copyArray(apps, arrayPos);
			Application[] allApps = copyArray(apps, arrayPos);
			Arrays.sort(apps);

			Arrays.sort(allApps, appNameComparator);
			applicationMap = new ApplicationMap(allApps);

			appCacheDao.removeUninstalledApps(installedApps, nameCache.keys());
			appsLabelDao.removeUninstalledApps(installedApps, nameCache.keys());
		}
	}

	private void loadAppLabel(ComponentInfo a, Application app, boolean discardCache, AppCacheDao appCacheDao) {
		// if label is not in cache table
		if (app.getLabel() == null) {
			// retrieve and store label
			CharSequence l = a.loadLabel(pm);
			if (l != null) {
				app.setLabel(l.toString());
				appCacheDao.insert(new AppCache(app.getPackage(), app.name, app.getLabel()));
			}
		} else if (discardCache) {
			CharSequence l = a.loadLabel(pm);
			if (l != null) {
				app.setLabel(l.toString());
				appCacheDao.updateLabel(app.getPackage(), app.name, app.getLabel());
			}
		}
	}

	public void reloadAppsLabel(LabelDao labelDao) {
		DoubleArray appsLabels = labelDao.getAppsLabelsConcat();
		for (int i = 0; i < apps.length; i++) {
			loadLabels(appsLabels, apps[i]);
		}
		notifyDataSetChanged(this);
	}

	private int loadLabels(DoubleArray appsLabels, Application app) {
		int pos = Arrays.binarySearch(appsLabels.keys, app.getPackage() + Application.SEPARATOR + app.name);
		if (pos >= 0) {
			app.setLabelListString(appsLabels.values[pos]);
			app.setLabelIds(appsLabels.labelIds[pos]);
		} else {
			app.setLabelListString(null);
			app.setLabelIds(null);
		}
		return pos;
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

	public Application[] getAppsArray() {
		return apps;
	}

	public Application[] getAppsNoLabel() {
		Application[] ret = new Application[apps.length];
		int pos = 0;
		for (int i = 0; i < apps.length; i++) {
			Application application = apps[i];
			String l = application.getLabelListString();
			if (l == null || l.equals("")) {
				ret[pos++] = application;
			}
		}
		return copyArray(ret, pos);
	}

	public Application[] getApps(long labelId, boolean onlyStarred) {
		Application[] ret = new Application[apps.length];
		int i = 0;
		String l = Application.LABEL_ID_SEPARATOR + Long.toString(labelId) + Application.LABEL_ID_SEPARATOR;
		for (Application application : apps) {
			String labelIds = application.getLabelIds();
			if (labelIds != null && labelIds.indexOf(l) != -1) {
				if (!onlyStarred || application.isStarred()) {
					ret[i++] = application;
				}
			}
		}
		return copyArray(ret, i);
	}

	private Application[] copyArray(Application[] ret, int pos) {
		Application[] a = new Application[pos];
		System.arraycopy(ret, 0, a, 0, pos);
		return a;
	}

	public Application getApplication(String packageName, String app) {
		return applicationMap.get(packageName + Application.SEPARATOR + app);
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
