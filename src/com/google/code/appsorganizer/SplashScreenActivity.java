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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ToggleButton;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.ListActivityWithDialog;
import com.google.code.appsorganizer.dialogs.SimpleDialog;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.service.StartupListener;

public class SplashScreenActivity extends ListActivityWithDialog implements DbChangeListener {

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private ApplicationInfoManager applicationInfoManager;

	private ToggleButton labelButton;

	private ToggleButton appButton;

	private OptionMenuManager optionMenuManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugReportActivity.registerExceptionHandler(this);

		// Debug.startMethodTracing("splash");

		applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());
		dbHelper = DatabaseHelper.initOrSingleton(SplashScreenActivity.this);
		optionMenuManager = new OptionMenuManager(SplashScreenActivity.this, dbHelper);

		chooseLabelDialog = new ChooseLabelDialogCreator(getGenericDialogManager(), dbHelper, applicationInfoManager);

		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				chooseLabelDialog.setCurrentApp(((Application) getListAdapter().getItem(position)));
				showDialog(chooseLabelDialog);
			}
		});
		getListView().setClickable(true);

		labelButton = (ToggleButton) findViewById(R.id.labelButton);
		appButton = (ToggleButton) findViewById(R.id.appButton);
		labelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(SplashScreenActivity.this, LabelListActivity.class));
			}
		});

		setProgressBarIndeterminateVisibility(true);
		reload();
		showStartHowTo();
	}

	private void showStartHowTo() {
		SharedPreferences settings = getSharedPreferences("appsOrganizer_pref", 0);
		boolean showStartHowTo = settings.getBoolean("showStartHowTo", true);
		if (showStartHowTo) {
			String msg = getString(R.string.how_to_message) + "\n" + getString(R.string.how_to_message_2);

			SimpleDialog howToDialog = new SimpleDialog(getGenericDialogManager(), getString(R.string.app_name), msg);
			howToDialog.setIcon(R.drawable.icon);
			howToDialog.setShowNegativeButton(false);

			showDialog(howToDialog);

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("showStartHowTo", false);
			editor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (labelButton.isChecked()) {
			labelButton.setChecked(false);
		}
		if (!appButton.isChecked()) {
			appButton.setChecked(true);
		}
		BugReportActivity.showLastException(this);
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

				applicationInfoManager.reloadAll(dbHelper, handler, false);
				handler.sendEmptyMessage(-1);

				final Application[] apps = applicationInfoManager.getAppsArray();
				ArrayList<Application> l = new ArrayList<Application>(apps.length);
				for (int i = 0; i < apps.length; i++) {
					l.add(apps[i]);
				}
				appsAdapter = new ArrayAdapter<Application>(SplashScreenActivity.this, R.layout.app_row, l) {
					@Override
					public View getView(int position, View v, ViewGroup parent) {
						return getItem(position).getAppView(SplashScreenActivity.this, dbHelper, applicationInfoManager, v,
								chooseLabelDialog);
					}

				};
				appsAdapter.setNotifyOnChange(false);
				handler.sendEmptyMessage(-2);

				registerForContextMenu(getListView());
				handler.sendEmptyMessage(-3);
				loadIcons(apps);
				ApplicationInfoManager.addListener(SplashScreenActivity.this);
			}
		};
		t.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ApplicationInfoManager.removeListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Application app = (Application) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.singleton().createMenu(menu, app);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Application app = (Application) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.singleton().onContextItemSelected(item, app, this, chooseLabelDialog, applicationInfoManager);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ApplicationContextMenuManager.singleton().onActivityResult(this, requestCode, resultCode, data);
	}

	private final Handler listHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			} else if (msg.what == -2) {
				setProgressBarIndeterminateVisibility(false);
				StartupListener.startService(SplashScreenActivity.this);
			}
		}
	};

	public void dataSetChanged(Object source, short type) {
		if (type == CHANGED_STARRED) {
			if (source != this) {
				appsAdapter.notifyDataSetChanged();
			}
		} else {
			appsAdapter.setNotifyOnChange(false);
			appsAdapter.clear();
			Application[] appsArray = applicationInfoManager.getAppsArray();
			for (int i = 0; i < appsArray.length; i++) {
				appsAdapter.add(appsArray[i]);
			}
			appsAdapter.notifyDataSetChanged();
			loadInconsInThread(appsArray);
		}
	}

	private void loadInconsInThread(final Application[] appsArray) {
		Thread t = new Thread() {
			@Override
			public void run() {
				loadIcons(appsArray);
			}
		};
		t.start();
	}

	private void loadIcons(final Application[] appsArray) {
		int pos = 0;
		for (Application ab : appsArray) {
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
		listHandler.sendEmptyMessage(-2);
		// Debug.stopMethodTracing();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return optionMenuManager.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionMenuManager.onOptionsItemSelected(item);
	}

}