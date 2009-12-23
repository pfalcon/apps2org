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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.google.code.appsorganizer.ApplicationContextMenuManager;
import com.google.code.appsorganizer.BugReportActivity;
import com.google.code.appsorganizer.ChooseAppsDialogCreator;
import com.google.code.appsorganizer.ChooseLabelDialogCreator;
import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DatabaseHelperBasic;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.dialogs.ActivityWithDialog;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.model.Label;

public class LabelShortcut extends ActivityWithDialog {

	private static final String ONLY_STARRED_PREF = "onlyStarred";
	public static final long ALL_LABELS_ID = -2l;
	public static final long ALL_STARRED_ID = -3l;
	public static final long OTHER_APPS = -4l;
	public static final String LABEL_ID = "com.example.android.apis.app.LauncherShortcuts";

	private DatabaseHelperBasic dbHelper;

	private long labelId;

	private boolean allLabelsSelected;

	private GridView grid;

	private CheckBox starCheck;

	private TextView titleView;

	private ChooseAppsDialogCreator chooseAppsDialogCreator;

	private ChooseLabelDialogCreator chooseLabelDialog;

	public static boolean firstTime = true;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		OnOkClickListener onOkClickListener = new OnOkClickListener() {
			private static final long serialVersionUID = 1L;

			public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
				requeryCursor();
			}
		};
		chooseAppsDialogCreator = new ChooseAppsDialogCreator(getGenericDialogManager(), onOkClickListener);
		chooseLabelDialog = new ChooseLabelDialogCreator(getGenericDialogManager(), onOkClickListener);
		// Debug.startMethodTracing("grid1");
		BugReportActivity.registerExceptionHandler(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getOrCreateGrid();

		final Intent intent = getIntent();

		labelId = intent.getLongExtra(LABEL_ID, 2);// ALL_STARRED_ID);
		allLabelsSelected = labelId == ALL_LABELS_ID;
	}

	@Override
	protected void onResume() {
		super.onResume();
		new LoadIconTask().execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeCurrentCursor();
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
	}

	private void closeCurrentCursor() {
		if (cursorAdapter != null) {
			Cursor cursor = cursorAdapter.getCursor();
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	private Cursor reloadGrid() {
		if (labelId == ALL_LABELS_ID) {
			Cursor cursor = getDbHelper().getDb().query(LabelDao.TABLE_NAME,
					new String[] { LabelDao.ID_COL_NAME, LabelDao.LABEL_COL_NAME, LabelDao.ICON_COL_NAME, LabelDao.IMAGE_COL_NAME }, null, null,
					null, null, ("upper(" + LabelDao.LABEL_COL_NAME + ")"));
			return cursor;
		} else {
			Cursor tmpCursor;
			if (labelId == ALL_STARRED_ID) {
				tmpCursor = getDbHelper().getDb().rawQuery(
						"select _id, label, image, package, name from apps where starred = 1 and disabled = 0 order by upper(label)", null);
			} else if (labelId == OTHER_APPS) {
				tmpCursor = getDbHelper().getDb().rawQuery(
						"select a._id, a.label, a.image, a.package, a.name from apps a where a.disabled = 0 and not exists("
								+ "select 1 from apps_labels al where a.name = al.app and a.package = al.package) order by upper(a.label)", null);
			} else {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				boolean starredFirst = prefs.getBoolean("starred_first", true);
				boolean onlyStarred = prefs.getBoolean(ONLY_STARRED_PREF, false);
				tmpCursor = AppCacheDao.getAppsOfLabelCursor(getDbHelper().getDb(), labelId, starredFirst, onlyStarred);
			}
			return tmpCursor;
		}
	}

	private String retrieveTitle() {
		String title = "";
		if (labelId == ALL_LABELS_ID) {
			title = getString(R.string.all_labels);
		} else if (labelId == ALL_STARRED_ID) {
			title = getString(R.string.Starred_apps);
		} else if (labelId == OTHER_APPS) {
			title = getString(R.string.other_label);
		} else {
			Cursor c = getDbHelper().getDb().query(LabelDao.TABLE_NAME, new String[] { LabelDao.LABEL_COL_NAME }, LabelDao.ID_COL_NAME + "=?",
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

	public DatabaseHelperBasic getDbHelper() {
		if (dbHelper == null) {
			dbHelper = new DatabaseHelperBasic(LabelShortcut.this);
		}
		return dbHelper;
	}

	private class LoadIconTask extends AsyncTask<String, Object, Object> {

		@Override
		protected Object doInBackground(String... ss) {
			publishProgress(retrieveTitle());

			Cursor prevCursor = null;
			if (cursorAdapter != null) {
				prevCursor = cursorAdapter.getCursor();
			}
			Cursor actual = reloadGrid();
			publishProgress(prevCursor, actual);
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... progress) {
			if (progress.length == 0) {
				cursorAdapter.notifyDataSetChanged();
			} else if (progress.length == 1) {
				updateTitleView((String) progress[0]);
				// Debug.stopMethodTracing();
			} else {
				Cursor prevCursor = (Cursor) progress[0];
				Cursor actualCursor = (Cursor) progress[1];
				if (cursorAdapter == null) {
					createAdapter(actualCursor);
				} else {
					cursorAdapter.changeCursor(actualCursor);
				}
				if (prevCursor != null && !prevCursor.isClosed()) {
					prevCursor.close();
				}
				if (!firstTime) {
					setVisible(true);
				} else {
					firstTime = false;
				}
			}
		}
	}

	private void createAdapter(Cursor cursor) {
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.app_cell_with_icon, cursor, new String[] { LabelDao.ID_COL_NAME,
				LabelDao.LABEL_COL_NAME }, new int[] { R.id.image, R.id.name });
		cursorAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(final View view, Cursor cursor, int columnIndex) {
				if (view instanceof ImageView) {
					ImageView icon = (ImageView) view;
					if (cursor.getColumnCount() == 4) {
						if (!cursor.isNull(3)) {
							byte[] imageBytes = cursor.getBlob(3);
							icon.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
						} else {
							if (!cursor.isNull(2)) {
								int ic = Label.convertToIcon(cursor.getInt(2));
								icon.setImageResource(ic);
							} else {
								icon.setImageResource(R.drawable.icon_default);
							}
						}
					} else {
						byte[] imageBytes = cursor.getBlob(2);
						if (imageBytes != null) {
							Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
							if (bitmap != null) {
								icon.setImageBitmap(bitmap);
							} else {
								icon.setImageResource(R.drawable.icon_default);
							}
						} else {
							icon.setImageResource(R.drawable.icon_default);
						}
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
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LabelShortcut.this);
						if (prefs.getBoolean("close_folder_after_launch", false)) {
							finish();
						}
					}
				}
			});
			grid.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
					SQLiteCursor c = (SQLiteCursor) grid.getAdapter().getItem(info.position);
					if (c.getColumnCount() != 4) {
						ApplicationContextMenuManager.createMenu(menu, c.getString(1));
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
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if (grid != null) {
			SQLiteCursor c = (SQLiteCursor) grid.getAdapter().getItem(info.position);
			ApplicationContextMenuManager.onContextItemSelected(item, c.getString(3), c.getString(4), this, chooseLabelDialog);
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (ApplicationContextMenuManager.onActivityResult(this, requestCode, resultCode, data)) {
			requeryCursor();
		}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (labelId != ALL_LABELS_ID && labelId != ALL_STARRED_ID && labelId != OTHER_APPS) {
			showChooseAppsDialog();
		}
		return false;
	}

	private void showChooseAppsDialog() {
		chooseAppsDialogCreator.setCurrentLabelId(labelId);
		showDialog(chooseAppsDialogCreator);
	}

	private void updateTitleView(String title) {
		if (titleView == null) {
			LayoutInflater factory = LayoutInflater.from(LabelShortcut.this);
			RelativeLayout titleLayout = (RelativeLayout) factory.inflate(R.layout.shortcut_grid_title, null);
			titleLayout.setBackgroundColor(Color.GRAY);
			titleView = (TextView) titleLayout.findViewById(R.id.title);
			starCheck = (CheckBox) titleLayout.findViewById(R.id.starCheck);
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LabelShortcut.this);
			starCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Editor edit = prefs.edit();
					edit.putBoolean(ONLY_STARRED_PREF, isChecked);
					edit.commit();
					new LoadIconTask().execute();
				}
			});
			starCheck.setChecked(prefs.getBoolean(ONLY_STARRED_PREF, false));
			LinearLayout layout = (LinearLayout) findViewById(R.id.shortcutLayout);

			boolean showCloseButton = prefs.getBoolean("show_close_button_in_folder", true);
			View closeButton = titleLayout.findViewById(R.id.closeButton);
			closeButton.setVisibility(showCloseButton ? View.VISIBLE : View.INVISIBLE);
			if (showCloseButton) {
				closeButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});
			}
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			layout.addView(titleLayout, 0, new LayoutParams(LayoutParams.FILL_PARENT, (int) (34 * dm.density)));
		}
		titleView.setText(title);
		int v = labelId > 0 ? View.VISIBLE : View.INVISIBLE;
		if (starCheck.getVisibility() != v) {
			starCheck.setVisibility(v);
		}
	}

	private void requeryCursor() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) grid.getAdapter();
		if (adapter != null) {
			Cursor c = adapter.getCursor();
			if (c != null) {
				c.requery();
			}
		}
	}
}
