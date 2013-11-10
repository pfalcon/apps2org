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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.google.code.appsorganizer.appwidget.AppsOrganizerAppWidgetProvider;
import com.google.code.appsorganizer.chooseicon.SelectAppDialog;
import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.dialogs.ExpandableListActivityWithDialog;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.SimpleDialog;
import com.google.code.appsorganizer.dialogs.TextEntryDialog;
import com.google.code.appsorganizer.model.Label;
import com.google.code.appsorganizer.shortcut.ShortcutCreator;

public class LabelListActivity extends ExpandableListActivityWithDialog implements GenericDialogManagerActivity {
	private static final int MENU_ITEM_SELECT_APPS = 2;

	private static final int MENU_ITEM_CHANGE_ICON = 1;

	private static final int MENU_ITEM_RENAME = 0;

	private static final int MENU_ITEM_DELETE = 3;

	private static final int MENU_ITEM_ADD_TO_HOME = 4;

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private ChooseAppsDialogCreator chooseAppsDialogCreator;

	private RenameLabelDialog textEntryDialog;

	private ToggleButton labelButton;

	private ToggleButton appButton;

	private OptionMenuManager optionMenuManager;

	private ConfirmDeleteDialog confirmDeleteDialog;

	private SimpleDialog labelAlreadExistsDialog;

	private SelectAppDialog selectAppDialog;

	private ApplicationViewBinder applicationViewBinder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugReportActivity.registerExceptionHandler(this);
		setContentView(R.layout.main_labels);
		dbHelper = DatabaseHelper.initOrSingleton(this);
		OnOkClickListener onOkClickListener = new OnOkClickListener() {
			private static final long serialVersionUID = 1L;

			public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
				requeryCursor();
			}
		};
		GenericDialogManager dialogManager = getGenericDialogManager();
		chooseLabelDialog = new ChooseLabelDialogCreator(dialogManager, onOkClickListener);

		chooseAppsDialogCreator = new ChooseAppsDialogCreator(dialogManager, onOkClickListener);
		textEntryDialog = new RenameLabelDialog(dialogManager);

		confirmDeleteDialog = new ConfirmDeleteDialog(dialogManager);

		selectAppDialog = new SelectAppDialog(dialogManager, dbHelper);

		labelAlreadExistsDialog = new SimpleDialog(dialogManager, getString(R.string.label_already_exists));
		labelAlreadExistsDialog.setShowNegativeButton(false);
		optionMenuManager = new OptionMenuManager(this, dbHelper, onOkClickListener);

		labelButton = (ToggleButton) findViewById(R.id.labelButton);
		appButton = (ToggleButton) findViewById(R.id.appButton);
		appButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(LabelListActivity.this, SplashScreenActivity.class));
			}
		});

		applicationViewBinder = new ApplicationViewBinder(dbHelper, this, chooseLabelDialog);
		ExpandableListView expandableListView = getExpandableListView();
		registerForContextMenu(expandableListView);
		expandableListView.setClickable(true);
		expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Cursor cursor = getExpandableListAdapter().getChild(groupPosition, childPosition);
				String packageName = cursor.getString(ApplicationViewBinder.PACKAGE);
				String name = cursor.getString(ApplicationViewBinder.NAME);
				applicationViewBinder.onItemClick(packageName, name);
				return true;
			}
		});
	}

	private SimpleCursorTreeAdapter createAdapter() {
		Cursor c = dbHelper.labelDao.getLabelCursor();
		MatrixCursor otherAppsCursor = new MatrixCursor(LabelDao.COLS_STRING, 1);
		otherAppsCursor.addRow(new Object[] { AppCacheDao.OTHER_LABEL_ID, getText(R.string.other_label).toString(), 0, null });
		MergeCursor m = new MergeCursor(new Cursor[] { c, otherAppsCursor });
		startManagingCursor(m);

		SimpleCursorTreeAdapter mAdapter = new SimpleCursorTreeAdapter(this, m, R.layout.label_row_with_icon, new String[] { LabelDao.LABEL_COL_NAME,
				LabelDao.ICON_COL_NAME }, new int[] {}, R.layout.app_row, ApplicationViewBinder.COLS, ApplicationViewBinder.VIEWS) {

			@Override
			protected Cursor getChildrenCursor(Cursor groupCursor) {
				long labelId = groupCursor.getLong(0);
				return dbHelper.appCacheDao.getAppsCursor(labelId);
			}

			@Override
			protected void bindGroupView(View cv, Context context, Cursor cursor, boolean isExpanded) {
				TextView v = (TextView) cv.findViewById(R.id.name);
				ImageView image = (ImageView) cv.findViewById(R.id.image);

				v.setText(cursor.getString(1));
				if (!cursor.isNull(3)) {
					byte[] imageBytes = cursor.getBlob(3);
					image.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
				} else {
					int icon = cursor.getInt(2);
					if (icon != 0) {
						image.setImageResource(Label.convertToIcon(icon));
					} else {
						image.setImageResource(R.drawable.icon_default);
					}
				}
			}

			@Override
			protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
				applicationViewBinder.bindView(view, cursor);
			}
		};
		return mAdapter;
	}

	@Override
	public SimpleCursorTreeAdapter getExpandableListAdapter() {
		return (SimpleCursorTreeAdapter) super.getExpandableListAdapter();
	}

	private class RenameLabelDialog extends TextEntryDialog {

		private static final String RENAME_LABEL_ID = "Rename_label_id";
		long labelId;

		public RenameLabelDialog(GenericDialogManager dialogManager) {
			super(dialogManager, getString(R.string.rename_label), getString(R.string.label_name));

			setOnOkListener(new OnOkClickListener() {
				private static final long serialVersionUID = 1L;

				public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
					String labelName = charSequence.toString();
					if (dbHelper.labelDao.labelAlreadyExists(labelName)) {
						labelAlreadExistsDialog.showDialog();
					} else {
						dbHelper.labelDao.updateName(labelId, labelName);
						AppsOrganizerAppWidgetProvider.updateAppWidget(LabelListActivity.this, dbHelper.labelDao.queryById(labelId));
						requeryCursor();
					}
				}
			});
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putLong(RENAME_LABEL_ID, labelId);
		}

		@Override
		public void onRestoreInstanceState(Bundle state) {
			super.onRestoreInstanceState(state);
			labelId = state.getLong(RENAME_LABEL_ID);
		}
	}

	private final class ConfirmDeleteDialog extends SimpleDialog {

		private static final long serialVersionUID = 1L;

		long labelId;

		public ConfirmDeleteDialog(GenericDialogManager dialogManager) {
			super(dialogManager);
			this.onOkListener = new OnOkClickListener() {
				private static final long serialVersionUID = 1L;

				public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
					DatabaseHelper dbHelper = DatabaseHelper.singleton();
					dbHelper.appsLabelDao.deleteAppsOfLabel(labelId);
					dbHelper.labelDao.delete(labelId);
					requeryCursor();
				}
			};
		}

		@Override
		public void onRestoreInstanceState(Bundle state) {
			super.onRestoreInstanceState(state);
			labelId = state.getLong("ConfirmDeleteDialog_labelId");
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putLong("ConfirmDeleteDialog_labelId", labelId);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!labelButton.isChecked()) {
			labelButton.setChecked(true);
		}
		if (appButton.isChecked()) {
			appButton.setChecked(false);
		}
		setListAdapter(createAdapter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Cursor c = getExpandableListAdapter().getChild(groupPos, childPos);
			ApplicationContextMenuManager.createMenu(this, menu, c.getString(1), -1);
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			Cursor c = getExpandableListAdapter().getGroup(groupPos);
			menu.setHeaderTitle(c.getString(1));
			if (!c.isNull(3)) {
				byte[] imageBytes = c.getBlob(3);
				Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
				menu.setHeaderIcon(new BitmapDrawable(bitmap));
			} else {
				int icon = c.getInt(2);
				if (icon != 0) {
					menu.setHeaderIcon(Label.convertToIcon(icon));
				} else {
					menu.setHeaderIcon(R.drawable.icon_default);
				}
			}
			MenuItem renameItem = menu.add(0, MENU_ITEM_RENAME, 0, R.string.rename);
			MenuItem deleteItem = menu.add(0, MENU_ITEM_DELETE, 1, R.string.delete);
			MenuItem changeIconItem = menu.add(0, MENU_ITEM_CHANGE_ICON, 2, R.string.change_icon);
			MenuItem chooseAppsItem = menu.add(0, MENU_ITEM_SELECT_APPS, 3, R.string.select_apps);
			menu.add(0, MENU_ITEM_ADD_TO_HOME, 4, R.string.add_to_home);
			if (c.getLong(0) == AppCacheDao.OTHER_LABEL_ID) {
				deleteItem.setEnabled(false);
				renameItem.setEnabled(false);
				changeIconItem.setEnabled(false);
				chooseAppsItem.setEnabled(false);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);

		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Cursor c = getExpandableListAdapter().getChild(groupPos, childPos);
			ApplicationContextMenuManager.onContextItemSelected(item, c.getString(ApplicationViewBinder.PACKAGE), c
					.getString(ApplicationViewBinder.NAME), this, chooseLabelDialog);
			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			final Cursor c = getExpandableListAdapter().getGroup(groupPos);
			String labelName = c.getString(1);
			final long labelId = c.getLong(0);
			switch (item.getItemId()) {
			case MENU_ITEM_RENAME:
				textEntryDialog.setDefaultValue(labelName);
				textEntryDialog.labelId = labelId;
				showDialog(textEntryDialog);
				break;
			case MENU_ITEM_DELETE:
				confirmDeleteDialog.setTitle(getString(R.string.delete_confirm, labelName));
				confirmDeleteDialog.labelId = labelId;
				getGenericDialogManager().showDialog(confirmDeleteDialog);
				break;
			case MENU_ITEM_CHANGE_ICON:
				showChooseIconActivity(groupPos);
				return true;
			case MENU_ITEM_SELECT_APPS:
				chooseAppsDialogCreator.setCurrentLabelId(labelId);
				showDialog(chooseAppsDialogCreator);
				break;
			case MENU_ITEM_ADD_TO_HOME:
				int icon = c.getInt(2);
				Intent result = ShortcutCreator.createIntent(this, labelId, labelName, c.isNull(3) ? null : c.getBlob(3), icon > 0 ? Label
						.convertToIcon(icon) : R.drawable.icon_default);

				result.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
				sendBroadcast(result);
				break;
			}
		}

		return false;
	}

	private void showChooseIconActivity(int groupPos) {
		Cursor cursor = getExpandableListAdapter().getGroup(groupPos);
		selectAppDialog.showDialog(cursor.getLong(0));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (ApplicationContextMenuManager.onActivityResult(this, requestCode, resultCode, data)) {
			requeryCursor();
		}
		if (selectAppDialog.onActivityResult(requestCode, resultCode, data)) {
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
		SimpleCursorTreeAdapter expandableListAdapter = getExpandableListAdapter();
		if (expandableListAdapter != null) {
			Cursor cursor = expandableListAdapter.getCursor();
			if (cursor != null) {
				cursor.requery();
			}
		}
	}
}
