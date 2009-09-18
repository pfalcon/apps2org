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
import android.view.ContextMenu;
import android.view.MenuItem;

import com.google.code.appsorganizer.model.Application;

/**
 * @author fabio
 * 
 */
public class ApplicationContextMenuManager {

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
	}

	public void onContextItemSelected(MenuItem item, final Application app, final Activity activity,
			ChooseLabelDialogCreator chooseLabelDialog, final ApplicationInfoManager applicationInfoManager) {
		switch (item.getItemId()) {
		case CHOOSE_LABELS:
			chooseLabelDialog.setCurrentApp(app);
			activity.showDialog(chooseLabelDialog.getDialogId());
			break;
		case LAUNCH:
			app.startApplication(activity);
			break;
		case UNINSTALL:
			app.uninstallApplication(activity);
			break;
		}
	}

	public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			new AppsReloader(activity, false).reload();
		}
	}

}
