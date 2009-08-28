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

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.google.code.appsorganizer.model.Application;

public class AppsListActivity extends ListActivity implements DbChangeListener {

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private GenericDialogManager genericDialogManager;

	private ApplicationInfoManager applicationInfoManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Debug.startMethodTracing("splash");
		setContentView(R.layout.main);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				chooseLabelDialog.setCurrentApp(((Application) getListAdapter().getItem(position)));
				showDialog(chooseLabelDialog.getDialogId());
			}
		});

		// SharedPreferences pref = getPreferences(Activity.MODE_PRIVATE);
		// if (pref.getBoolean(ALERT_0_4_PREF, false)) {

		reload();

		// } else {
		// showDialog(1);
		// }

	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				pd.setMessage(getText(R.string.preparing_apps_list));
			} else if (msg.what == -2) {
				setListAdapter(appsAdapter);
			} else if (msg.what == -3) {
				pd.hide();
			} else {
				pd.setMessage(getString(R.string.total_apps) + ": " + msg.what);
			}
		}
	};

	private ProgressDialog pd;

	private ArrayAdapter<Application> appsAdapter;

	public void reload() {
		pd = ProgressDialog.show(this, getText(R.string.preparing_apps_list), getText(R.string.please_wait_loading), true, false);
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
				genericDialogManager = new GenericDialogManager(AppsListActivity.this);
				applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());
				dbHelper = DatabaseHelper.initOrSingleton(AppsListActivity.this);
				chooseLabelDialog = new ChooseLabelDialogCreator(dbHelper, applicationInfoManager);
				genericDialogManager.addDialog(chooseLabelDialog);

				applicationInfoManager.reloadAll(dbHelper.appCacheDao, dbHelper.labelDao, handler);
				handler.sendEmptyMessage(-1);

				final ArrayList<Application> appsArray = applicationInfoManager.getAppsArray();
				appsAdapter = new ArrayAdapter<Application>(AppsListActivity.this, R.layout.app_row, new ArrayList<Application>(appsArray)) {
					@Override
					public View getView(int position, View v, ViewGroup parent) {
						ViewHolder viewHolder;
						if (v == null) {
							LayoutInflater factory = LayoutInflater.from(getContext());
							v = factory.inflate(R.layout.app_row, null);
							viewHolder = new ViewHolder();
							viewHolder.image = (ImageView) v.findViewById(R.id.image);
							viewHolder.labels = (TextView) v.findViewById(R.id.labels);
							viewHolder.name = (TextView) v.findViewById(R.id.name);
							v.setTag(viewHolder);
						} else {
							viewHolder = (ViewHolder) v.getTag();
						}
						Application a = getItem(position);
						viewHolder.image.setImageDrawable(a.getIcon());
						viewHolder.labels.setText(a.getLabelListString());
						viewHolder.name.setText(a.getLabel());
						return v;
					}

				};
				handler.sendEmptyMessage(-2);

				registerForContextMenu(getListView());
				handler.sendEmptyMessage(-3);
				loadIcons(appsArray);
				applicationInfoManager.addListener(AppsListActivity.this);
			}
		};
		t.start();
	}

	static class ViewHolder {
		ImageView image;
		TextView labels;
		TextView name;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		applicationInfoManager.removeListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Application app = (Application) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.singleton().createMenu(menu, app.getLabel());
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Application app = (Application) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.singleton().onContextItemSelected(item, app, this, chooseLabelDialog);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ApplicationContextMenuManager.singleton().onActivityResult(this, requestCode, resultCode, data);
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

	private final Handler listHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
		}
	};

	public void dataSetChanged() {
		@SuppressWarnings("unchecked")
		ArrayAdapter<Application> appsAdapter = (ArrayAdapter<Application>) getListAdapter();
		appsAdapter.clear();
		final ArrayList<Application> appsArray = applicationInfoManager.getAppsArray();
		for (Application applicationBinding : appsArray) {
			appsAdapter.add(applicationBinding);
		}
		appsAdapter.notifyDataSetChanged();
		loadInconsInThread(appsArray);
	}

	private void loadInconsInThread(final ArrayList<Application> appBindingVector) {
		Thread t = new Thread() {
			@Override
			public void run() {
				loadIcons(appBindingVector);
			}
		};
		t.start();
	}

	private void loadIcons(final ArrayList<Application> appBindingVector) {
		int pos = 0;
		for (Application ab : appBindingVector) {
			if (ab.getIcon() == null) {
				ab.loadIcon(getPackageManager());
				ab.setIcon(ab.getIcon());
				pos++;
			}
			if (pos == 50) {
				listHandler.sendEmptyMessage(-1);
				pos = 0;
			}
		}
		if (pos > 0) {
			listHandler.sendEmptyMessage(-1);
		}
		// Debug.stopMethodTracing();
	}
}