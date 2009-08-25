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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.model.AppLabel;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.Label;

public class ChooseAppsDialogCreator extends GenericDialogCreator {

	private final DatabaseHelper dbHelper;

	private final ApplicationInfoManager applicationInfoManager;

	private Label currentLabel;

	private ArrayAdapter<Application> adapter;

	public ChooseAppsDialogCreator(DatabaseHelper dbHelper, ApplicationInfoManager applicationInfoManager) {
		this.dbHelper = dbHelper;
		this.applicationInfoManager = applicationInfoManager;
	}

	private ListView listView;

	private Set<String> checkedApps;

	private List<AppLabel> appsWithLabel;

	@Override
	public void prepareDialog(Dialog dialog) {
		appsWithLabel = dbHelper.appsLabelDao.getApps(currentLabel.getId());
		Collection<Application> l1 = applicationInfoManager.convertToApplicationList(appsWithLabel);
		checkedApps = createSet(l1);
		List<Application> allApps = new ArrayList<Application>();
		allApps.addAll(l1);
		ArrayList<Application> tmp = applicationInfoManager.getAppsArray();
		for (Application application : tmp) {
			if (!checkedApps.contains(application.getName())) {
				allApps.add(application);
			}
		}

		adapter = new ArrayAdapter<Application>(owner, android.R.layout.simple_list_item_multiple_choice, allApps);

		listView.setAdapter(adapter);

		int size = l1.size();
		for (int i = 0; i < size; i++) {
			listView.setItemChecked(i, true);
		}
	}

	@Override
	public Dialog createDialog() {
		listView = new ListView(owner);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(owner);
		builder = builder.setTitle(R.string.select_apps);
		builder = builder.setView(listView);
		builder = builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				save(checkedApps);
			}

		});
		builder = builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		return builder.create();
	}

	private Set<String> createSet(Collection<Application> checkedApps) {
		Set<String> s = new HashSet<String>();
		for (Application application : checkedApps) {
			s.add(application.getName());
		}
		return s;
	}

	public void setCurrentLabel(Label currentLabel) {
		this.currentLabel = currentLabel;
	}

	private void save(Set<String> checkedSet) {
		Map<String, Long> appsId = new HashMap<String, Long>();
		for (AppLabel appLabel : appsWithLabel) {
			appsId.put(appLabel.getApp(), appLabel.getId());
		}
		boolean changed = false;
		for (int i = 0; i < adapter.getCount(); i++) {
			Application app = (Application) listView.getItemAtPosition(i);
			String appName = app.getName();
			if (listView.isItemChecked(i)) {
				if (!checkedSet.contains(appName)) {
					dbHelper.appsLabelDao.insert(appName, currentLabel.getId());
					changed = true;
				}
			} else {
				if (checkedSet.contains(appName)) {
					dbHelper.appsLabelDao.delete(appsId.get(appName));
					changed = true;
				}
			}
		}
		if (changed) {
			applicationInfoManager.notifyDataSetChanged();
		}
	}
}
