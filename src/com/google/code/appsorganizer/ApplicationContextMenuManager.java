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
import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.MenuItem;

/**
 * @author fabio
 * 
 */
public class ApplicationContextMenuManager {

	private static final ApplicationContextMenuManager singleton = new ApplicationContextMenuManager();

	public static ApplicationContextMenuManager singleton() {
		return singleton;
	}

	private ApplicationContextMenuManager() {
	}

	public void createMenu(ContextMenu menu, Application app) {
		menu.setHeaderTitle(app.getLabel());
		menu.add(0, 0, 0, R.string.choose_labels_header);
		menu.add(0, 1, 1, R.string.launch);
		menu.add(0, 2, 2, R.string.uninstall);
	}

	public void onContextItemSelected(MenuItem item, Application app, Activity activity, ChooseLabelDialogCreator chooseLabelDialog) {
		switch (item.getItemId()) {
		case 0:
			chooseLabelDialog.setCurrentApp(app);
			activity.showDialog(chooseLabelDialog.getDialogId());
			break;
		case 1:
			Intent intent = app.getIntent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivity(intent);
			break;
		case 2:
			Uri packageURI = Uri.parse("package:" + app.getPackage());
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			uninstallIntent.putExtra("package", app.getPackage());
			activity.startActivityForResult(uninstallIntent, 1);
			break;
		}
	}

}
