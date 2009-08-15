package com.google.code.appsorganizer.model;


import java.util.List;

import com.google.code.appsorganizer.AppLabelBinding;
import com.google.code.appsorganizer.db.DatabaseHelper;

public class AppLabelSaver {

	private final DatabaseHelper dbHelper;

	public AppLabelSaver(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public void save(String appId, List<AppLabelBinding> modifiedLabels) {
		if (!modifiedLabels.isEmpty()) {
			for (AppLabelBinding b : modifiedLabels) {
				Long labelId = b.getLabelId();
				if (b.isChecked()) {
					if (b.isChecked() && labelId == null) {
						labelId = dbHelper.labelDao.insert(b.getLabel());
					}
					dbHelper.appsLabelDao.insert(appId, labelId);
				} else {
					dbHelper.appsLabelDao.delete(b.getAppLabelId());
				}
			}
			dbHelper.appsLabelDao.notifyDataSetChanged();
		}
	}
}
