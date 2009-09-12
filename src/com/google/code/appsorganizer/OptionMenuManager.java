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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbImportExport;
import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.TextEntryDialog;
import com.google.code.appsorganizer.preferences.PreferencesFromXml;

/**
 * @author fabio
 * 
 */
public class OptionMenuManager {

	private final Activity context;

	private final TextEntryDialog textEntryDialog;

	private final AboutDialogCreator aboutDialogCreator;

	public OptionMenuManager(final Activity context, final DatabaseHelper dbHelper) {
		this.context = context;
		textEntryDialog = new TextEntryDialog(context.getString(R.string.export_menu), context.getString(R.string.file_name),
				new OnOkClickListener() {
					public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
						String fileName = FileImporter.EXPORT_DIR + charSequence;
						if (!fileName.endsWith("." + FileImporter.FILE_EXTENSION)) {
							fileName += "." + FileImporter.FILE_EXTENSION;
						}
						try {
							DbImportExport.export(dbHelper, fileName);
						} catch (Throwable e) {
							((GenericDialogManagerActivity) context).getGenericDialogManager().showSimpleDialog(
									context.getString(R.string.export_error) + ": " + e.getMessage(), false, null);
						}
					}
				});
		aboutDialogCreator = new AboutDialogCreator();
		((GenericDialogManagerActivity) context).getGenericDialogManager().addDialog(textEntryDialog);
		((GenericDialogManagerActivity) context).getGenericDialogManager().addDialog(aboutDialogCreator);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		// mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = context.getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);

		menu.getItem(0).setIcon(R.drawable.fileimport);
		menu.getItem(1).setIcon(R.drawable.fileexport);
		menu.getItem(2).setIcon(R.drawable.reload);

		menu.getItem(3).setIcon(R.drawable.advancedsettings);
		menu.getItem(4).setIcon(R.drawable.info);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reload_apps:
			new AppsReloader(context, true).reload();
			return true;
		case R.id.export_menu:
			context.showDialog(textEntryDialog.getDialogId());
			return true;
		case R.id.import_menu:
			context.startActivity(new Intent(context, FileImporter.class));
			return true;
		case R.id.preferneces:
			context.startActivity(new Intent(context, PreferencesFromXml.class));
			return true;
		case R.id.about:
			context.showDialog(aboutDialogCreator.getDialogId());
			return true;
		}
		return false;
	}
}
