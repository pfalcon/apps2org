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

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.model.AppLabelSaver;
import com.google.code.appsorganizer.model.AppCache;

public class ChooseLabelDialogCreator extends GenericDialogCreator {

	private static final String APPLICATION_BUNDLE_NAME = "application_label_dialog";
	private static final String PACKAGE_BUNDLE_NAME = "application_package_label_dialog";

	private String packageName;

	private String name;

	private ChooseLabelListAdapter adapter;

	private final OnOkClickListener onOkClickListener;

	private final NewLabelDialog newLabelDialog;

	public ChooseLabelDialogCreator(GenericDialogManager dialogManager, OnOkClickListener onOkClickListener) {
		super(dialogManager);
		this.onOkClickListener = onOkClickListener;

		newLabelDialog = new NewLabelDialog(dialogManager, new OnOkClickListener() {

			private static final long serialVersionUID = 7421660517919410764L;

			public void onClick(CharSequence t, DialogInterface dialog, int which) {
				int count = adapter.getCount();
				boolean[] checked = new boolean[count];
				for (int i = 0; i < count; i++) {
					checked[i] = listView.isItemChecked(i);
				}
				adapter.addLabel(t.toString());
				for (int i = 1; i < count + 1; i++) {
					listView.setItemChecked(i, checked[i - 1]);
				}
				listView.setItemChecked(0, true);
			}
		});
	}

	private ListView listView;

	@Override
	public void prepareDialog(final Dialog dialog) {
		final DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(owner);
		List<AppLabelBinding> allLabels = dbHelper.labelDao.getAppsLabelList(packageName, name);
		adapter = new ChooseLabelListAdapter(owner, allLabels);
		listView.setAdapter(adapter);

		AppCache appCache = dbHelper.appCacheDao.queryForAppCache(packageName, name, false, false);
		final CheckBox starCheck = (CheckBox)dialog.findViewById(R.id.starCheck);
		starCheck.setChecked(appCache.starred);

		int pos = 0;
		for (AppLabelBinding appLabelBinding : allLabels) {
			if (appLabelBinding.checked) {
				listView.setItemChecked(pos, true);
			}
			pos++;
		}

		dialog.findViewById(R.id.newLabelButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				newLabelDialog.showDialog();
			}
		});

		dialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int count = adapter.getCount();
				for (int i = 0; i < count; i++) {
					adapter.getItem(i).checked = listView.isItemChecked(i);
				}
				List<AppLabelBinding> modifiedLabels = adapter.getModifiedLabels();
				AppLabelSaver.save(DatabaseHelper.initOrSingleton(owner), packageName, name, modifiedLabels);
				dbHelper.appCacheDao.updateStarred(packageName, name, starCheck.isChecked());
				if (onOkClickListener != null) {
					onOkClickListener.onClick(null, dialog, Dialog.BUTTON_POSITIVE);
				}
				dialog.hide();
			}
		});

		dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.hide();
			}
		});
	}

	@Override
	public Dialog createDialog() {
		View body = getChooseDialogBody();

		listView = (ListView) body.findViewById(R.id.labelList);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(owner);
		builder = builder.setTitle(R.string.choose_labels_header);
		builder = builder.setView(body);
		return builder.create();
	}

	public View getChooseDialogBody() {
		LayoutInflater factory = LayoutInflater.from(owner);
		View body = factory.inflate(R.layout.choose_label_dialog_body, null);
		return body;
	}

	public void setCurrentApp(String packageName, String name) {
		this.packageName = packageName;
		this.name = name;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(APPLICATION_BUNDLE_NAME, name);
		outState.putString(PACKAGE_BUNDLE_NAME, packageName);
	}

	@Override
	public void onRestoreInstanceState(Bundle state) {
		name = state.getString(APPLICATION_BUNDLE_NAME);
		packageName = state.getString(PACKAGE_BUNDLE_NAME);
	}
}
