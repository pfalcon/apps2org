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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;

import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.db.DoubleArray;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.maps.ApplicationMap;
import com.google.code.appsorganizer.model.AppCache;
import com.google.code.appsorganizer.model.AppLabel;
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
			loadAppsMap(dbHelper.appCacheDao, dbHelper.labelDao, null, false);
		}
	}

	public void reloadAll(AppCacheDao appCacheDao, LabelDao labelDao, Handler handler, boolean discardCache) {
		// Debug.startMethodTracing("splash");
		loadAppsMap(appCacheDao, labelDao, handler, discardCache);
		// Debug.stopMethodTracing();
	}

	private static final Comparator<Application> appNameComparator = new Comparator<Application>() {
		public int compare(Application a1, Application a2) {
			return a1.name.compareTo(a2.name);
		}
	};

	private void loadAppsMap(AppCacheDao appCacheDao, LabelDao labelDao, Handler handler, boolean discardCache) {
		synchronized (this) {
			DoubleArray appsLabels = labelDao.getAppsLabelsConcat();
			AppCacheMap nameCache = appCacheDao.queryForCacheMap();
			ApplicationMap oldApps = applicationMap;
			List<ResolveInfo> installedApplications = getAllResolveInfo();
			long pos = 0;
			int arrayPos = 0;
			apps = new Application[installedApplications.size()];
			for (ResolveInfo resolveInfo : installedApplications) {
				ComponentInfo a = resolveInfo.activityInfo;
				if (a.enabled) {
					String name = resolveInfo.activityInfo.name;
					AppCache appCache = nameCache.get(name);
					Application app = oldApps.get(name);
					if (app == null) {
						app = new Application(resolveInfo.activityInfo, pos++);
						if (appCache != null) {
							app.setLabel(appCache.label);
						}
					}
					if (appCache != null) {
						app.setStarred(appCache.starred);
					}
					// if label is not in cache table
					if (app.getLabel() == null) {
						// retrieve and store label
						CharSequence l = a.loadLabel(pm);
						if (l != null) {
							app.setLabel(l.toString());
							appCacheDao.insert(new AppCache(app.getPackage(), name, app.getLabel()));
						}
					} else if (discardCache) {
						CharSequence l = a.loadLabel(pm);
						if (l != null) {
							app.setLabel(l.toString());
							appCacheDao.updateLabel(app.getPackage(), name, app.getLabel());
						}
					}
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
		}
	}

	public void reloadAppsLabel(LabelDao labelDao) {
		DoubleArray appsLabels = labelDao.getAppsLabelsConcat();
		for (int i = 0; i < apps.length; i++) {
			loadLabels(appsLabels, apps[i]);
		}
		notifyDataSetChanged(this);
	}

	private void loadLabels(DoubleArray appsLabels, Application app) {
		int pos = Arrays.binarySearch(appsLabels.keys, app.name);
		if (pos >= 0) {
			app.setLabelListString(appsLabels.values[pos]);
			app.setLabelIds(appsLabels.labelIds[pos]);
		}
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

	public Collection<Application> convertToApplicationList(AppLabel[] l) {
		TreeSet<Application> ret = new TreeSet<Application>();
		for (int i = 0; i < l.length; i++) {
			AppLabel appLabel = l[i];
			Application application = getApplication(appLabel.getApp());
			if (application != null) {
				ret.add(application);
			}
		}
		return ret;
	}

	public Collection<Application> convertToApplicationList(String[] l) {
		TreeSet<Application> ret = new TreeSet<Application>();
		for (int i = 0; i < l.length; i++) {
			Application application = getApplication(l[i]);
			if (application != null) {
				ret.add(application);
			}
		}
		return ret;
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

	public Application[] getStarredApps() {
		Application[] ret = new Application[apps.length];
		int i = 0;
		for (Application application : apps) {
			if (application.isStarred()) {
				ret[i++] = application;
			}
		}
		return copyArray(ret, i);
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

	public Application[] convertToApplicationArray(String[] l, boolean onlyStarred) {
		Application[] ret = new Application[l.length];
		int pos = 0;
		for (int i = 0; i < l.length; i++) {
			Application a = getApplication(l[i]);
			if (a != null && (!onlyStarred || a.isStarred())) {
				ret[pos++] = a;
			}
		}
		ret = copyArray(ret, pos);
		Arrays.sort(ret);
		return ret;
	}

	private Application[] copyArray(Application[] ret, int pos) {
		Application[] a = new Application[pos];
		System.arraycopy(ret, 0, a, 0, pos);
		return a;
	}

	public Collection<Application> convertToApplicationList(List<AppLabel> l) {
		TreeSet<Application> ret = new TreeSet<Application>();
		for (AppLabel appLabel : l) {
			Application application = getApplication(appLabel.getApp());
			if (application != null) {
				ret.add(application);
			}
		}
		return ret;
	}

	public Collection<Application> convertToApplicationListNotSorted(List<AppLabel> l) {
		List<Application> ret = new ArrayList<Application>();
		for (AppLabel appLabel : l) {
			Application application = getApplication(appLabel.getApp());
			if (application != null) {
				ret.add(application);
			}
		}
		return ret;
	}

	public Collection<Application> convertToApplicationListFromString(List<String> l) {
		TreeSet<Application> ret = new TreeSet<Application>();
		for (String app : l) {
			Application application = getApplication(app);
			if (application != null) {
				ret.add(application);
			}
		}
		return ret;
	}

	private Application getApplication(String app) {
		return applicationMap.get(app);
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
