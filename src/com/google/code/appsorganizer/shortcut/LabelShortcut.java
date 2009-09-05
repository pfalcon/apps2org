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
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.GridView;
import android.widget.TextView;

import com.google.code.appsorganizer.ApplicationInfoManager;
import com.google.code.appsorganizer.ChooseAppsDialogCreator;
import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.GridObject;
import com.google.code.appsorganizer.model.Label;

public class LabelShortcut extends Activity implements DbChangeListener {

	public static final long ALL_LABELS_ID = -2l;
	public static final String LABEL_ID = "com.example.android.apis.app.LauncherShortcuts";

	private ApplicationInfoManager applicationInfoManager;

	private DatabaseHelper dbHelper;

	private Label label;

	private boolean allLabelsSelected;

	private static Label ALL_LABELS;

	private TextView titleView;

	private ChooseAppsDialogCreator chooseAppsDialogCreator;

	private GenericDialogManager genericDialogManager;

	public static Drawable DRAWABLE_DEFAULT;

	private boolean showProgress;

	private ProgressDialog pd;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				((AppGridAdapter<?>) grid.getAdapter()).notifyDataSetChanged();
				setVisible(true);
				if (showProgress) {
					pd.hide();
				}
			} else if (msg.what == -2) {
				titleView.setText(label.getName());
			} else if (msg.what == -3) {
				((AppGridAdapter<?>) grid.getAdapter()).notifyDataSetChanged();
			}
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		genericDialogManager = new GenericDialogManager(this);

		// Debug.startMethodTracing("grid");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getOrCreateGrid();
		View titleLayout = findViewById(R.id.titleLayout);
		titleLayout.setBackgroundColor(Color.GRAY);
		titleView = (TextView) findViewById(R.id.title);

		findViewById(R.id.closeButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!onBack()) {
					finish();
				}
			}
		});

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
				if (DRAWABLE_DEFAULT == null) {
					DRAWABLE_DEFAULT = getResources().getDrawable(R.drawable.icon_default);
				}

				applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());
				applicationInfoManager.addListener(LabelShortcut.this);

				dbHelper = DatabaseHelper.initOrSingleton(LabelShortcut.this);
				applicationInfoManager.getOrReloadAppsMap(dbHelper.appCacheDao);

				chooseAppsDialogCreator = new ChooseAppsDialogCreator(dbHelper, applicationInfoManager);
				genericDialogManager.addDialog(chooseAppsDialogCreator);

				if (ALL_LABELS == null) {
					ALL_LABELS = new Label(LabelShortcut.ALL_LABELS_ID, getString(R.string.all_labels), R.drawable.icon);
				}
				final Intent intent = getIntent();

				long labelId = intent.getLongExtra(LABEL_ID, 2);// ALL_LABELS_ID);
				if (labelId == ALL_LABELS_ID) {
					allLabelsSelected = true;
					label = ALL_LABELS;
				} else {
					allLabelsSelected = false;
					label = dbHelper.labelDao.queryById(labelId);
				}
				reloadGrid();
			}
		};
		setVisible(false);
		if (ApplicationInfoManager.isSingletonNull()) {
			pd = ProgressDialog.show(this, getText(R.string.preparing_apps_list), getText(R.string.loading_shortcut_grid), true, false);
			showProgress = true;
		} else {
			showProgress = false;
		}
		t.start();
		// Debug.stopMethodTracing();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		applicationInfoManager.removeListener(this);
	}

	private void reloadGridInThread() {
		new Thread() {
			@Override
			public void run() {
				reloadGrid();
			}
		}.start();
	}

	private void reloadGrid() {
		if (label != null) {
			handler.sendEmptyMessage(-2);

			@SuppressWarnings("unchecked")
			final AppGridAdapter<GridObject> gridAdapter = (AppGridAdapter<GridObject>) grid.getAdapter();
			if (label.getId() == ALL_LABELS_ID) {
				Label[] labels = dbHelper.labelDao.getLabelsArray();
				gridAdapter.setObjectList(labels);
				handler.sendEmptyMessage(-1);
			} else {
				String[] apps = dbHelper.appsLabelDao.getAppNames(label.getId());
				Application[] newList = applicationInfoManager.convertToApplicationListNoIgnored(apps);
				gridAdapter.setObjectList(newList);
				handler.sendEmptyMessage(-1);
				int pos = 0;
				for (Application a : newList) {
					if (a.getIcon() == null) {
						a.loadIcon(getPackageManager());
						pos++;
						if (pos == 20) {
							handler.sendEmptyMessage(-3);
							pos = 0;
						}
					}
				}
				if (pos > 0) {
					handler.sendEmptyMessage(-3);
				}
			}
		} else {
			handler.sendEmptyMessage(-1);
		}
	}

	private GridView grid;

	private View mainView;

	private GridView getOrCreateGrid() {
		if (grid == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			mainView = layoutInflater.inflate(R.layout.shortcut_grid, null);
			setContentView(mainView);

			grid = (GridView) findViewById(R.id.shortcutGrid);
			grid.setColumnWidth(65);
			final AppGridAdapter<GridObject> adapter = new AppGridAdapter<GridObject>(new GridObject[0], this);
			grid.setAdapter(adapter);

			grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
					if (label.getId() == ALL_LABELS_ID) {
						label = (Label) adapter.getItem(pos);
						reloadGridInThread();
					} else {
						Application a = (Application) grid.getAdapter().getItem(pos);
						Intent i = a.getIntent();
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
		boolean ret = false;
		if (allLabelsSelected) {
			if (label != null && label.getId() != ALL_LABELS_ID) {
				label = ALL_LABELS;
				reloadGridInThread();
				ret = true;
			}
		}
		return ret;
	}

	public void dataSetChanged() {
		reloadGrid();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (label != null && label.getId() != ALL_LABELS_ID) {
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
	// return label != null && label.getId() != ALL_LABELS_ID;
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
		chooseAppsDialogCreator.setCurrentLabel(label);
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
