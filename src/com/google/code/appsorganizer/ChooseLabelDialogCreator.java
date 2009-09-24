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
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.model.AppLabelSaver;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.Label;

public class ChooseLabelDialogCreator extends GenericDialogCreator {

	private static final String APPLICATION_BUNDLE_NAME = "application_label_dialog";
	private static final String PACKAGE_BUNDLE_NAME = "application_package_label_dialog";

	private final DatabaseHelper labelAdapter;

	private final ApplicationInfoManager applicationInfoManager;

	private Application application;

	private ChooseLabelListAdapter adapter;

	public ChooseLabelDialogCreator(GenericDialogManager dialogManager, DatabaseHelper labelAdapter,
			ApplicationInfoManager applicationInfoManager) {
		super(dialogManager);
		this.labelAdapter = labelAdapter;
		this.applicationInfoManager = applicationInfoManager;
	}

	private ListView listView;

	@Override
	public void prepareDialog(Dialog dialog) {
		final TextView tv = (TextView) dialog.findViewById(R.id.labelEdit);
		tv.setText("");
		List<AppLabelBinding> allLabels = getAllLabels(application);
		adapter = new ChooseLabelListAdapter(owner, allLabels);
		listView.setAdapter(adapter);

		int pos = 0;
		for (AppLabelBinding appLabelBinding : allLabels) {
			if (appLabelBinding.checked) {
				listView.setItemChecked(pos, true);
			}
			pos++;
		}

		ImageButton btn = (ImageButton) dialog.findViewById(R.id.newLabelButton);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CharSequence t = tv.getText();
				if (t != null && t.length() > 0) {
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
			}
		});
	}

	private List<AppLabelBinding> getAllLabels(Application a) {
		List<AppLabelBinding> ret = new ArrayList<AppLabelBinding>();
		Label[] all = labelAdapter.labelDao.getLabelsArray();
		for (int i = 0; i < all.length; i++) {
			Label label = all[i];
			long id = label.getId();
			boolean checked = a.hasLabel(id);
			AppLabelBinding b = new AppLabelBinding(label.getName(), id, checked);
			b.checked = checked;
			ret.add(b);
		}
		Collections.sort(ret);
		return ret;
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
		builder = builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int count = adapter.getCount();
				for (int i = 0; i < count; i++) {
					adapter.getItem(i).checked = listView.isItemChecked(i);
				}
				List<AppLabelBinding> modifiedLabels = adapter.getModifiedLabels();
				new AppLabelSaver(labelAdapter, applicationInfoManager).save(application, modifiedLabels, this);
			}
		});
		builder = builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				/* User clicked No so do some stuff */
			}
		});
		return builder.create();
	}

	public View getChooseDialogBody() {
		LayoutInflater factory = LayoutInflater.from(owner);
		View body = factory.inflate(R.layout.choose_label_dialog_body, null);
		return body;
	}

	public void setCurrentApp(Application application) {
		this.application = application;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (application != null) {
			outState.putString(APPLICATION_BUNDLE_NAME, application.name);
			outState.putString(PACKAGE_BUNDLE_NAME, application.getPackage());
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle state) {
		String appName = state.getString(APPLICATION_BUNDLE_NAME);
		if (appName != null) {
			application = applicationInfoManager.getApplication(state.getString(PACKAGE_BUNDLE_NAME), appName);
		}
	}
}
