package it.appmanager.model;


import java.util.ArrayList;
import java.util.List;

import com.google.code.appsorganizer.AppLabelBinding;
import com.google.code.appsorganizer.AppsListActivity;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.AppLabelSaver;

import android.test.ActivityInstrumentationTestCase2;

public class AppLabelSaverTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public AppLabelSaverTest() {
		super("it.appmanager", AppsListActivity.class);
	}

	private static final String appId = "aaaaaa";

	public void testGetLabelsString() throws Exception {
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

		AppLabelSaver appLabelSaver = new AppLabelSaver(dbHelper);

		List<AppLabelBinding> modifiedLabels = new ArrayList<AppLabelBinding>();
		modifiedLabels.add(new AppLabelBinding("lab1", true));
		modifiedLabels.add(new AppLabelBinding("lab2", true));
		try {
			appLabelSaver.save(appId, modifiedLabels);
		} finally {
			modifiedLabels.get(0).setChecked(false);
			modifiedLabels.get(1).setChecked(false);
			appLabelSaver.save(appId, modifiedLabels);
			dbHelper.labelDao.delete(modifiedLabels.get(0).getLabelId());
			dbHelper.labelDao.delete(modifiedLabels.get(1).getLabelId());
		}
	}

}
