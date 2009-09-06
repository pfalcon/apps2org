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
import android.content.ComponentName;
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
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.Label;

public class LabelShortcut extends Activity implements DbChangeListener {

	private static final String TITLE_BUNDLE_KEY = "title";
	private static final int SET_TITLE = -2;
	private static final int CHANGE_CURSOR = -1;
	private static final String ID_COL = "_id";
	private static final String NAME_COL = "name";
	private static final String LABEL_COL = "label";
	private static final String IMAGE_COL = "image";
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
					// cursorAdapter.notifyDataSetChanged();
				}
			} else if (msg.what == SET_TITLE) {
				titleView.setText(msg.getData().getString(TITLE_BUNDLE_KEY));
				starCheck.setVisibility(labelId > 0 ? View.VISIBLE : View.INVISIBLE);
			}
		}
	};

	private void reloadGrid() {
		handler.sendMessage(getTitleMessage());

		if (labelId == ALL_LABELS_ID) {
			Label[] labels = dbHelper.labelDao.getLabelsArray();
			cursor = createCursor(labels);
		} else {
			if (labelId == ALL_STARRED_ID) {
				cursor = dbHelper.appCacheDao.getStarredApps();
			} else {
				cursor = dbHelper.appsLabelDao.getAppsCursor(labelId, onlyStarred);
			}
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

	private static class IconComp {
		ComponentName componentName;
		ImageView imageView;
		Drawable drawable;

		public IconComp(ComponentName componentName, ImageView imageView) {
			this.componentName = componentName;
			this.imageView = imageView;
		}
	}

	private class LoadIconTask extends AsyncTask<IconComp, IconComp, Long> {
		@Override
		protected Long doInBackground(IconComp... icons) {
			for (int i = 0; i < icons.length; i++) {
				IconComp iconComp = icons[i];
				ComponentName componentName = iconComp.componentName;
				iconComp.drawable = Application.loadIcon(getPackageManager(), componentName);
				publishProgress(iconComp);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(IconComp... progress) {
			for (int i = 0; i < progress.length; i++) {
				IconComp ic = progress[i];
				ic.imageView.setImageDrawable(ic.drawable);
			}
		}
	}

	private void createAdapter(Cursor cursor) {
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.app_cell_with_icon, cursor, new String[] { IMAGE_COL, LABEL_COL },
				new int[] { R.id.image, R.id.name });
		cursorAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(final View view, Cursor cursor, int columnIndex) {
				if (columnIndex == 1) {
					if (!cursor.isNull(columnIndex)) {
						ImageView icon = (ImageView) view;
						icon.setImageResource(cursor.getInt(columnIndex));
					} else {
						String appName = cursor.getString(4);
						String packageName = cursor.getString(3);
						final ComponentName componentName = new ComponentName(packageName, appName);
						Drawable drawable = Application.getIconFromCache(componentName);
						if (drawable == null) {
							new LoadIconTask().execute(new IconComp(componentName, (ImageView) view));
						} else {
							((ImageView) view).setImageDrawable(drawable);
						}
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

	private Cursor createCursor(Label[] labels) {
		MatrixCursor c = new MatrixCursor(new String[] { ID_COL, IMAGE_COL, LABEL_COL, NAME_COL }, labels.length);
		for (int i = 0; i < labels.length; i++) {
			Label l = labels[i];
			c.addRow(new Object[] { l.getId(), l.getIcon(), l.getLabel(), null });
		}
		return c;
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
