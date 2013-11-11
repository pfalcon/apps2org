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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbImportExport;
import com.google.code.appsorganizer.dialogs.FullVersionDialog;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.SimpleDialog;
import com.google.code.appsorganizer.dialogs.TextEntryDialog;
import com.google.code.appsorganizer.download.LabelDownloader;
import com.google.code.appsorganizer.preferences.PreferencesFromXml;

/**
 * @author fabio
 * 
 */
public class OptionMenuManager {

	private final Activity context;

	private final TextEntryDialog textEntryDialog;

	private final AboutDialogCreator aboutDialogCreator;

	private final SimpleDialog exportErrorDialog;

	private final LabelDownloader labelDownloader;

	private final NewLabelDialog newLabelDialog;

	private final OnOkClickListener onOkClickListener;

	public OptionMenuManager(final Activity context, final DatabaseHelper dbHelper, OnOkClickListener onOkClickListener) {
		this.context = context;
		this.onOkClickListener = onOkClickListener;

		labelDownloader = new LabelDownloader(context, dbHelper, onOkClickListener);

		final GenericDialogManager genericDialogManager = ((GenericDialogManagerActivity) context).getGenericDialogManager();
		exportErrorDialog = new SimpleDialog(genericDialogManager, context.getString(R.string.export_error));

		textEntryDialog = new TextEntryDialog(genericDialogManager, context.getString(R.string.export_menu), context.getString(R.string.file_name),
				new OnOkClickListener() {
					private static final long serialVersionUID = 1L;

					public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
						String fileName = FileImporter.EXPORT_DIR + charSequence;
						if (!fileName.endsWith("." + FileImporter.FILE_EXTENSION)) {
							fileName += "." + FileImporter.FILE_EXTENSION;
						}
						try {
							DbImportExport.export(dbHelper, fileName);
						} catch (Throwable e) {
							exportErrorDialog.setTitle(context.getString(R.string.export_error) + ": " + e.getMessage());
							genericDialogManager.showDialog(exportErrorDialog);
						}
					}
				});
		textEntryDialog.setDefaultValue("backup.txt");
		aboutDialogCreator = new AboutDialogCreator(genericDialogManager);
		newLabelDialog = new NewLabelDialog(genericDialogManager, new OnOkClickListener() {

			private static final long serialVersionUID = 1036198138637107577L;

			public void onClick(CharSequence l, DialogInterface dialog, int which) {
				dbHelper.labelDao.insert(l.toString());
				OptionMenuManager.this.onOkClickListener.onClick(l, dialog, which);
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = context.getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.new_label:
			newLabelDialog.showDialog();
			return true;
		case R.id.reload_apps:
			new AppsReloader(context, true).reload();
			onOkClickListener.onClick(null, null, 0);
			return true;
		case R.id.export_menu:
			textEntryDialog.showDialog();
			return true;
			// case R.id.download_labels:
			// labelDownloader.download();
			// return true;
		case R.id.import_menu:
			context.startActivity(new Intent(context, FileImporter.class));
			return true;
		case R.id.preferneces:
			context.startActivity(new Intent(context, PreferencesFromXml.class));
			return true;
		case R.id.about:
			aboutDialogCreator.showDialog();
			return true;
		case R.id.donate:
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://sites.google.com/site/appsorganizer/donate")));
			return true;
		case R.id.other_apps:
			try {
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(FullVersionDialog.FOLDER_ORGANIZER_MARKET_QUERY));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				return true;
			} catch (Throwable t) {
				Toast.makeText(context, "Error launching Android Market", Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}

	public void startDownload() {
		labelDownloader.startDownload(true);
	}
}
