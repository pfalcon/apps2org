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

import gnu.trove.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.AppLabel;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.Label;

public class LabelListAdapter extends BaseExpandableListAdapter {

	private static final long OTHER_LABEL_ID = -1l;
	private static final long IGNORED_LABEL_ID = -2l;
	private final DatabaseHelper dbHelper;
	private final Activity context;
	private final ApplicationInfoManager applicationInfoManager;
	private final ChooseLabelDialogCreator chooseLabelDialog;

	private List<Label> groups;

	private TLongObjectHashMap<List<Application>> apps = new TLongObjectHashMap<List<Application>>();

	public LabelListAdapter(Activity context, DatabaseHelper dbHelper, ApplicationInfoManager applicationInfoManager,
			ChooseLabelDialogCreator chooseLabelDialog) {
		this.context = context;
		this.dbHelper = dbHelper;
		this.applicationInfoManager = applicationInfoManager;
		this.chooseLabelDialog = chooseLabelDialog;
		reload();
	}

	public void reloadAndNotify() {
		reload();
		notifyDataSetChanged();
	}

	private void reload() {
		groups = dbHelper.labelDao.getLabels();
		groups.add(new Label(OTHER_LABEL_ID, context.getText(R.string.other_label).toString()));
		groups.add(new Label(IGNORED_LABEL_ID, context.getText(R.string.ignored_label).toString()));
		apps = new TLongObjectHashMap<List<Application>>();
	}

	private List<Application> getAppsInPos(Integer pos) {
		Label label = groups.get(pos);
		return getApps(label.getId());
	}

	private List<Application> getApps(long labelId) {
		List<Application> ret = apps.get(labelId);
		if (ret == null) {
			if (labelId == OTHER_LABEL_ID) {
				String[] l = dbHelper.appsLabelDao.getAppsWithLabel();
				ret = new ArrayList<Application>(applicationInfoManager.convertToApplicationListNot(l));
			} else if (labelId == IGNORED_LABEL_ID) {
				String[] l = dbHelper.appCacheDao.getIgnoredApps();
				ret = new ArrayList<Application>(applicationInfoManager.convertToApplicationList(l));
			} else {
				AppLabel[] l = dbHelper.appsLabelDao.getApps(labelId);
				ret = new ArrayList<Application>(applicationInfoManager.convertToApplicationList(l));
			}
			apps.put(labelId, ret);
		}
		return ret;
	}

	public Application getChild(int groupPosition, int childPosition) {
		return getAppsInPos(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		return getAppsInPos(groupPosition).size();
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View cv, ViewGroup parent) {
		return SplashScreenActivity.getAppView(context, dbHelper, applicationInfoManager, cv, getChild(groupPosition, childPosition),
				chooseLabelDialog);
	}

	public Label getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	public int getGroupCount() {
		return groups.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View cv, ViewGroup parent) {
		if (cv == null) {
			LayoutInflater factory = LayoutInflater.from(context);
			cv = factory.inflate(R.layout.label_row_with_icon, null);
		}
		TextView v = (TextView) cv.findViewById(R.id.name);
		ImageView image = (ImageView) cv.findViewById(R.id.image);

		Label label = getGroup(groupPosition);

		v.setText(label.getName());
		Integer icon = label.getIcon();
		if (icon != null) {
			image.setImageResource(icon);
		} else {
			image.setImageDrawable(null);
		}
		return cv;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

}