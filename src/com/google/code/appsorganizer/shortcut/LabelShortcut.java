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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.google.code.appsorganizer.ChooseAppsDialogCreator;
import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.db.ObjectWithIdDao;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.Label;

public class LabelShortcut extends Activity implements DbChangeListener {

	private static final String TITLE_BUNDLE_KEY = "title";
	private static final int SET_TITLE = -2;
	private static final int CHANGE_CURSOR = -1;
	public static final long ALL_LABELS_ID = -2l;
	public static final long ALL_STARRED_ID = -3l;
	public static final String LABEL_ID = "com.example.android.apis.app.LauncherShortcuts";

	private DatabaseHelper dbHelper;

	private long labelId;

	private boolean allLabelsSelected;

	private TextView titleView;

	private ChooseAppsDialogCreator chooseAppsDialogCreator;

	private GenericDialogManager genericDialogManager;

	private boolean onlyStarred;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// Debug.startMethodTracing("grid");
		genericDialogManager = new GenericDialogManager(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getOrCreateGrid();
		View titleLayout = findViewById(R.id.titleLayout);
		titleLayout.setBackgroundColor(Color.GRAY);
		titleView = (TextView) findViewById(R.id.title);
		starCheck = (CheckBox) findViewById(R.id.starCheck);
		starCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onlyStarred = isChecked;
				reloadGrid();
			}
		});

		findViewById(R.id.closeButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		// setVisible(false);
		// if (ApplicationInfoManager.isSingletonNull()) {
		// pd = ProgressDialog.show(this, getText(R.string.preparing_apps_list),
		// getText(R.string.loading_shortcut_grid), true, false);
		// showProgress = true;
		// } else {
		// }

		dbHelper = DatabaseHelper.initOrSingleton(LabelShortcut.this);

		chooseAppsDialogCreator = new ChooseAppsDialogCreator(dbHelper, getPackageManager());
		genericDialogManager.addDialog(chooseAppsDialogCreator);

		final Intent intent = getIntent();

		labelId = intent.getLongExtra(LABEL_ID, 2);// ALL_STARRED_ID);
		allLabelsSelected = labelId == ALL_LABELS_ID;
	}

	@Override
	protected void onResume() {
		super.onResume();
		reloadGridInThread();
	}

	private void reloadGridInThread() {
		new Thread() {
			@Override
			public void run() {
				reloadGrid();
			}
		}.start();
	}

	private Cursor cursor;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == CHANGE_CURSOR) {
				if (cursorAdapter == null) {
					createAdapter(cursor);

				} else {
					cursorAdapter.changeCursor(cursor);
				}
				if (labelId != ALL_LABELS_ID) {
					new LoadIconTask().execute(iconsToLoad);
				}
			} else if (msg.what == SET_TITLE) {
				titleView.setText(msg.getData().getString(TITLE_BUNDLE_KEY));
				starCheck.setVisibility(labelId > 0 ? View.VISIBLE : View.INVISIBLE);
			}
		}
	};

	private String[] iconsToLoad;

	private void reloadGrid() {
		handler.sendMessage(getTitleMessage());
		// Debug.startMethodTracing("grid");

		if (labelId == ALL_LABELS_ID) {
			cursor = dbHelper.labelDao.getLabelCursor();
		} else {
			if (labelId == ALL_STARRED_ID) {
				cursor = dbHelper.appCacheDao.getStarredApps();
			} else {
				cursor = dbHelper.appsLabelDao.getAppsCursor(labelId, onlyStarred);
			}
			int count = cursor.getCount();
			MatrixCursor m = new MatrixCursor(new String[] { ObjectWithIdDao.ID_COL_NAME, LabelDao.LABEL_COL_NAME, LabelDao.ICON_COL_NAME,
					AppCacheDao.PACKAGE_NAME_COL_NAME, AppCacheDao.NAME_COL_NAME }, count);
			iconsToLoad = new String[count];
			int i = 0;
			try {
				while (cursor.moveToNext()) {
					String packageName = cursor.getString(1);
					String name = cursor.getString(2);
					m.addRow(new Object[] { i, cursor.getString(0), null, packageName, name });
					iconsToLoad[i++] = packageName + Application.SEPARATOR + name;
				}
			} finally {
				cursor.close();
			}
			cursor = m;
		}
		handler.sendEmptyMessage(CHANGE_CURSOR);
	}

	private Message getTitleMessage() {
		String title;
		if (labelId == ALL_LABELS_ID) {
			title = getString(R.string.all_labels);
		} else if (labelId == ALL_STARRED_ID) {
			title = getString(R.string.Starred_apps);
		} else {
			Label label = dbHelper.labelDao.queryById(labelId);
			title = label.getLabel();
		}
		Message msg = new Message();
		Bundle bundle = new Bundle(1);
		bundle.putString(TITLE_BUNDLE_KEY, title);
		msg.setData(bundle);
		msg.what = SET_TITLE;
		return msg;
	}

	private class LoadIconTask extends AsyncTask<String, Object, Object> {
		@Override
		protected Object doInBackground(String... comps) {
			int tot = 0;
			for (int i = 0; i < comps.length; i++) {
				String componentName = comps[i];
				if (Application.getIconFromCache(componentName) == null) {
					Application.loadIcon(getPackageManager(), componentName);
					tot++;
					if (tot % 4 == 0) {
						publishProgress((Object) null);
					}
				}
			}
			if (tot % 4 != 0) {
				publishProgress((Object) null);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... progress) {
			cursorAdapter.notifyDataSetChanged();
		}
	}

	private void createAdapter(Cursor cursor) {
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.app_cell_with_icon, cursor, new String[] { LabelDao.ICON_COL_NAME,
				LabelDao.LABEL_COL_NAME }, new int[] { R.id.image, R.id.name });
		cursorAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(final View view, Cursor cursor, int columnIndex) {
				if (columnIndex == 2) {
					if (cursor.getColumnCount() == 3) {
						ImageView icon = (ImageView) view;
						if (cursor.isNull(columnIndex)) {
							icon.setImageResource(R.drawable.icon_default);
						} else {
							icon.setImageResource(Label.convertToIcon(cursor.getInt(columnIndex)));
						}
					} else {
						String appName = cursor.getString(4);
						String packageName = cursor.getString(3);
						Drawable drawable = Application.getIconFromCache(packageName, appName);
						((ImageView) view).setImageDrawable(drawable);
					}
				} else {
					((TextView) view).setText(cursor.getString(columnIndex));
				}
				// if (cursor.getString(2).equals("TestFileManager")) {
				// Debug.stopMethodTracing();
				// }
				return true;
			}
		});
		grid.setAdapter(cursorAdapter);
	}

	private GridView grid;

	private View mainView;
	private CheckBox starCheck;
	private SimpleCursorAdapter cursorAdapter;

	private GridView getOrCreateGrid() {
		if (grid == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			mainView = layoutInflater.inflate(R.layout.shortcut_grid, null);
			setContentView(mainView);

			grid = (GridView) findViewById(R.id.shortcutGrid);
			grid.setColumnWidth(65);

			grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
					Cursor item = (Cursor) cursorAdapter.getItem(pos);
					if (labelId == ALL_LABELS_ID) {
						labelId = item.getLong(0);
						reloadGridInThread();
					} else {
						Intent i = new Intent(Intent.ACTION_MAIN);
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						i.setClassName(item.getString(3), item.getString(4));
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					}
				}
			});
		}
		return grid;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (onBack()) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean onBack() {
		if (allLabelsSelected) {
			if (labelId != ALL_LABELS_ID) {
				labelId = ALL_LABELS_ID;
				reloadGridInThread();
				return true;
			}
		}
		return false;
	}

	public void dataSetChanged(Object source, short type) {
		reloadGrid();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (labelId != ALL_LABELS_ID && labelId != ALL_STARRED_ID) {
			showChooseAppsDialog();
		}
		return false;
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// menu.add(0, 0, 0, R.string.select_apps);
	// return true;
	// }
	//
	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// return label != null && labelId != ALL_LABELS_ID;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case 0:
	// showChooseAppsDialog();
	// return true;
	// }
	// return false;
	// }

	private void showChooseAppsDialog() {
		chooseAppsDialogCreator.setCurrentLabelId(labelId);
		showDialog(chooseAppsDialogCreator.getDialogId());
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		genericDialogManager.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return genericDialogManager.onCreateDialog(id);
	}
}
