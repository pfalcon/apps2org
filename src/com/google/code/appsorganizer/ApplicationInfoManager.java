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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.LiveFolders;
import android.widget.ImageView;

import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DbChangeListener;
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
			loadAppsMap(appCacheDao, null);
		}
	}

	public void reloadAll(AppCacheDao appCacheDao, Handler handler) {
		loadAppsMap(appCacheDao, handler);
	}

	private void loadAppsMap(AppCacheDao appCacheDao, Handler handler) {
		synchronized (this) {
			HashMap<String, String> nameCache = appCacheDao.queryForStringMap(false, AppCacheDao.NAME, AppCacheDao.LABEL, null, null, null,
					null);
			Map<String, Application> oldApps = applicationMap;
			applicationMap = new HashMap<String, Application>();
			List<ResolveInfo> installedApplications = getAllResolveInfo();
			long pos = 0;
			apps = new ArrayList<Application>();
			for (ResolveInfo resolveInfo : installedApplications) {
				ComponentInfo a = resolveInfo.activityInfo;
				if (a.enabled) {
					String name = resolveInfo.activityInfo.name;
					ApplicationImpl app = (ApplicationImpl) oldApps.get(resolveInfo.activityInfo.name);
					if (app == null) {
						app = new ApplicationImpl(resolveInfo.activityInfo, pos++);
						String n = nameCache.get(name);
						app.setLabel(n);
					}
					applicationMap.put(name, app);
					apps.add(app);
					// if label is not in cache table
					if (app.label == null) {
						// retrieve and store label
						appCacheDao.insert(new AppCache(name, app.getLabel()));
					}
					if (handler != null) {
						handler.sendEmptyMessage(apps.size());
					}
				}
			}
			Collections.sort(apps);
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

	private HashMap<String, Application> applicationMap = new HashMap<String, Application>();

	private ArrayList<Application> apps;

	public ArrayList<Application> getAppsArray() {
		return apps;
	}

	public Collection<Application> convertToApplicationListNot(List<String> l) {
		HashSet<String> s = new HashSet<String>(l);
		TreeSet<Application> ret = new TreeSet<Application>();
		for (Application application : apps) {
			if (!s.contains(application.getName())) {
				ret.add(application);
			}
		}
		return ret;
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

	private Application retrieveActivityInfo(String pack, String app) {
		int lastIndexOf = pack.lastIndexOf('.');
		if (lastIndexOf == -1) {
			return null;
		}
		pack = pack.substring(0, lastIndexOf);
		try {
			ComponentName componentName = new ComponentName(pack, app);
			ActivityInfo activityInfo = pm.getActivityInfo(componentName, 0);
			if (activityInfo != null) {
				return new ApplicationImpl(activityInfo, (long) applicationMap.size());
			} else {
				return null;
			}
		} catch (NameNotFoundException e) {
			return retrieveActivityInfo(pack, app);
		}
	}

	private class ApplicationImpl implements Application, Comparable<Application> {

		private final Long id;

		private final ActivityInfo activityInfo;

		private Drawable drawableIcon;

		private String label;

		private Intent intent;

		public ApplicationImpl(ActivityInfo activityInfo, Long id) {
			this.id = id;
			this.activityInfo = activityInfo;
		}

		public String getLabel() {
			// return getName();
			if (label == null) {
				CharSequence l = activityInfo.loadLabel(pm);
				if (l != null) {
					label = l.toString();
				}
			}
			return label;
		}

		public int compareTo(Application another) {
			int r = getLabel().compareToIgnoreCase(another.getLabel());
			if (r == 0) {
				r = getName().compareToIgnoreCase(another.getName());
			}
			return r;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return activityInfo.name;
		}

		public String getPackage() {
			return activityInfo.packageName;
		}

		public int getIconResource() {
			if (activityInfo.icon > 0) {
				return activityInfo.icon;
			}
			return activityInfo.applicationInfo.icon;
		}

		public Intent getIntent() {
			if (intent == null) {
				intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setClassName(getPackage(), getName());
			}
			return intent;
		}

		public Uri getIntentUri() {
			Intent intent = getIntent();
			Uri intentUri = null;
			if (intent != null) {
				intentUri = Uri.parse(intent.toURI());
			}
			return intentUri;
		}

		public Iterable<Object> getIterable(String[] cursorColumns) {
			List<Object> values = new ArrayList<Object>();
			for (String col : cursorColumns) {
				if (col.equals(BaseColumns._ID)) {
					values.add(getId());
				} else if (col.equals(LiveFolders.NAME)) {
					values.add(getLabel());
				} else if (col.equals(LiveFolders.ICON_PACKAGE)) {
					values.add(getPackage());
				} else if (col.equals(LiveFolders.ICON_RESOURCE)) {
					values.add(getIconResource());
				} else if (col.equals(LiveFolders.INTENT)) {
					values.add(getIntentUri());
				}
			}
			return values;
		}

		public Drawable getIcon() {
			return drawableIcon;
		}

		public void loadIcon() {
			drawableIcon = activityInfo.loadIcon(pm);
		}

		public void showIcon(ImageView imageView) {
			imageView.setImageDrawable(getIcon());
		}

		@Override
		public String toString() {
			return getLabel();
		}

		public void setLabel(String label) {
			this.label = label;
		}

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

	private final List<DbChangeListener> listeners = new ArrayList<DbChangeListener>();

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
