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
package com.google.code.appsorganizer.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class Application implements Comparable<Application> {

	public static final char LABEL_ID_SEPARATOR = '#';

	private final Long id;

	private Drawable drawableIcon;

	private String label;

	private Intent intent;

	private String labelListString;

	private String labelIds;

	private boolean starred;

	public final String name;

	private final String packageName;

	private final int icon;

	public Application(ActivityInfo activityInfo, Long id) {
		this.id = id;
		this.name = activityInfo.name;
		this.packageName = activityInfo.packageName;
		if (activityInfo.icon > 0) {
			this.icon = activityInfo.icon;
		} else {
			this.icon = activityInfo.applicationInfo.icon;
		}
	}

	public String getLabel() {
		return label;
	}

	public int compareTo(Application another) {
		int r = label.compareToIgnoreCase(another.label);
		if (r == 0) {
			r = name.compareToIgnoreCase(another.name);
		}
		return r;
	}

	public Long getId() {
		return id;
	}

	public String getPackage() {
		return packageName;
	}

	public int getIconResource() {
		return icon;
	}

	public Intent getIntent() {
		if (intent == null) {
			intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName(packageName, name);
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

	public Drawable getIcon() {
		return drawableIcon;
	}

	public void loadIcon(PackageManager pm) {
		drawableIcon = loadIconOrCache(pm, packageName, name);
	}

	private static final Map<String, Drawable> iconsCache = Collections.synchronizedMap(new HashMap<String, Drawable>());
	public static final char SEPARATOR = '#';

	public static Drawable loadIconOrCache(PackageManager pm, String packageName, String name) {
		Drawable d = getIconFromCache(packageName, name);
		if (d == null) {
			d = loadIcon(pm, packageName, name);
		}
		return d;
	}

	public static Drawable getIconFromCache(String packageName, String name) {
		return getIconFromCache(packageName + SEPARATOR + name);
	}

	public static Drawable getIconFromCache(String comp) {
		return iconsCache.get(comp);
	}

	public static Drawable loadIcon(PackageManager pm, String packageName, String name) {
		try {
			Drawable d = pm.getActivityIcon(new ComponentName(packageName, name));
			iconsCache.put(packageName + SEPARATOR + name, d);
			return d;
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	public static Drawable loadIcon(PackageManager pm, String comp) {
		try {
			int i = comp.indexOf(SEPARATOR);
			Drawable d = pm.getActivityIcon(new ComponentName(comp.substring(0, i), comp.substring(i + 1)));
			iconsCache.put(comp, d);
			return d;
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabelListString() {
		return labelListString;
	}

	public void setLabelListString(String labelListString) {
		this.labelListString = labelListString;
	}

	public void setIcon(Drawable drawableIcon) {
		this.drawableIcon = drawableIcon;
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public String getLabelIds() {
		return labelIds;
	}

	public void setLabelIds(String labelIds) {
		this.labelIds = labelIds;
	}

	public boolean hasLabel(long labelId) {
		if (labelIds == null) {
			return false;
		}
		return labelIds.indexOf(Application.LABEL_ID_SEPARATOR + Long.toString(labelId) + Application.LABEL_ID_SEPARATOR) != -1;
	}
}
