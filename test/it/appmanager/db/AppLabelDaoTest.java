package it.appmanager.db;


import java.util.List;

import com.google.code.appsorganizer.AppsListActivity;
import com.google.code.appsorganizer.db.AppLabelDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.LabelDao;
import com.google.code.appsorganizer.model.AppLabel;

import android.test.ActivityInstrumentationTestCase2;

public class AppLabelDaoTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public AppLabelDaoTest() {
		super("it.appmanager", AppsListActivity.class);
	}

	private static final String appId1 = "aaaaaa1";
	private static final String appId2 = "aaaaaa2";

	public void testGetLabelsString() throws Exception {
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		LabelDao labelDao = dbHelper.labelDao;
		AppLabelDao appsLabelDao = dbHelper.appsLabelDao;

		Long lab1Id = null;
		Long id1 = null;
		Long id2 = null;
		try {
			lab1Id = labelDao.insert("lab1");
			List<AppLabel> l = appsLabelDao.getApps(lab1Id);
			assertEquals(0, l.size());

			id1 = appsLabelDao.insert(appId1, lab1Id);
			id2 = appsLabelDao.insert(appId2, lab1Id);

			l = appsLabelDao.getApps(lab1Id);
			assertEquals(2, l.size());
			assertEquals(appId1, l.get(0).getApp());
			assertEquals(appId2, l.get(1).getApp());
		} finally {
			if (lab1Id != null) {
				labelDao.delete(lab1Id);
			}
			if (id1 != null) {
				appsLabelDao.delete(id1);
			}
			if (id2 != null) {
				appsLabelDao.delete(id2);
			}
			List<AppLabel> l = appsLabelDao.getApps(lab1Id);
			assertEquals(0, l.size());
		}
	}

	// public void testSalva() {
	// DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
	// LabelDao labelDao = dbHelper.labelDao;
	// AppLabelDao appsLabelDao = dbHelper.appsLabelDao;
	// appsLabelDao.saveLabels(appId, modifiedLabels)
	// }
}
