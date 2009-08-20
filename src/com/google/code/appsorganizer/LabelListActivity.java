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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.google.code.appsorganizer.chooseicon.ChooseIconActivity;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.TextEntryDialog;
import com.google.code.appsorganizer.model.AppLabel;
import com.google.code.appsorganizer.model.Label;

public class LabelListActivity extends ExpandableListActivity {
	private MyExpandableListAdapter mAdapter;

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private TextEntryDialog textEntryDialog;

	private ApplicationInfoManager applicationInfoManager;

	private GenericDialogManager genericDialogManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper = DatabaseHelper.singleton();
		applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());

		mAdapter = new MyExpandableListAdapter();

		applicationInfoManager.addListener(new DbChangeListener() {
			public void notifyDataSetChanged() {
				mAdapter.reloadAndNotify();
			}
		});

		setListAdapter(mAdapter);

		genericDialogManager = new GenericDialogManager(this);
		chooseLabelDialog = new ChooseLabelDialogCreator(dbHelper, applicationInfoManager);
		textEntryDialog = new TextEntryDialog(getString(R.string.rename_label), getString(R.string.label_name));
		genericDialogManager.addDialog(chooseLabelDialog);
		genericDialogManager.addDialog(textEntryDialog);

		getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Application app = mAdapter.getChild(groupPosition, childPosition);
				chooseLabelDialog.setCurrentApp(app);
				showDialog(chooseLabelDialog.getDialogId());
				return false;
			}
		});

		registerForContextMenu(getExpandableListView());
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
			MenuItem renameItem = menu.add(0, 0, 0, R.string.rename);
			MenuItem changeIconItem = menu.add(0, 1, 1, R.string.change_icon);
			if (label.getId() == -1l) {
				renameItem.setEnabled(false);
				changeIconItem.setEnabled(false);
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
			ApplicationContextMenuManager.singleton().onContextItemSelected(item, app, this, chooseLabelDialog);
			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			final Label label = mAdapter.getGroup(groupPos);
			if (item.getItemId() == 0) {
				textEntryDialog.setDefaultValue(label.getName());
				textEntryDialog.setOnOkListener(new OnOkClickListener() {
					public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
						label.setName(charSequence.toString());
						dbHelper.labelDao.update(label);
					}
				});
				showDialog(textEntryDialog.getDialogId());
			} else if (item.getItemId() == 1) {
				startActivityForResult(new Intent(this, ChooseIconActivity.class), groupPos);
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			int icon = data.getIntExtra("icon", -1);
			Label label = mAdapter.getGroup(requestCode);
			label.setIcon(icon);
			dbHelper.labelDao.update(label);
			mAdapter.notifyDataSetChanged();
		}
	}

	public class MyExpandableListAdapter extends BaseExpandableListAdapter {
		private List<Label> groups = dbHelper.labelDao.getLabels();

		private Map<Long, List<Application>> apps = new HashMap<Long, List<Application>>();

		public MyExpandableListAdapter() {
			reload();
		}

		public void reloadAndNotify() {
			reload();
			notifyDataSetChanged();
		}

		private void reload() {
			groups = dbHelper.labelDao.getLabels();
			groups.add(createNoLabel());
			apps = new HashMap<Long, List<Application>>();
		}

		private Label createNoLabel() {
			Label label = new Label();
			label.setId(-1l);
			label.setName(getText(R.string.other_label).toString());
			return label;
		}

		private List<Application> getAppsInPos(Integer pos) {
			Label label = groups.get(pos);
			return getApps(label.getId());
		}

		private List<Application> getApps(Long labelId) {
			List<Application> ret = apps.get(labelId);
			if (ret == null) {
				if (labelId == -1) {
					List<String> l = dbHelper.appsLabelDao.getAppsWithLabel();
					ret = new ArrayList<Application>(applicationInfoManager.convertToApplicationListNot(l));
				} else {
					List<AppLabel> l = dbHelper.appsLabelDao.getApps(labelId);
					ret = new ArrayList<Application>(applicationInfoManager.convertToApplicationList(l));
				}
				apps.put(labelId, ret);
			}
			return ret;
		}

		public Application getChild(int groupPosition, int childPosition) {
			return getAppsInPos(groupPosition).get(childPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return getAppsInPos(groupPosition).size();
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View cv, ViewGroup parent) {
			if (cv == null) {
				LayoutInflater factory = LayoutInflater.from(LabelListActivity.this);
				cv = factory.inflate(R.layout.app_row, null);
			}
			TextView v = (TextView) cv.findViewById(R.id.name);
			TextView vl = (TextView) cv.findViewById(R.id.labels);
			ImageView image = (ImageView) cv.findViewById(R.id.image);

			Application application = getChild(groupPosition, childPosition);

			vl.setText(dbHelper.labelDao.getLabelsString(application));
			v.setText(application.getLabel());
			image.setImageDrawable(application.getIcon());
			return cv;
		}

		public Label getGroup(int groupPosition) {
			return groups.get(groupPosition);
		}

		public int getGroupCount() {
			return groups.size();
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View cv, ViewGroup parent) {
			if (cv == null) {
				LayoutInflater factory = LayoutInflater.from(LabelListActivity.this);
				cv = factory.inflate(R.layout.label_row_with_icon, null);
			}
			TextView v = (TextView) cv.findViewById(R.id.name);
			ImageView image = (ImageView) cv.findViewById(R.id.image);

			Label label = getGroup(groupPosition);

			v.setText(label.getName());
			Integer icon = label.getIcon();
			if (icon != null) {
				image.setImageResource(icon);
			} else {
				image.setImageDrawable(null);
			}
			return cv;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public boolean hasStableIds() {
			return true;
		}

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
