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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.ToggleButton;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.ListActivityWithDialog;
import com.google.code.appsorganizer.dialogs.SimpleDialog;

public class SplashScreenActivity extends ListActivityWithDialog implements DbChangeListener {

	private static final String SHOW_START_HOW_TO = "showStartHowTo";

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private ToggleButton labelButton;

	private ToggleButton appButton;

	private OptionMenuManager optionMenuManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugReportActivity.registerExceptionHandler(this);
		// provaDownload();
		// Debug.startMethodTracing("splash");

		dbHelper = DatabaseHelper.initOrSingleton(SplashScreenActivity.this);
		optionMenuManager = new OptionMenuManager(SplashScreenActivity.this, dbHelper);

		chooseLabelDialog = new ChooseLabelDialogCreator(getGenericDialogManager());

		setContentView(R.layout.main);

		getListView().setClickable(true);

		labelButton = (ToggleButton) findViewById(R.id.labelButton);
		appButton = (ToggleButton) findViewById(R.id.appButton);
		labelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(SplashScreenActivity.this, LabelListActivity.class));
			}
		});

		reload();
		showStartHowTo();
	}

	// private void provaDownload() {
	// try {
	// URL u = new
	// URL("http://www.cyrket.com/package/com.google.code.appsorganizer");
	// HttpURLConnection c = (HttpURLConnection) u.openConnection();
	// c.setRequestMethod("GET");
	// c.setDoOutput(true);
	// c.connect();
	//
	// BufferedReader in = new BufferedReader(new
	// InputStreamReader(c.getInputStream()));
	//
	// String s = null;
	// while ((s = in.readLine()) != null) {
	// int indexOf = s.indexOf("<label>Category</label>");
	// if (indexOf != -1) {
	// String category = in.readLine().trim();
	// System.out.println(category);
	// break;
	// }
	// }
	// in.close();
	// } catch (IOException e) {
	// }
	// }

	private void showStartHowTo() {
		SharedPreferences settings = getSharedPreferences("appsOrganizer_pref", 0);
		boolean showStartHowTo = settings.getBoolean(SHOW_START_HOW_TO, true);
		String msg = getString(R.string.how_to_message) + "\n" + getString(R.string.how_to_message_2);

		SimpleDialog howToDialog = new SimpleDialog(getGenericDialogManager(), getString(R.string.app_name), msg);
		howToDialog.setIcon(R.drawable.icon);
		howToDialog.setShowNegativeButton(false);

		if (showStartHowTo) {
			showDialog(howToDialog);

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(SHOW_START_HOW_TO, false);
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
				Cursor c = dbHelper.appCacheDao.getAllApps(ApplicationViewBinder.COLS);
				createAppsAdapter(c);
				// } else if (msg.what == -2) {
				setListAdapter(appsAdapter);
			} else if (msg.what == -3) {
				pd.hide();
			} else {
				pd.setMessage(getString(R.string.total_apps) + ": " + msg.what);
			}
		}
	};

	private ProgressDialog pd;

	private SimpleCursorAdapter appsAdapter;

	public void reload() {
		pd = ProgressDialog.show(this, getText(R.string.preparing_apps_list), getText(R.string.please_wait_loading), true, false);
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
				ApplicationInfoManager.reloadAll(getPackageManager(), dbHelper, handler, false);
				handler.sendEmptyMessage(-1);

				registerForContextMenu(getListView());
				handler.sendEmptyMessage(-3);
				ApplicationChangeListenerManager.addListener(SplashScreenActivity.this);
			}
		};
		t.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ApplicationChangeListenerManager.removeListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		SQLiteCursor c = (SQLiteCursor) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.createMenu(menu, c.getString(1));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		SQLiteCursor c = (SQLiteCursor) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.onContextItemSelected(item, c.getString(5), c.getString(2), this, chooseLabelDialog);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ApplicationContextMenuManager.onActivityResult(this, requestCode, resultCode, data);
	}

	public void dataSetChanged(Object source, short type) {
		if (appsAdapter != null) {
			appsAdapter.getCursor().requery();
		}
		// TODO si puo' gestire meglio
		// if (type == CHANGED_STARRED) {
		// if (source != this) {
		// appsAdapter.notifyDataSetChanged();
		// }
		// } else {
		// appsAdapter.runQueryOnBackgroundThread(null);
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return optionMenuManager.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionMenuManager.onOptionsItemSelected(item);
	}

	private void createAppsAdapter(Cursor c) {
		appsAdapter = new SimpleCursorAdapter(SplashScreenActivity.this, R.layout.app_row, c, ApplicationViewBinder.COLS,
				ApplicationViewBinder.VIEWS) {
		};
		appsAdapter.setViewBinder(new ApplicationViewBinder(dbHelper, this, chooseLabelDialog));
	}

}