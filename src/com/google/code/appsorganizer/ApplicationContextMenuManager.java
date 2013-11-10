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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;
import com.google.code.appsorganizer.model.AppCache;
import com.google.code.appsorganizer.db.DatabaseHelper;

/**
 * @author fabio
 * 
 */
public class ApplicationContextMenuManager {

	private static final int UNINSTALL = 2;
	private static final int LAUNCH = 1;
	private static final int CHOOSE_LABELS = 0;
	private static final int SET_STAR = 3;
	private static final int UNSET_STAR = 4;

	public static void createMenu(Context context, ContextMenu menu, String label, int id) {
		menu.setHeaderTitle(label);
		menu.add(0, CHOOSE_LABELS, 0, R.string.choose_labels_header);
		if (id >= 0) {
			AppCache app = DatabaseHelper.initOrSingleton(context).appCacheDao.queryById(Long.valueOf(id));
			menu.add(0, app.starred ? UNSET_STAR : SET_STAR,
			         1, app.starred ? R.string.unstar_app : R.string.star_app);
		}
		menu.add(0, LAUNCH, 1, R.string.launch);
		menu.add(0, UNINSTALL, 2, R.string.uninstall);
	}

	public static void onContextItemSelected(MenuItem item, String packageName, String name, Activity activity,
			ChooseLabelDialogCreator chooseLabelDialog) {
		switch (item.getItemId()) {
		case CHOOSE_LABELS:
			chooseLabelDialog.setCurrentApp(packageName, name);
			((GenericDialogManagerActivity) activity).showDialog(chooseLabelDialog);
			break;
		case SET_STAR:
			DatabaseHelper.initOrSingleton(activity).appCacheDao.updateStarred(packageName, name, true);
			break;
		case UNSET_STAR:
			DatabaseHelper.initOrSingleton(activity).appCacheDao.updateStarred(packageName, name, false);
			break;
		case LAUNCH:
			startApplication(activity, packageName, name);
			break;
		case UNINSTALL:
			uninstallApplication(activity, packageName);
			break;
		}
	}

	public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		return requestCode == 1;
	}

	public static void startApplication(Context activity, String packageName, String name) {
		try {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName(packageName, name);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivity(intent);
		} catch (Throwable t) {
			Toast.makeText(activity, R.string.error_while_launching_activity, Toast.LENGTH_SHORT).show();
		}

	}

	public static void uninstallApplication(Activity activity, String packageName) {
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		uninstallIntent.putExtra("package", packageName);
		activity.startActivityForResult(uninstallIntent, 1);
	}
}
