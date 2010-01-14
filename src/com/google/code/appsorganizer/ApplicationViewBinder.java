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

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.google.code.appsorganizer.db.AppCacheDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;

/**
 * @author fabio
 * 
 */
public class ApplicationViewBinder implements ViewBinder {

	public static final String[] COLS = new String[] { AppCacheDao.ID_COL_NAME, AppCacheDao.LABEL_COL_NAME, AppCacheDao.NAME_COL_NAME,
			AppCacheDao.STARRED_COL_NAME, AppCacheDao.IMAGE_COL_NAME, AppCacheDao.PACKAGE_NAME_COL_NAME };

	public static final int[] VIEWS = new int[] { R.id.image, R.id.name, R.id.labels, R.id.starCheck };

	private static final int APP_LABEL = 1;

	private static final int STARRED = 3;

	public static final int NAME = 2;

	public static final int PACKAGE = 5;

	private final DatabaseHelper dbHelper;

	private final Activity context;

	private final ChooseLabelDialogCreator chooseLabelDialog;

	public ApplicationViewBinder(DatabaseHelper dbHelper, Activity context, ChooseLabelDialogCreator chooseLabelDialog) {
		this.dbHelper = dbHelper;
		this.context = context;
		this.chooseLabelDialog = chooseLabelDialog;
	}

	private static OnLongClickListener onLongClickListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			return false;
		}
	};

	public boolean setViewValue(View view, final Cursor cursor, int columnIndex) {
		switch (columnIndex) {
		case 0:
			bindImage((ImageView) view, cursor);
			break;
		case 1:
			bindAppName((TextView) view, cursor);
			break;
		case 2:
			bindLabels((TextView) view, cursor);
			break;
		case 3:
			bindStarred((CheckBox) view, cursor);
			break;
		}
		return true;
	}

	static class ViewHolder {
		ImageView image;
		TextView labels;
		TextView name;
		CheckBox starred;
	}

	public void bindView(View view, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		if (viewHolder == null) {
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView) view.findViewById(R.id.image);
			viewHolder.labels = (TextView) view.findViewById(R.id.labels);
			viewHolder.name = (TextView) view.findViewById(R.id.name);
			viewHolder.starred = (CheckBox) view.findViewById(R.id.starCheck);
			view.setTag(viewHolder);
		}
		bindImage(viewHolder.image, cursor);
		bindAppName(viewHolder.name, cursor);
		bindLabels(viewHolder.labels, cursor);
		bindStarred(viewHolder.starred, cursor);
	}

	private void bindLabels(TextView view, final Cursor cursor) {
		view.setOnLongClickListener(onLongClickListener);
		view.setText(dbHelper.appsLabelDao.getLabelListString(cursor.getString(PACKAGE), cursor.getString(NAME)));
		addOnClickListener(view, cursor);
	}

	private void bindAppName(TextView view, final Cursor cursor) {
		view.setOnLongClickListener(onLongClickListener);
		view.setText(cursor.getString(APP_LABEL));
		addOnClickListener(view, cursor);
	}

	private void bindImage(ImageView view, final Cursor cursor) {
		view.setOnLongClickListener(onLongClickListener);
		byte[] imageBytes = cursor.getBlob(4);
		if (imageBytes != null) {
			view.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
		} else {
			view.setImageResource(R.drawable.icon_default);
		}
		addOnClickListener(view, cursor);
	}

	private void addOnClickListener(View view, final Cursor cursor) {
		final String packageName = cursor.getString(PACKAGE);
		final String name = cursor.getString(NAME);
		view.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onItemClick(packageName, name);
			}
		});
	}

	private void bindStarred(CheckBox checkbox, Cursor cursor) {
		checkbox.setOnLongClickListener(onLongClickListener);
		checkbox.setOnCheckedChangeListener(null);
		checkbox.setChecked(cursor.getInt(STARRED) == 1);
		final String packageName = cursor.getString(PACKAGE);
		final String name = cursor.getString(NAME);
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dbHelper.appCacheDao.updateStarred(packageName, name, isChecked);
			}
		});
	}

	public void onItemClick(final String packageName, final String name) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String defaultAction = prefs.getString("defaultAction", "choose_labels");
		if (defaultAction.equals("choose_labels")) {
			chooseLabelDialog.setCurrentApp(packageName, name);
			((GenericDialogManagerActivity) context).showDialog(chooseLabelDialog);
		} else if (defaultAction.equals("uninstall")) {
			ApplicationContextMenuManager.uninstallApplication(context, packageName);
		} else {
			ApplicationContextMenuManager.startApplication(context, packageName, name);
		}
	}
}
