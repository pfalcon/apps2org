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
package com.google.code.appsorganizer.db;

import gnu.trove.TObjectLongHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;

import com.google.code.appsorganizer.FileImporter;
import com.google.code.appsorganizer.maps.AppCacheMap;
import com.google.code.appsorganizer.model.AppCache;
import com.google.code.appsorganizer.model.AppLabel;
import com.google.code.appsorganizer.model.Label;

/**
 * @author fabio
 * 
 */
public class DbImportExport {

	private static final String APP_LABEL_SEPARATOR_LINE = "\t\t\t";
	private static final String LABEL_PREFIX = "\t";
	private static final String ICON_NAME_SEPARATOR = "\t";

	public static void export(DatabaseHelper dbHelper, String charSequence) throws IOException {
		FileImporter.checkDirExists(FileImporter.EXPORT_DIR);
		BufferedWriter bout = new BufferedWriter(new FileWriter(new File(charSequence)));
		export(dbHelper, bout);
	}

	public static void export(DatabaseHelper dbHelper, BufferedWriter bout) throws IOException {
		try {
			writeLabels(dbHelper.labelDao, bout);
			bout.write(APP_LABEL_SEPARATOR_LINE);
			bout.newLine();
			writeApps(dbHelper.labelDao, bout);
			bout.write(APP_LABEL_SEPARATOR_LINE);
			bout.newLine();
			writeStarredApps(dbHelper.appCacheDao, bout);
		} finally {
			bout.close();
		}
	}

	private static void writeStarredApps(AppCacheDao appCacheDao, BufferedWriter bout) throws IOException {
		AppCacheMap cache = appCacheDao.queryForCacheMap(true);
		AppCache[] values = cache.values();
		for (int i = 0; i < values.length; i++) {
			AppCache a = values[i];
			if (a.starred) {
				bout.write(a.packageName + AppCacheMap.SEPARATOR + a.name);
				bout.newLine();
			}
		}
	}

	private static void writeApps(LabelDao labelDao, BufferedWriter bout) throws IOException {
		DoubleArray appsLabels = labelDao.getAppsLabels();
		String[] apps = appsLabels.keys;
		String[] labels = appsLabels.values;

		String curApp = null;
		for (int i = 0; i < apps.length; i++) {
			String app = apps[i];
			if (!app.equals(curApp)) {
				bout.write(app);
				bout.newLine();
				curApp = app;
			}
			bout.write(LABEL_PREFIX + labels[i]);
			bout.newLine();
		}

	}

	private static void writeLabels(LabelDao labelDao, BufferedWriter bout) throws IOException {
		Label[] labels = labelDao.getLabelsArray();
		for (int i = 0; i < labels.length; i++) {
			Label l = labels[i];
			bout.write(l.getIconDb() + ICON_NAME_SEPARATOR + l.getName());
			bout.newLine();
		}
	}

	public static void importData(Activity context, String charSequence) throws IOException {
		DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);

		BufferedReader bout = null;
		dbHelper.beginTransaction();
		try {
			bout = new BufferedReader(new FileReader(charSequence));
			importData(dbHelper, bout);
			dbHelper.setTransactionSuccessful();
		} finally {
			dbHelper.endTransaction();
			if (bout != null) {
				bout.close();
			}
		}
	}

	private static void importData(DatabaseHelper dbHelper, BufferedReader in) throws IOException {
		TObjectLongHashMap<String> labelsId = importLabels(dbHelper.labelDao, in);
		importApps(dbHelper.appsLabelDao, labelsId, in, dbHelper.labelDao);
		importStarred(dbHelper.appCacheDao, in);
		// old files doesn't contain package
		dbHelper.addPackages();
	}

	private static void importStarred(AppCacheDao appCacheDao, BufferedReader in) throws IOException {
		String s = null;
		appCacheDao.clearStarred();
		while ((s = in.readLine()) != null) {
			if (s.length() > 0) {
				int i = s.indexOf(AppCacheMap.SEPARATOR);
				String packageName = s.substring(0, i);
				String name = s.substring(i + 1);
				appCacheDao.updateStarred(packageName, name, true);
			}
		}
	}

	private static void importApps(AppLabelDao appsLabelDao, TObjectLongHashMap<String> labelsId, BufferedReader in, LabelDao labelDao)
			throws IOException {
		String s = null;
		String curApp = null;
		int labelPrefixLength = LABEL_PREFIX.length();
		DoubleArray appsLabels = labelDao.getAppsLabels();
		while ((s = in.readLine()) != null) {
			if (s.equals(APP_LABEL_SEPARATOR_LINE)) {
				break;
			}
			if (s.startsWith(LABEL_PREFIX)) {
				String labelName = s.substring(labelPrefixLength);
				if (!appLabelAlreadyExist(appsLabels, curApp, labelName)) {
					AppLabel appLabel = new AppLabel();
					int indexOf = curApp.indexOf(AppCacheMap.SEPARATOR);
					if (indexOf == -1) {
						appLabel.setApp(curApp);
					} else {
						appLabel.setApp(curApp.substring(indexOf + 1));
						appLabel.setPackageName(curApp.substring(0, indexOf));
					}
					appLabel.setLabelId(labelsId.get(labelName));
					appsLabelDao.insert(appLabel);
				}
			} else {
				curApp = s;
			}
		}
	}

	private static boolean appLabelAlreadyExist(DoubleArray appsLabels, String curApp, String label) {
		String[] apps = appsLabels.keys;
		String[] labels = appsLabels.values;
		for (int i = 0; i < apps.length; i++) {
			String a = apps[i];
			if (a.endsWith(curApp)) {
				for (; i < labels.length; i++) {
					// for backward compatibility curApp can be only app name
					// (with no package)
					if (!a.endsWith(curApp)) {
						return false;
					}
					if (labels[i].equals(label)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static TObjectLongHashMap<String> importLabels(LabelDao labelDao, BufferedReader in) throws IOException {
		TObjectLongHashMap<String> ret = new TObjectLongHashMap<String>();
		Label[] labels = labelDao.getLabelsArray();
		String s = null;
		while ((s = in.readLine()) != null) {
			if (APP_LABEL_SEPARATOR_LINE.equals(s)) {
				break;
			}
			String[] split = s.split(ICON_NAME_SEPARATOR);
			int icon = 0;
			try {
				icon = Integer.parseInt(split[0]);
			} catch (NumberFormatException ignored) {
			}
			String name = split[1];
			long id = insertOrUpdateLabel(labelDao, labels, icon, name);
			ret.put(name, id);
		}
		return ret;
	}

	private static long insertOrUpdateLabel(LabelDao labelDao, Label[] labels, int icon, String name) {
		Label label = searchExistingLabel(labels, name);
		if (label != null) {
			if (label.getIconDb() != icon) {
				label.setIconDb(icon);
				labelDao.update(label);
			}
		} else {
			label = new Label(name, icon);
			labelDao.insert(label);
		}
		return label.getId();
	}

	private static Label searchExistingLabel(Label[] labels, String name) {
		for (int i = 0; i < labels.length; i++) {
			if (labels[i].getName().equals(name)) {
				return labels[i];
			}
		}
		return null;
	}

}
