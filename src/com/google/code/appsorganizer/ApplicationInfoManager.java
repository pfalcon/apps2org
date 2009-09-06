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
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Handler;

import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.db.DoubleArray;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.model.AppCache;
import com.google.code.appsorganizer.model.AppLabel;
import com.google.code.appsorganizer.model.Application;

public class ApplicationInfoManager {

	private final PackageManager pm;

	private static ApplicationInfoManager singleton;

	private ApplicationInfoManager(PackageManager pm) {
		this.pm = pm;
	}

	public void getOrReloadAppsMap(AppCacheDao appCacheDao) {
		if (applicationMap.isEmpty()) {
			loadAppsMap(appCacheDao, null, null);
		}
	}

	public void reloadAll(AppCacheDao appCacheDao, LabelDao labelDao, Handler handler) {
		// Debug.startMethodTracing("splash");
		reload2(appCacheDao, labelDao, handler);
		// Debug.stopMethodTracing();
	}

	private void reload2(AppCacheDao appCacheDao, LabelDao labelDao, Handler handler) {
		// getOrReloadAppsMap(appCacheDao);
		// applicationMap = new HashMap<String, Application>();
		loadAppsMap(appCacheDao, labelDao, handler);
	}

	private void loadAppsMap(AppCacheDao appCacheDao, LabelDao labelDao, Handler handler) {
		synchronized (this) {
			DoubleArray appsLabels = null;
			if (labelDao != null) {
				appsLabels = labelDao.getAppsLabelsConcat();
			}
			HashMap<String, AppCache> nameCache = appCacheDao.queryForCacheMap();
			HashMap<String, Application> oldApps = applicationMap;
			applicationMap = new HashMap<String, Application>();
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
							app.setLabel(appCache.getLabel());
						}
					}
					if (appCache != null) {
						app.setStarred(appCache.isStarred());
					}
					applicationMap.put(name, app);
					// if label is not in cache table
					if (app.getLabel() == null) {
						// retrieve and store label
						CharSequence l = a.loadLabel(pm);
						if (l != null) {
							app.setLabel(l.toString());
							appCacheDao.insert(new AppCache(app.getPackage(), name, app.getLabel()));
						}
					}
					if (appsLabels != null) {
						loadLabels(appsLabels, app);
					}
					boolean ignored = appCache != null && appCache.isIgnored();
					app.setIgnored(ignored);
					if (!ignored) {
						apps[arrayPos++] = app;
						if (handler != null) {
							handler.sendEmptyMessage(arrayPos);
						}
					}
				}
			}
			apps = copyArray(apps, arrayPos);
			Arrays.sort(apps);
		}
	}

	public void reloadAppsLabel(LabelDao labelDao) {
		DoubleArray appsLabels = labelDao.getAppsLabelsConcat();
		for (Application app : apps) {
			loadLabels(appsLabels, app);
		}
	}

	public void ignoreApp(Application a) {
		// TODO
		// apps.remove(a);
	}

	public void dontIgnoreApp(Application a) {
		// TODO
		Application[] oldApps = apps;
		apps = new Application[oldApps.length + 1];
		for (int i = 0; i < oldApps.length; i++) {
			Application cur = oldApps[i];
			if (a.compareTo(cur) > 0) {
				apps[i] = a;
			} else {
				apps[i] = cur;
			}
		}
	}

	private void loadLabels(DoubleArray appsLabels, Application app) {
		String[] keys = appsLabels.keys;
		int length = keys.length;
		for (int i = 0; i < length; i++) {
			if (keys[i] == null) {
				return;
			}
			if (keys[i].equals(app.name)) {
				app.setLabelListString(appsLabels.values[i]);
				app.setLabelIds(appsLabels.labelIds[i]);
				return;
			}
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

	private HashMap<String, Application> applicationMap = new HashMap<String, Application>();

	private Application[] apps;

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
			if (application.isStarred() && !application.isIgnored()) {
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

	public Application[] getIgnoredApps() {
		Application[] ret = new Application[apps.length];
		int pos = 0;
		Collection<Application> values = applicationMap.values();
		for (Application application : values) {
			if (application.isIgnored()) {
				ret[pos++] = application;
			}
		}
		return copyArray(ret, pos);
	}

	public Application[] convertToApplicationArray(String[] l, boolean ignored, boolean onlyStarred) {
		Application[] ret = new Application[l.length];
		int pos = 0;
		for (int i = 0; i < l.length; i++) {
			Application a = getApplication(l[i]);
			if (a != null && (ignored || !a.isIgnored()) && (!onlyStarred || a.isStarred())) {
				ret[pos++] = a;
			}
		}
		ret = copyArray(ret, pos);
		Arrays.sort(ret);
		return ret;
	}

	private Application[] copyArray(Application[] ret, int pos) {
		Application[] a = new Application[pos];
		for (int i = 0; i < pos; i++) {
			a[i] = ret[i];
		}
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
		Application ret = applicationMap.get(app);
		// if (ret == null) {
		// ret = retrieveActivityInfo(app, app);
		// if (ret != null) {
		// applicationMap.put(app, ret);
		// }
		// }
		return ret;
	}

	public Cursor convertToCursor(List<AppLabel> l, String[] cursorColumns) throws NameNotFoundException {
		final ArrayList<Application> applications = new ArrayList<Application>(convertToApplicationList(l));
		return convertToCursorFromApplications(applications, cursorColumns);
	}

	public Cursor convertToCursorFromApplications(final List<Application> applications, String[] cursorColumns) {
		MatrixCursor m = createCursor(cursorColumns, applications);
		for (Application application : applications) {
			m.addRow(application.getIterable(cursorColumns));
		}
		return m;
	}

	private MatrixCursor createCursor(String[] cursorColumns, final List<Application> applications) {
		return new MatrixCursor(cursorColumns, applications.size());
	}

	private final ArrayList<DbChangeListener> listeners = new ArrayList<DbChangeListener>();

	public boolean addListener(DbChangeListener object) {
		return listeners.add(object);
	}

	public boolean removeListener(DbChangeListener object) {
		return listeners.remove(object);
	}

	public void notifyDataSetChanged(Object source, short type) {
		for (DbChangeListener a : listeners) {
			a.dataSetChanged(source, type);
		}
	}

	public void notifyDataSetChanged(Object source) {
		notifyDataSetChanged(source, DbChangeListener.CHANGED_ALL);
	}

}
