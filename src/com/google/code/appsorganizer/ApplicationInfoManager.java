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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
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

	private static final Set<String> ignoredApps = new HashSet<String>();

	static {
		ignoredApps.add("com.android.phone");
		ignoredApps.add("com.google.android.street");
		ignoredApps.add("com.android.googlesearch");
		ignoredApps.add("com.samsung.camera.firmware");
		ignoredApps.add("com.samsung.TSP.firmware");
		ignoredApps.add("com.samsung.RilErrorNotifier");
		ignoredApps.add("com.samsung.android.wlantest");
		ignoredApps.add("com.samsung.bootsetting");
		ignoredApps.add("com.samsung.sec.android.musictest_tool");
		ignoredApps.add("com.samsung.sec.android.application.csc");
		ignoredApps.add("com.samsung.InputEventApp");
		ignoredApps.add("com.samsung.test.shutdown");
		ignoredApps.add("com.android.samsungtest.FactoryTest");
		ignoredApps.add("com.android.samsungtest.CurrenctDataState");
		ignoredApps.add("com.test.version");
		ignoredApps.add("com.test.lcdtest");
		ignoredApps.add("com.android.samsungtest.FileCopy");
		ignoredApps.add("com.android.testgps");
		ignoredApps.add("com.android.samsungtest.DataCopy");
		ignoredApps.add("android");
		ignoredApps.add("com.google.android.providers.settings");
		ignoredApps.add("com.google.android.providers.gmail");
		ignoredApps.add("com.android.providers.settings");
		ignoredApps.add("com.android.providers.im");
		ignoredApps.add("com.android.providers.drm");
		ignoredApps.add("com.android.providers.calendar");
		ignoredApps.add("com.android.providers.contacts");
		ignoredApps.add("com.android.providers.media");
		ignoredApps.add("com.android.providers.downloads");
		ignoredApps.add("com.android.providers.userdictionary");
		ignoredApps.add("com.android.providers.telephony");
		ignoredApps.add("com.android.providers.subscribedfeeds");
		ignoredApps.add("com.google.android.server.checkin");
		ignoredApps.add("com.android.serviceModeApp");
		ignoredApps.add("com.android.RilFactoryApp");
		ignoredApps.add("com.google.android.apps.uploader");
		ignoredApps.add("com.android.packageinstaller");
		ignoredApps.add("com.android.htmlviewer");
		ignoredApps.add("com.android.FileCount");
		ignoredApps.add("com.google.android.googleapps");
		ignoredApps.add("com.google.android.apps.gtalkservice");
		ignoredApps.add("com.google.android.location");
		ignoredApps.add("com.android.stk");
		ignoredApps.add("com.android.setupwizard");
		ignoredApps.add("com.android.inputmethod.latin");
		ignoredApps.add("com.android.Personalization");
		ignoredApps.add("com.android.term");
		ignoredApps.add("com.android.soundrecorder");
	}

	public ApplicationInfoManager(PackageManager pm) {
		this.pm = pm;
	}

	public ArrayList<Application> getAppsArray(Handler handler) {
		List<ApplicationInfo> installedApplications = pm.getInstalledApplications(0);
		ArrayList<Application> apps = new ArrayList<Application>();
		for (Iterator<ApplicationInfo> iterator = installedApplications.iterator(); iterator.hasNext();) {
			ApplicationInfo a = iterator.next();
			if (!a.enabled || ignoredApps.contains(a.packageName)) {
				iterator.remove();
			} else {
				apps.add(convertToApplication(a, null));
				getLabel(a);
				getIcon(a);
				handler.sendEmptyMessage(apps.size());
			}
		}
		Collections.sort(apps);
		return apps;
	}

	public Collection<Application> convertToApplicationList(List<AppLabel> l) {
		TreeSet<Application> ret = new TreeSet<Application>();
		for (AppLabel appLabel : l) {
			try {
				ret.add(convertToApplication(appLabel));
			} catch (NameNotFoundException ignored) {
			}
		}
		return ret;
	}

	public Application convertToApplication(AppLabel appLabel) throws NameNotFoundException {
		return new ApplicationImpl(appLabel);
	}

	public Application convertToApplication(String appId, Long id) throws NameNotFoundException {
		return new ApplicationImpl(appId, id);
	}

	public Application convertToApplication(ApplicationInfo applicationInfo, Long id) {
		return new ApplicationImpl(applicationInfo, id);
	}

	private class ApplicationImpl implements Application, Comparable<Application> {

		private final Long id;

		private final ApplicationInfo applicationInfo;

		public ApplicationImpl(AppLabel appLabel) throws NameNotFoundException {
			this(appLabel.getApp(), appLabel.getId());
		}

		public ApplicationImpl(String appId, Long id) throws NameNotFoundException {
			this.id = id;
			this.applicationInfo = getApplicationInfo(appId);
		}

		public ApplicationImpl(ApplicationInfo applicationInfo, Long id) {
			this.id = id;
			this.applicationInfo = applicationInfo;
		}

		public String getName() {
			CharSequence name = getLabel(applicationInfo);
			return name.toString();
		}

		public int compareTo(Application another) {
			return getName().compareToIgnoreCase(another.getName());
		}

		public Long getId() {
			return id;
		}

		public String getPackage() {
			return applicationInfo.packageName;
		}

		public int getIconResource() {
			return applicationInfo.icon;
		}

		public Uri getIntent() {
			Intent intent = ApplicationInfoManager.this.getIntent(applicationInfo.packageName);
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
					values.add(getName());
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
			return ApplicationInfoManager.this.getIcon(applicationInfo);
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

	private static Map<String, CharSequence> labelCache = new HashMap<String, CharSequence>();

	private CharSequence getLabel(ApplicationInfo applicationInfo) {
		CharSequence ret = labelCache.get(applicationInfo.packageName);
		if (ret == null) {
			ret = applicationInfo.loadLabel(pm);
			labelCache.put(applicationInfo.packageName, ret);
		}
		return ret;
	}

	private ApplicationInfo getApplicationInfo(String appId) throws NameNotFoundException {
		return pm.getApplicationInfo(appId, 0);
	}

	private static Map<String, Drawable> iconCache = new HashMap<String, Drawable>();

	private Drawable getIcon(ApplicationInfo applicationInfo) {
		Drawable ret = iconCache.get(applicationInfo.packageName);
		if (ret == null) {
			ret = applicationInfo.loadIcon(pm);
			iconCache.put(applicationInfo.packageName, ret);
		}
		return ret;
	}
}
