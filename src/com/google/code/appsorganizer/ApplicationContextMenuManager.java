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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.model.AppLabelSaver;
import com.google.code.appsorganizer.model.Application;

/**
 * @author fabio
 * 
 */
public class ApplicationContextMenuManager {

	private static final int NO_IGNORE = 4;
	private static final int IGNORE = 3;
	private static final int UNINSTALL = 2;
	private static final int LAUNCH = 1;
	private static final int CHOOSE_LABELS = 0;
	private static final ApplicationContextMenuManager singleton = new ApplicationContextMenuManager();

	public static ApplicationContextMenuManager singleton() {
		return singleton;
	}

	private ApplicationContextMenuManager() {
	}

	public void createMenu(ContextMenu menu, Application app) {
		menu.setHeaderTitle(app.getLabel());
		menu.add(0, CHOOSE_LABELS, 0, R.string.choose_labels_header);
		menu.add(0, LAUNCH, 1, R.string.launch);
		menu.add(0, UNINSTALL, 2, R.string.uninstall);
		if (app.isIgnored()) {
			menu.add(0, NO_IGNORE, 4, R.string.dont_ignore);
		} else {
			menu.add(0, IGNORE, 3, R.string.ignore);
		}
	}

	public void onContextItemSelected(MenuItem item, final Application app, final Activity activity,
			ChooseLabelDialogCreator chooseLabelDialog, final ApplicationInfoManager applicationInfoManager) {
		switch (item.getItemId()) {
		case CHOOSE_LABELS:
			chooseLabelDialog.setCurrentApp(app);
			activity.showDialog(chooseLabelDialog.getDialogId());
			break;
		case LAUNCH:
			Intent intent = app.getIntent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivity(intent);
			break;
		case UNINSTALL:
			Uri packageURI = Uri.parse("package:" + app.getPackage());
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			uninstallIntent.putExtra("package", app.getPackage());
			activity.startActivityForResult(uninstallIntent, 1);
			break;
		case IGNORE:
			((GenericDialogManagerActivity) activity).getGenericDialogManager().showSimpleDialog(
					activity.getString(R.string.ignore_warning_title), activity.getString(R.string.ignore_warning), true,
					new OnOkClickListener() {
						public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
							AppLabelSaver.saveIgnored(DatabaseHelper.singleton(), applicationInfoManager, app, true, activity);
						}
					});
			break;
		case NO_IGNORE:
			AppLabelSaver.saveIgnored(DatabaseHelper.singleton(), applicationInfoManager, app, false, activity);
			break;
		}
	}

	public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			new AppsReloader(activity, false).reload();
		}
	}

}
