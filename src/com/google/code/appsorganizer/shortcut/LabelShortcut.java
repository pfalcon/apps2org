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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.google.code.appsorganizer.ApplicationInfoManager;
import com.google.code.appsorganizer.ChooseAppsDialogCreator;
import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DatabaseHelperBasic;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.db.ObjectWithIdDao;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.Label;

public class LabelShortcut extends Activity implements DbChangeListener {

	public static final long ALL_LABELS_ID = -2l;
	public static final long ALL_STARRED_ID = -3l;
	public static final String LABEL_ID = "com.example.android.apis.app.LauncherShortcuts";

	private DatabaseHelperBasic dbHelper;

	private long labelId;

	private boolean allLabelsSelected;

	private GridView grid;

	private CheckBox starCheck;

	private TextView titleView;

	private ChooseAppsDialogCreator chooseAppsDialogCreator;

	private GenericDialogManager genericDialogManager;

	private boolean onlyStarred;

	private static boolean firstTime = true;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// Debug.startMethodTracing("grid1");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getOrCreateGrid();

		final Intent intent = getIntent();

		labelId = intent.getLongExtra(LABEL_ID, 2);// ALL_STARRED_ID);
		allLabelsSelected = labelId == ALL_LABELS_ID;
		ApplicationInfoManager.addListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new LoadIconTask().execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ApplicationInfoManager.removeListener(this);
		dbHelper.close();
	}

	private Cursor cursor;

	private String[] iconsToLoad;

	private void reloadGrid() {
		if (labelId == ALL_LABELS_ID) {
			cursor = dbHelper.getDb().query(LabelDao.TABLE_NAME,
					new String[] { LabelDao.ID_COL_NAME, LabelDao.LABEL_COL_NAME, LabelDao.ICON_COL_NAME }, null, null, null, null,
					("upper(" + LabelDao.LABEL_COL_NAME + ")"));

		} else {
			if (labelId == ALL_STARRED_ID) {
				cursor = dbHelper.getDb().rawQuery("select label, package, name from apps where starred = 1 order by label", null);
			} else {
				cursor = dbHelper.getDb()
						.rawQuery(
								"select a.label, a.package, a.name from apps a inner join apps_labels al "
										+ "on a.name = al.app where id_label = ? " + (onlyStarred ? "and a.starred = 1" : "")
										+ " order by a.label", new String[] { Long.toString(labelId) });
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
	}

	private String retrieveTitle() {
		String title = "";
		if (labelId == ALL_LABELS_ID) {
			title = getString(R.string.all_labels);
		} else if (labelId == ALL_STARRED_ID) {
			title = getString(R.string.Starred_apps);
		} else {
			Cursor c = dbHelper.getDb().query(LabelDao.TABLE_NAME, new String[] { LabelDao.LABEL_COL_NAME }, LabelDao.ID_COL_NAME + "=?",
					new String[] { Long.toString(labelId) }, null, null, null);
			try {
				if (c.moveToNext()) {
					title = c.getString(0);
				}
			} finally {
				c.close();
			}
		}
		return title;
	}

	private String title;

	private static final int CHANGE_CURSOR = -1;
	private static final int DATASET_CHANGED = -2;
	private static final int CHANGE_TITLE = -3;

	private class LoadIconTask extends AsyncTask<String, Integer, Object> {

		@Override
		protected Object doInBackground(String... ss) {
			if (dbHelper == null) {
				dbHelper = new DatabaseHelperBasic(LabelShortcut.this);
			}
			title = retrieveTitle();
			publishProgress(CHANGE_TITLE);

			reloadGrid();
			publishProgress(CHANGE_CURSOR);
			if (labelId != ALL_LABELS_ID) {
				int tot = 0;
				for (int i = 0; i < iconsToLoad.length; i++) {
					String componentName = iconsToLoad[i];
					if (Application.getIconFromCache(componentName) == null) {
						Application.loadIcon(getPackageManager(), componentName);
						tot++;
						if (tot % 4 == 0) {
							publishProgress(DATASET_CHANGED);
						}
					}
				}
				if (tot % 4 != 0) {
					publishProgress(DATASET_CHANGED);
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			updateView(progress[0]);
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
							int ic = Label.convertToIcon(cursor.getInt(columnIndex));
							if (ic > 0) {
								icon.setImageResource(ic);
							} else {
								icon.setImageResource(R.drawable.icon_default);
							}
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
				return true;
			}
		});
		grid.setAdapter(cursorAdapter);
	}

	private SimpleCursorAdapter cursorAdapter;

	private GridView getOrCreateGrid() {
		if (grid == null) {
			setContentView(R.layout.shortcut_grid);

			grid = (GridView) findViewById(R.id.shortcutGrid);

			grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
					Cursor item = (Cursor) cursorAdapter.getItem(pos);
					if (labelId == ALL_LABELS_ID) {
						labelId = item.getLong(0);
						new LoadIconTask().execute();
					} else {
						Intent i = new Intent(Intent.ACTION_MAIN);
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						i.setClassName(item.getString(3), item.getString(4));
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					}
				}
			});
			if (!firstTime) {
				setVisible(false);
			}
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
				new LoadIconTask().execute();
				return true;
			}
		}
		return false;
	}

	public void dataSetChanged(Object source, short type) {
		new LoadIconTask().execute();
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
		initDialogs();
		chooseAppsDialogCreator.setCurrentLabelId(labelId);
		showDialog(chooseAppsDialogCreator.getDialogId());
	}

	private void initDialogs() {
		if (chooseAppsDialogCreator == null) {
			chooseAppsDialogCreator = new ChooseAppsDialogCreator(DatabaseHelper.initOrSingleton(this), getPackageManager());
			genericDialogManager = new GenericDialogManager(this);
			genericDialogManager.addDialog(chooseAppsDialogCreator);
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		initDialogs();
		genericDialogManager.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		initDialogs();
		return genericDialogManager.onCreateDialog(id);
	}

	private void updateTitleView() {
		if (titleView == null) {
			LayoutInflater factory = LayoutInflater.from(LabelShortcut.this);
			View titleLayout = factory.inflate(R.layout.shortcut_grid_title, null);
			titleLayout.setBackgroundColor(Color.GRAY);
			titleView = (TextView) titleLayout.findViewById(R.id.title);
			starCheck = (CheckBox) titleLayout.findViewById(R.id.starCheck);
			starCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					onlyStarred = isChecked;
					new LoadIconTask().execute();
				}
			});
			LinearLayout layout = (LinearLayout) findViewById(R.id.shortcutLayout);
			layout.addView(titleLayout, 0, new LayoutParams(LayoutParams.FILL_PARENT, 34));

			findViewById(R.id.closeButton).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			});
		}
		titleView.setText(title);
		int v = labelId > 0 ? View.VISIBLE : View.INVISIBLE;
		if (starCheck.getVisibility() != v) {
			starCheck.setVisibility(v);
		}
	}

	private void updateView(int p) {
		if (p == DATASET_CHANGED) {
			cursorAdapter.notifyDataSetChanged();
		} else if (p == CHANGE_CURSOR) {
			if (cursorAdapter == null) {
				createAdapter(cursor);

			} else {
				cursorAdapter.changeCursor(cursor);
			}
			if (!firstTime) {
				setVisible(true);
			} else {
				firstTime = false;
			}
		} else {
			updateTitleView();
			// Debug.stopMethodTracing();
		}
	}
}
