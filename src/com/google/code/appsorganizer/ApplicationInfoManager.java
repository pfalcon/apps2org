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
import java.util.Collections;
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
		loadAppsMap(appCacheDao, labelDao, handler);
	}

	private void loadAppsMap(AppCacheDao appCacheDao, LabelDao labelDao, Handler handler) {
		synchronized (this) {
			String[] keys = null;
			String[] values = null;
			if (labelDao != null) {
				DoubleArray appsLabels = labelDao.getAppsLabelsConcat();
				keys = appsLabels.keys;
				values = appsLabels.values;
			}
			HashMap<String, AppCache> nameCache = appCacheDao.queryForCacheMap();
			HashMap<String, Application> oldApps = applicationMap;
			applicationMap = new HashMap<String, Application>();
			List<ResolveInfo> installedApplications = getAllResolveInfo();
			long pos = 0;
			apps = new ArrayList<Application>(installedApplications.size());
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
							appCacheDao.insert(new AppCache(name, app.getLabel()));
						}
					}
					if (keys != null) {
						loadLabels(keys, values, app);
					}
					boolean ignored = appCache != null && appCache.isIgnored();
					app.setIgnored(ignored);
					if (!ignored) {
						apps.add(app);
						if (handler != null) {
							handler.sendEmptyMessage(apps.size());
						}
					}
				}
			}
			Collections.sort(apps);
		}
	}

	public void reloadAppsLabel(LabelDao labelDao) {
		DoubleArray appsLabels = labelDao.getAppsLabelsConcat();
		String[] keys = appsLabels.keys;
		String[] values = appsLabels.values;
		for (Application app : apps) {
			loadLabels(keys, values, app);
		}
	}

	public void ignoreApp(Application a) {
		apps.remove(a);
	}

	public void dontIgnoreApp(Application a) {
		apps.add(a);
		Collections.sort(apps);
	}

	private void loadLabels(String[] keys, String[] values, Application app) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == null) {
				return;
			}
			if (keys[i].equals(app.name)) {
				app.setLabelListString(values[i]);
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

	private ArrayList<Application> apps;

	public ArrayList<Application> getAppsArray() {
		return apps;
	}

	public Collection<Application> convertToApplicationListNot(String[] l) {
		TreeSet<Application> ret = new TreeSet<Application>();
		for (Application application : apps) {
			boolean found = false;
			for (int i = 0; i < l.length; i++) {
				if (l[i].equals(application.name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				ret.add(application);
			}
		}
		return ret;
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

	public Application[] convertToApplicationListNoIgnored(String[] l) {
		Application[] ret = new Application[l.length];
		int pos = 0;
		for (int i = 0; i < l.length; i++) {
			Application application = getApplication(l[i]);
			if (application != null && !application.isIgnored()) {
				ret[pos++] = application;
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

	public void notifyDataSetChanged() {
		for (DbChangeListener a : listeners) {
			a.dataSetChanged();
		}
	}

}
