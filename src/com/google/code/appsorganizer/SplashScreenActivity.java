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
import android.content.DialogInterface;
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
import com.google.code.appsorganizer.dialogs.ListActivityWithDialog;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.SimpleDialog;

public class SplashScreenActivity extends ListActivityWithDialog {

	private static final String SHOW_START_HOW_TO = "showStartHowTo";
	private static final String SHOW_FIRST_TIME_DOWNLOAD = "showFirstTimeDownload";

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private ToggleButton labelButton;

	private ToggleButton appButton;

	private OptionMenuManager optionMenuManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugReportActivity.registerExceptionHandler(this);
		// Debug.startMethodTracing("splash");

		dbHelper = DatabaseHelper.initOrSingleton(SplashScreenActivity.this);
		optionMenuManager = new OptionMenuManager(SplashScreenActivity.this, dbHelper, new OnOkClickListener() {
			private static final long serialVersionUID = 1L;

			public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
				requeryCursor();
			}
		});

		chooseLabelDialog = new ChooseLabelDialogCreator(getGenericDialogManager(), new OnOkClickListener() {
			private static final long serialVersionUID = 1L;

			public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
				requeryCursor();
			}
		});

		setContentView(R.layout.main);

		getListView().setClickable(true);

		registerForContextMenu(getListView());

		labelButton = (ToggleButton) findViewById(R.id.labelButton);
		appButton = (ToggleButton) findViewById(R.id.appButton);
		labelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(SplashScreenActivity.this, LabelListActivity.class));
			}
		});

		createHowToDialog();
		createFirstTimeDownloadDialog();
	}

	private SimpleDialog howToDialog;

	private SimpleDialog firstTimeDownloadDialog;

	private boolean showFirstTimeDownloadDialog() {
		return false;
		// SharedPreferences settings =
		// getSharedPreferences("appsOrganizer_pref", 0);
		// boolean firstTime = settings.getBoolean(SHOW_FIRST_TIME_DOWNLOAD,
		// true);
		//
		// if (firstTime) {
		// firstTimeDownloadDialog.showDialog();
		//
		// SharedPreferences.Editor editor = settings.edit();
		// editor.putBoolean(SHOW_FIRST_TIME_DOWNLOAD, false);
		// editor.commit();
		// }
		// return firstTime;
	}

	private void createFirstTimeDownloadDialog() {
		String msg = getString(R.string.First_time_download_message_1) + "\n" + getString(R.string.First_time_download_message_2);

		firstTimeDownloadDialog = new SimpleDialog(getGenericDialogManager(), getString(R.string.app_name), msg, new OnOkClickListener() {
			private static final long serialVersionUID = 1L;

			public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
				optionMenuManager.startDownload();
			}
		});
		firstTimeDownloadDialog.setIcon(R.drawable.icon);
		firstTimeDownloadDialog.setShowNegativeButton(true);
	}

	private boolean showStartHowTo() {
		SharedPreferences settings = getSharedPreferences("appsOrganizer_pref", 0);
		boolean showStartHowTo = settings.getBoolean(SHOW_START_HOW_TO, true);

		if (showStartHowTo) {
			howToDialog.showDialog();

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(SHOW_START_HOW_TO, false);
			editor.commit();
		}
		return showStartHowTo;
	}

	private void createHowToDialog() {
		String msg = getString(R.string.how_to_message) + "\n" + getString(R.string.how_to_message_2);

		howToDialog = new SimpleDialog(getGenericDialogManager(), getString(R.string.app_name), msg);
		howToDialog.setIcon(R.drawable.icon);
		howToDialog.setShowNegativeButton(false);
	}

	private static boolean firstTime = true;

	@Override
	protected void onResume() {
		super.onResume();
		if (labelButton.isChecked()) {
			labelButton.setChecked(false);
		}
		if (!appButton.isChecked()) {
			appButton.setChecked(true);
		}
		if (firstTime) {
			reload();
			firstTime = false;
		} else {
			initAdapter();
		}
		BugReportActivity.showLastException(this);
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 != 0) {
				pd.setMax(msg.arg1);
			} else if (msg.what == -3) {
				pd.setMessage(getText(R.string.preparing_apps_list));
				initAdapter();
				pd.hide();
				if (!showFirstTimeDownloadDialog()) {
					showStartHowTo();
				}
			} else {
				pd.incrementProgressBy(1);
				pd.setMessage(msg.obj.toString());
			}
		}
	};

	private ProgressDialog pd;

	@Override
	public SimpleCursorAdapter getListAdapter() {
		return (SimpleCursorAdapter) super.getListAdapter();
	}

	private void reload() {
		pd = new ProgressDialog(this);
		pd.setTitle(getText(R.string.preparing_apps_list));
		pd.setMessage(getText(R.string.please_wait_loading));
		pd.setIndeterminate(false);
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.show();
		Thread t = new Thread() {
			@Override
			public void run() {
				ApplicationInfoManager.reloadAll(getPackageManager(), dbHelper, handler, false);
				handler.sendEmptyMessage(-3);
			}
		};
		t.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
		if (ApplicationContextMenuManager.onActivityResult(this, requestCode, resultCode, data)) {
			requeryCursor();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return optionMenuManager.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionMenuManager.onOptionsItemSelected(item);
	}

	private void requeryCursor() {
		SimpleCursorAdapter listAdapter = getListAdapter();
		if (listAdapter != null) {
			Cursor cursor = listAdapter.getCursor();
			if (cursor != null) {
				cursor.requery();
			}
		}
	}

	private void initAdapter() {
		Cursor c = dbHelper.appCacheDao.getAllApps(ApplicationViewBinder.COLS);
		startManagingCursor(c);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(SplashScreenActivity.this, R.layout.app_row, c, ApplicationViewBinder.COLS,
				ApplicationViewBinder.VIEWS) {
		};
		adapter.setViewBinder(new ApplicationViewBinder(dbHelper, this, chooseLabelDialog));
		setListAdapter(adapter);
	}

}