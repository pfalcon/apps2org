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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.LiveFolders;

import com.google.code.appsorganizer.model.AppLabel;

public class ApplicationInfoManager {

	private final PackageManager pm;

	private static ApplicationInfoManager singleton;

	private ApplicationInfoManager(PackageManager pm) {
		this.pm = pm;
		loadAppsMap(pm);
	}

	private void loadAppsMap(PackageManager pm) {
		synchronized (this) {
			applicationMap = new HashMap<String, Application>();
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			List<ResolveInfo> installedApplications = pm.queryIntentActivities(mainIntent, 0);
			for (Iterator<ResolveInfo> iterator = installedApplications.iterator(); iterator.hasNext();) {
				ResolveInfo resolveInfo = iterator.next();
				ComponentInfo a = resolveInfo.activityInfo;
				if (a.enabled) {
					Application app = new ApplicationImpl(resolveInfo.activityInfo, (long) applicationMap.size());
					applicationMap.put(app.getName(), app);
				}
			}
		}
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

	private Map<String, Application> applicationMap = new HashMap<String, Application>();

	private List<Application> apps;

	public List<Application> getAppsArray(Handler handler) {
		if (apps == null) {
			apps = createAppsArray(handler);
		}
		return apps;
	}

	private ArrayList<Application> createAppsArray(Handler handler) {
		ArrayList<Application> l = new ArrayList<Application>();

		Collection<Application> values = applicationMap.values();
		for (Application app : values) {
			l.add(app);
			app.getLabel();
			app.getIcon();
			if (handler != null) {
				handler.sendEmptyMessage(l.size());
			}
			if (l.size() > 5) {
				break;
			}
		}
		Collections.sort(l);
		return l;
	}

	public Collection<Application> convertToApplicationList(List<AppLabel> l) {
		TreeSet<Application> ret = new TreeSet<Application>();
		for (AppLabel appLabel : l) {
			Application application = applicationMap.get(appLabel.getApp());
			if (application != null) {
				ret.add(application);
			}
		}
		return ret;
	}

	private class ApplicationImpl implements Application, Comparable<Application> {

		private final Long id;

		private final ActivityInfo activityInfo;

		private String label;

		private Drawable drawableIcon;

		public ApplicationImpl(ActivityInfo activityInfo, Long id) {
			this.id = id;
			this.activityInfo = activityInfo;
		}

		public String getLabel() {
			if (label == null) {
				CharSequence l = activityInfo.loadLabel(pm);
				if (l != null) {
					label = l.toString();
				}
			}
			return label;
		}

		public int compareTo(Application another) {
			return getLabel().compareToIgnoreCase(another.getLabel());
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

		public Uri getIntent() {
			Intent intent = ApplicationInfoManager.this.getIntent(activityInfo.packageName);
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
				} else if (col.equals(LiveFolders.ICON_BITMAP)) {
					values.add(((BitmapDrawable) getIcon()).getBitmap());
				} else if (col.equals(LiveFolders.ICON_PACKAGE)) {
					values.add(getPackage());
				} else if (col.equals(LiveFolders.ICON_RESOURCE)) {
					values.add(getIconResource());
				} else if (col.equals(LiveFolders.INTENT)) {
					values.add(getIntent());
				}
			}
			return values;
		}

		public Drawable getIcon() {
			if (drawableIcon == null) {
				drawableIcon = activityInfo.loadIcon(pm);
			}
			return drawableIcon;
		}
	}

	public Cursor convertToCursor(List<AppLabel> l, String[] cursorColumns) throws NameNotFoundException {
		MatrixCursor m = new MatrixCursor(cursorColumns, l.size());
		for (Application application : convertToApplicationList(l)) {
			m.addRow(application.getIterable(cursorColumns));
		}
		return m;
	}

	private static Map<String, ResolveInfo> infoMap = null;

	public Map<String, ResolveInfo> getRunableMap(boolean reload) {
		if (infoMap == null || reload == true) {
			synchronized (this) {
				Intent baseIntent = new Intent(Intent.ACTION_MAIN);
				baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> l = pm.queryIntentActivities(baseIntent, 0);
				infoMap = new HashMap<String, ResolveInfo>();
				for (ResolveInfo info : l) {
					infoMap.put(info.activityInfo.packageName, info);
				}
			}
		}
		return infoMap;
	}

	private Intent getIntent(String packageName) {
		// pm.getLaunchIntentForPackage(applicationInfo.packageName);
		Map<String, ResolveInfo> map = getRunableMap(false);
		ResolveInfo info = map.get(packageName);
		if (info != null) {
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.setClassName(packageName, info.activityInfo.name);
			return i;
		}
		return null;
	}
}
