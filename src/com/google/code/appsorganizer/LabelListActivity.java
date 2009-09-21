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

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ToggleButton;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.google.code.appsorganizer.chooseicon.ChooseIconActivity;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.ExpandableListActivityWithDialog;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.SimpleDialog;
import com.google.code.appsorganizer.dialogs.TextEntryDialog;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.Label;

public class LabelListActivity extends ExpandableListActivityWithDialog implements DbChangeListener, GenericDialogManagerActivity {
	private static final int MENU_ITEM_SELECT_APPS = 2;

	private static final int MENU_ITEM_CHANGE_ICON = 1;

	private static final int MENU_ITEM_RENAME = 0;

	private static final int MENU_ITEM_DELETE = 3;

	private LabelListAdapter mAdapter;

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private ChooseAppsDialogCreator chooseAppsDialogCreator;

	private TextEntryDialog textEntryDialog;

	private ApplicationInfoManager applicationInfoManager;

	private ToggleButton labelButton;

	private ToggleButton appButton;

	private OptionMenuManager optionMenuManager;

	private ConfirmDeleteDialog confirmDeleteDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugReportActivity.registerExceptionHandler(this);
		setContentView(R.layout.main_labels);
		dbHelper = DatabaseHelper.initOrSingleton(this);
		applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());
		chooseLabelDialog = new ChooseLabelDialogCreator(getGenericDialogManager(), dbHelper, applicationInfoManager);

		mAdapter = new LabelListAdapter(this, dbHelper, applicationInfoManager, chooseLabelDialog);

		ApplicationInfoManager.addListener(this);

		setListAdapter(mAdapter);

		chooseAppsDialogCreator = new ChooseAppsDialogCreator(getGenericDialogManager());
		textEntryDialog = new TextEntryDialog(getGenericDialogManager(), getString(R.string.rename_label), getString(R.string.label_name));

		confirmDeleteDialog = new ConfirmDeleteDialog(getGenericDialogManager());

		optionMenuManager = new OptionMenuManager(this, dbHelper);

		labelButton = (ToggleButton) findViewById(R.id.labelButton);
		appButton = (ToggleButton) findViewById(R.id.appButton);
		appButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(LabelListActivity.this, SplashScreenActivity.class));
			}
		});

		getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Application app = mAdapter.getChild(groupPosition, childPosition);
				chooseLabelDialog.setCurrentApp(app);
				showDialog(chooseLabelDialog);
				return false;
			}
		});

		registerForContextMenu(getExpandableListView());
	}

	private static final class ConfirmDeleteDialog extends SimpleDialog {

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
					ApplicationInfoManager.singleton(null).reloadAppsLabel(dbHelper.labelDao);
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
	}

	public void dataSetChanged(Object source, short type) {
		if (type != CHANGED_STARRED) {
			mAdapter.reloadAndNotify();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ApplicationInfoManager.removeListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Application app = mAdapter.getChild(groupPos, childPos);
			ApplicationContextMenuManager.singleton().createMenu(menu, app);
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			Label label = mAdapter.getGroup(groupPos);
			menu.setHeaderTitle(label.getName());
			byte[] imageBytes = label.getImageBytes();
			if (imageBytes != null) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
				menu.setHeaderIcon(new BitmapDrawable(bitmap));
			} else {
				Integer icon = label.getIcon();
				if (icon != null) {
					menu.setHeaderIcon(icon);
				} else {
					menu.setHeaderIcon(null);
				}
			}
			MenuItem renameItem = menu.add(0, MENU_ITEM_RENAME, 0, R.string.rename);
			MenuItem deleteItem = menu.add(0, MENU_ITEM_DELETE, 1, R.string.delete);
			MenuItem changeIconItem = menu.add(0, MENU_ITEM_CHANGE_ICON, 2, R.string.change_icon);
			MenuItem chooseAppsItem = menu.add(0, MENU_ITEM_SELECT_APPS, 3, R.string.select_apps);
			if (label.getId() == -1l) {
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
			Application app = mAdapter.getChild(groupPos, childPos);
			ApplicationContextMenuManager.singleton().onContextItemSelected(item, app, this, chooseLabelDialog, applicationInfoManager);
			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			final Label label = mAdapter.getGroup(groupPos);
			switch (item.getItemId()) {
			case MENU_ITEM_RENAME:
				textEntryDialog.setDefaultValue(label.getName());
				textEntryDialog.setOnOkListener(new OnOkClickListener() {
					public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
						label.setName(charSequence.toString());
						dbHelper.labelDao.update(label);
						applicationInfoManager.reloadAppsLabel(dbHelper.labelDao);
					}
				});
				showDialog(textEntryDialog);
				break;
			case MENU_ITEM_DELETE:
				confirmDeleteDialog.setTitle(getString(R.string.delete_confirm, label.getName()));
				confirmDeleteDialog.labelId = label.getId();
				getGenericDialogManager().showDialog(confirmDeleteDialog);
				break;
			case MENU_ITEM_CHANGE_ICON:
				showChooseIconActivity(groupPos);
				return true;
			case MENU_ITEM_SELECT_APPS:
				chooseAppsDialogCreator.setCurrentLabelId(label.getId());
				showDialog(chooseAppsDialogCreator);
				break;
			}
		}

		return false;
	}

	private void showChooseIconActivity(int groupPos) {
		Intent intent = new Intent(this, ChooseIconActivity.class);
		intent.putExtra("group", groupPos);
		startActivityForResult(intent, 2);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ApplicationContextMenuManager.singleton().onActivityResult(this, requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 2) {
			byte[] image = data.getByteArrayExtra("image");
			Label label = mAdapter.getGroup(data.getIntExtra("group", -1));
			if (image != null) {
				label.setImageBytes(image);
			} else {
				int icon = data.getIntExtra("icon", -1);
				label.setIcon(icon);
				label.setImageBytes(null);
			}
			dbHelper.labelDao.update(label);
			mAdapter.notifyDataSetChanged();
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
}
