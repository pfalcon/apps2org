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
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.model.AppLabel;
import com.google.code.appsorganizer.model.AppLabelSaver;
import com.google.code.appsorganizer.model.Label;

public class ChooseLabelDialogCreator extends GenericDialogCreator<Activity> {

	private final DatabaseHelper labelAdapter;

	private String appId;

	private ChooseLabelListAdapter adapter;

	public ChooseLabelDialogCreator(Activity owner, DatabaseHelper labelAdapter) {
		super(owner);
		this.labelAdapter = labelAdapter;
	}

	@Override
	public void prepareDialog(Dialog dialog) {
		final TextView tv = (TextView) dialog.findViewById(R.id.labelEdit);
		tv.setText("");
		adapter = new ChooseLabelListAdapter(owner, getAllLabels(appId));

		ListView l = (ListView) dialog.findViewById(R.id.labelList);
		l.setAdapter(adapter);

		ImageButton btn = (ImageButton) dialog.findViewById(R.id.newLabelButton);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CharSequence t = tv.getText();
				if (t != null && t.length() > 0) {
					adapter.addLabel(t.toString());
				}
			}
		});
	}

	public List<AppLabelBinding> getAllLabels(String appId) {
		List<AppLabelBinding> ret = new ArrayList<AppLabelBinding>();
		List<AppLabel> labels = labelAdapter.appsLabelDao.getApps(appId);
		TreeMap<Long, Label> all = labelAdapter.labelDao.getLabelsTreeMap();
		for (AppLabel l : labels) {
			AppLabelBinding b = new AppLabelBinding();
			b.setChecked(true);
			b.setOriginalChecked(true);
			b.setAppLabelId(l.getId());
			b.setLabelId(l.getLabelId());
			Label label = all.remove(l.getLabelId());
			if (label != null) {
				b.setLabel(label.getName());
			}
			ret.add(b);
		}
		TreeSet<Label> values = new TreeSet<Label>(all.values());
		for (Label l : values) {
			AppLabelBinding b = new AppLabelBinding();
			b.setLabel(l.getName());
			b.setLabelId(l.getId());
			ret.add(b);
		}
		return ret;
	}

	@Override
	public Dialog createDialog() {
		View body = getChooseDialogBody();

		AlertDialog.Builder builder = new AlertDialog.Builder(owner);
		builder = builder.setTitle(R.string.choose_labels_header);
		builder = builder.setView(body);
		builder = builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				List<AppLabelBinding> modifiedLabels = adapter.getModifiedLabels();
				new AppLabelSaver(labelAdapter).save(appId, modifiedLabels);
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

	public void setCurrentApp(String appId) {
		this.appId = appId;
	}
}
