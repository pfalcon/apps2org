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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.LiveFolders;
import android.widget.ImageView;

public class Application implements Comparable<Application>, GridObject {

	private final Long id;

	private final ActivityInfo activityInfo;

	private Drawable drawableIcon;

	private String label;

	private Intent intent;

	private String labelListString;

	public Application(ActivityInfo activityInfo, Long id) {
		this.id = id;
		this.activityInfo = activityInfo;
	}

	public String getLabel() {
		return label;
	}

	public int compareTo(Application another) {
		int r = label.compareToIgnoreCase(another.label);
		if (r == 0) {
			r = activityInfo.name.compareToIgnoreCase(another.activityInfo.name);
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

	public void loadIcon(PackageManager pm) {
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

	public String getLabelListString() {
		return labelListString;
	}

	public void setLabelListString(String labelListString) {
		this.labelListString = labelListString;
	}

	public void setIcon(Drawable drawableIcon) {
		this.drawableIcon = drawableIcon;
	}
}
