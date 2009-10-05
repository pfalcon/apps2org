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

import java.util.HashSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;

public class ChooseAppsDialogCreator extends GenericDialogCreator {

	private long currentLabelId;

	private SimpleCursorAdapter adapter;

	private final OnOkClickListener onOkClickListener;

	public ChooseAppsDialogCreator(GenericDialogManager dialogManager, OnOkClickListener onOkClickListener) {
		super(dialogManager);
		this.onOkClickListener = onOkClickListener;
	}

	private ListView listView;

	private HashSet<Long> checkedApps;

	@Override
	public void prepareDialog(Dialog dialog) {
		DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(owner);
		checkedApps = dbHelper.appCacheDao.getAppsOfLabelSet(currentLabelId);
		Cursor c = dbHelper.appCacheDao.getAppsOfLabel(currentLabelId);

		adapter = new SimpleCursorAdapter(owner, android.R.layout.simple_list_item_multiple_choice, c, new String[] { "label" },
				new int[] { android.R.id.text1 });
		listView.setAdapter(adapter);

		int size = checkedApps.size();
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

	private void save(HashSet<Long> checkedSet) {
		boolean changed = false;
		DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(owner);
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Cursor app = (Cursor) listView.getItemAtPosition(i);
			long appId = app.getLong(0);
			if (listView.isItemChecked(i)) {
				if (!checkedSet.contains(appId)) {
					dbHelper.appsLabelDao.insert(app.getString(2), app.getString(3), currentLabelId);
					changed = true;
				}
			} else {
				if (checkedSet.contains(appId)) {
					dbHelper.appsLabelDao.delete(app.getString(2), app.getString(3), currentLabelId);
					changed = true;
				}
			}
		}
		if (changed) {
			onOkClickListener.onClick(null, null, 0);
		}
	}

	public void setCurrentLabelId(long currentLabelId) {
		this.currentLabelId = currentLabelId;
	}
}
