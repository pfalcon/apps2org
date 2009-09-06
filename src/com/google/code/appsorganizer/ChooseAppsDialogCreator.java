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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.utils.ArrayAdapterSmallRow;

public class ChooseAppsDialogCreator extends GenericDialogCreator {

	private final DatabaseHelper dbHelper;

	private final ApplicationInfoManager applicationInfoManager;

	private long currentLabelId;

	private ArrayAdapter<Application> adapter;

	public ChooseAppsDialogCreator(DatabaseHelper dbHelper, ApplicationInfoManager applicationInfoManager) {
		this.dbHelper = dbHelper;
		this.applicationInfoManager = applicationInfoManager;
	}

	public ChooseAppsDialogCreator(DatabaseHelper dbHelper, PackageManager pm) {
		this.dbHelper = dbHelper;
		this.applicationInfoManager = ApplicationInfoManager.singleton(pm);
		applicationInfoManager.getOrReloadAppsMap(dbHelper);
	}

	private ListView listView;

	private Set<String> checkedApps;

	@Override
	public void prepareDialog(Dialog dialog) {
		Application[] l1 = applicationInfoManager.getApps(currentLabelId, false);
		checkedApps = createSet(l1);
		List<Application> allApps = new ArrayList<Application>();
		for (int i = 0; i < l1.length; i++) {
			allApps.add(l1[i]);
		}
		Application[] tmp = applicationInfoManager.getAppsArray();
		for (int i = 0; i < tmp.length; i++) {
			Application application = tmp[i];
			if (!checkedApps.contains(application.name)) {
				allApps.add(application);
			}
		}

		adapter = new ArrayAdapterSmallRow<Application>(owner, android.R.layout.simple_list_item_multiple_choice, allApps);

		listView.setAdapter(adapter);

		int size = l1.length;
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

	private Set<String> createSet(Application[] checkedApps) {
		Set<String> s = new HashSet<String>();
		for (int i = 0; i < checkedApps.length; i++) {
			s.add(checkedApps[i].name);
		}
		return s;
	}

	private void save(Set<String> checkedSet) {
		boolean changed = false;
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Application app = (Application) listView.getItemAtPosition(i);
			String appName = app.name;
			if (listView.isItemChecked(i)) {
				if (!checkedSet.contains(appName)) {
					dbHelper.appsLabelDao.insert(appName, currentLabelId);
					changed = true;
				}
			} else {
				if (checkedSet.contains(appName)) {
					dbHelper.appsLabelDao.delete(appName, currentLabelId);
					changed = true;
				}
			}
		}
		if (changed) {
			ApplicationInfoManager.notifyDataSetChanged(this);
		}
	}

	public void setCurrentLabelId(long currentLabelId) {
		this.currentLabelId = currentLabelId;
	}
}
