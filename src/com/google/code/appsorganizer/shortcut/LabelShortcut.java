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
package com.google.code.appsorganizer.shortcut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.code.appsorganizer.Application;
import com.google.code.appsorganizer.ApplicationInfoManager;
import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.model.AppLabel;
import com.google.code.appsorganizer.model.Label;

public class LabelShortcut extends Activity {

	private static final String LABEL_ID = "com.example.android.apis.app.LauncherShortcuts";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Resolve the intent

		final Intent intent = getIntent();
		final String action = intent.getAction();

		// If the intent is a request to create a shortcut, we'll do that and
		// exit
		final DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(this);

		if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
			final List<Label> labels = dbHelper.labelDao.getLabels();
			ListView listView = new ListView(this);
			setTitle(R.string.choose_labels_header);
			setContentView(listView);
			listView.setAdapter(new ArrayAdapter<Label>(this, android.R.layout.simple_list_item_1, labels));

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
					CharSequence label = ((TextView) v).getText();
					Label labelObject = getLabelId(label);
					setupShortcut(labelObject);
					finish();
				}

				private Label getLabelId(CharSequence label) {
					for (Label l : labels) {
						if (l.getName().equals(label.toString())) {
							return l;
						}
					}
					throw new RuntimeException("Label " + label + " non trovata");
				}
			});
		} else {
			final long labelId = intent.getLongExtra(LABEL_ID, 2);
			Label label = dbHelper.labelDao.queryById(labelId);
			setTitle(label.getName());

			List<AppLabel> apps = dbHelper.appsLabelDao.getApps(labelId);
			final ApplicationInfoManager applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());
			applicationInfoManager.reloadAppsMap();
			final List<Application> applicationList = new ArrayList<Application>(applicationInfoManager.convertToApplicationList(apps));
			setContentView(R.layout.shortcut_grid);
			GridView mGrid = (GridView) findViewById(R.id.shortcutGrid);
			mGrid.setColumnWidth(50);
			final AppGridAdapter gridAdapter = new AppGridAdapter(applicationList, this);
			mGrid.setAdapter(gridAdapter);

			applicationInfoManager.addListener(new DbChangeListener() {
				public void notifyDataSetChanged() {
					List<AppLabel> apps = dbHelper.appsLabelDao.getApps(labelId);
					Collection<Application> newList = applicationInfoManager.convertToApplicationList(apps);
					gridAdapter.setApplicationList(new ArrayList<Application>(newList));
				}
			});

			addOnItemClick(mGrid);
		}
	}

	private void addOnItemClick(final GridView mGrid) {
		mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Application a = (Application) mGrid.getAdapter().getItem(pos);
				Intent i = a.getIntent();
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});
	}

	private void setupShortcut(Label label) {
		Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
		shortcutIntent.setClassName(this, this.getClass().getName());
		shortcutIntent.putExtra(LABEL_ID, label.getId());
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label.getName());
		Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, label.getIcon());
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

		setResult(RESULT_OK, intent);
	}
}
