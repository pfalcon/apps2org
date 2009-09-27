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

import com.google.code.appsorganizer.maps.AppCacheMap;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class Application implements Comparable<Application> {

	public static final char LABEL_ID_SEPARATOR = '#';

	private final Long id;

	private Drawable drawableIcon;

	private String label;

	private Intent intent;

	private boolean starred;

	public final String name;

	private final String packageName;

	private final String completeName;

	private final int icon;

	public Application(ActivityInfo activityInfo, Long id) {
		this.id = id;
		this.name = activityInfo.name;
		this.packageName = activityInfo.packageName;
		this.completeName = packageName + AppCacheMap.SEPARATOR + name;
		if (activityInfo.icon > 0) {
			this.icon = activityInfo.icon;
		} else {
			this.icon = activityInfo.applicationInfo.icon;
		}
	}

	public Application(String packageName, String name, Long id) {
		this.id = id;
		this.name = name;
		this.packageName = packageName;
		this.completeName = packageName + AppCacheMap.SEPARATOR + name;
		this.icon = 0;
	}

	public String getLabel() {
		return label;
	}

	public int compareTo(Application another) {
		int r = label.compareToIgnoreCase(another.label);
		if (r == 0) {
			r = packageName.compareToIgnoreCase(another.packageName);
			if (r == 0) {
				r = name.compareToIgnoreCase(another.name);
			}
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

	@Override
	public String toString() {
		return getLabel();
	}

	public void setLabel(String label) {
		this.label = label;
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

	public String getCompleteName() {
		return completeName;
	}
}
