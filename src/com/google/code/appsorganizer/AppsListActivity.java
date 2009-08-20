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

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;

public class AppsListActivity extends ListActivity {

	private DatabaseHelper dbHelper;

	private List<Application> apps;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private GenericDialogManager genericDialogManager;

	private ApplicationInfoManager applicationInfoManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper = DatabaseHelper.singleton();
		setContentView(R.layout.main);
		genericDialogManager = new GenericDialogManager(this);
		applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());
		chooseLabelDialog = new ChooseLabelDialogCreator(dbHelper, applicationInfoManager);
		genericDialogManager.addDialog(chooseLabelDialog);
		apps = applicationInfoManager.getAppsArray(null);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				chooseLabelDialog.setCurrentApp(apps.get(position));
				showDialog(chooseLabelDialog.getDialogId());
			}
		});

		final ArrayAdapter<Application> appsAdapter = new ArrayAdapter<Application>(this, R.layout.app_row, apps) {
			@Override
			public View getView(int position, View v, ViewGroup parent) {
				if (v == null) {
					LayoutInflater factory = LayoutInflater.from(getContext());
					v = factory.inflate(R.layout.app_row, null);
				}
				Application a = getItem(position);
				((ImageView) v.findViewById(R.id.image)).setImageDrawable(a.getIcon());
				((TextView) v.findViewById(R.id.labels)).setText(dbHelper.labelDao.getLabelsString(a));
				((TextView) v.findViewById(R.id.name)).setText(a.getLabel());
				return v;
			}
		};
		applicationInfoManager.addListener(new DbChangeListener() {
			public void notifyDataSetChanged() {
				appsAdapter.clear();
				List<Application> appsArray = applicationInfoManager.getAppsArray(null);
				for (Application application : appsArray) {
					appsAdapter.add(application);
				}
				appsAdapter.notifyDataSetChanged();
			}
		});
		setListAdapter(appsAdapter);

		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Application app = apps.get(info.position);
		ApplicationContextMenuManager.singleton().createMenu(menu, app);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Application app = apps.get(info.position);
		ApplicationContextMenuManager.singleton().onContextItemSelected(item, app, this, chooseLabelDialog);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		new AppsReloader(this, false).reload();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		genericDialogManager.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return genericDialogManager.onCreateDialog(id);
	}

	public ChooseLabelDialogCreator getChooseLabelDialog() {
		return chooseLabelDialog;
	}

}