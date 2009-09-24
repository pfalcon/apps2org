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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.code.appsorganizer.db.DbImportExport;
import com.google.code.appsorganizer.dialogs.ListActivityWithDialog;
import com.google.code.appsorganizer.dialogs.SimpleDialog;

/**
 * @author fabio
 * 
 */
public class FileImporter extends ListActivityWithDialog {

	public static final String FILE_EXTENSION = "txt";

	public static final String EXPORT_DIR = "/sdcard/AppsOrganizer/";

	protected ArrayList<String> mFileList;
	protected File mRoot;

	private SimpleDialog importErrorDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.filelister);

		importErrorDialog = new SimpleDialog(getGenericDialogManager(), getString(R.string.import_error));
		importErrorDialog.setShowNegativeButton(false);

		initialize(getString(R.string.import_menu), EXPORT_DIR);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				String fileName = (String) getListAdapter().getItem(pos);
				try {
					DbImportExport.importData(FileImporter.this, EXPORT_DIR + fileName);
					new AppsReloader(FileImporter.this, false).reload();
					finish();
				} catch (Throwable e) {
					e.printStackTrace();
					importErrorDialog.setTitle(getString(R.string.import_error) + ": " + e.getMessage());
					getGenericDialogManager().showDialog(importErrorDialog);
				}
			}
		});
	}

	public void initialize(String title, String path) {
		setTitle(title);
		mFileList = new ArrayList<String>();
		if (getDirectory(path)) {
			getFiles(mRoot);
			displayFiles();
		}

	}

	public void refreshRoot() {
		getFiles(mRoot);
		displayFiles();
	}

	private boolean getDirectory(String path) {

		TextView tv = (TextView) findViewById(R.id.filelister_message);

		// check to see if there's an sd card.
		String cardstatus = Environment.getExternalStorageState();
		if (cardstatus.equals(Environment.MEDIA_REMOVED) || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED) || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			tv.setText(getString(R.string.sdcard_error));
			return false;
		}

		// if storage directory does not exist, create it.
		mRoot = checkDirExists(path);

		if (mRoot == null) {
			tv.setText(getString(R.string.directory_error, path));
			return false;
		} else {
			return true;
		}
	}

	public static File checkDirExists(String path) {
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				return null;
			}
		}
		return f;
	}

	private void getFiles(File f) {
		if (f.isDirectory()) {
			File[] childs = f.listFiles();
			for (File child : childs) {
				getFiles(child);
			}
		} else {
			String filename = f.getName();
			if (filename.matches(".*\\." + FILE_EXTENSION)) {
				mFileList.add(filename);
			}
		}
	}

	/**
	 * Opens the directory, puts valid files in array adapter for display
	 */
	private void displayFiles() {

		ArrayAdapter<String> fileAdapter;
		Collections.sort(mFileList, String.CASE_INSENSITIVE_ORDER);

		getListView().setItemsCanFocus(false);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, mFileList);

		setListAdapter(fileAdapter);
	}
}
