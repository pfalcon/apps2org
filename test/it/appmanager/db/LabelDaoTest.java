package it.appmanager.db;


import java.util.List;

import com.google.code.appsorganizer.AppLabelBinding;
import com.google.code.appsorganizer.AppsListActivity;
import com.google.code.appsorganizer.db.AppLabelDao;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.LabelDao;

import android.test.ActivityInstrumentationTestCase2;

public class LabelDaoTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public LabelDaoTest() {
		super("it.appmanager", AppsListActivity.class);
	}

	private static final String appId = "aaaaaa";
	private static final String otherAppId = "o_aaaaaa";

	public void testGetLabelsString() throws Exception {
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		LabelDao labelDao = dbHelper.labelDao;
		AppLabelDao appsLabelDao = dbHelper.appsLabelDao;

		String s = labelDao.getLabelsString(appId);
		assertEquals("", s);

		Long lab1Id = null;
		Long lab2Id = null;
		Long lab3Id = null;
		Long id1 = null;
		Long id2 = null;
		Long id3 = null;
		try {
			lab1Id = labelDao.insert("lab1");
			lab2Id = labelDao.insert("lab2");
			lab3Id = labelDao.insert("lab3");

			id1 = appsLabelDao.insert(appId, lab1Id);
			id2 = appsLabelDao.insert(appId, lab2Id);
			id3 = appsLabelDao.insert(otherAppId, lab3Id);
			s = labelDao.getLabelsString(appId);
			assertEquals("lab1, lab2", s);
		} finally {
			deleteAppLabel(appsLabelDao, id1);
			deleteAppLabel(appsLabelDao, id2);
			deleteAppLabel(appsLabelDao, id3);

			deleteLabel(labelDao, lab1Id);
			deleteLabel(labelDao, lab2Id);
			deleteLabel(labelDao, lab3Id);

			s = labelDao.getLabelsString(appId);
			assertEquals("", s);
		}
	}

	// public void testGetAllLabels() throws Exception {
	// DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
	// LabelDao labelDao = dbHelper.labelDao;
	// AppLabelDao appsLabelDao = dbHelper.appsLabelDao;
	//
	// Long lab1Id = null;
	// Long lab2Id = null;
	// Long lab3Id = null;
	// Long id1 = null;
	// Long id2 = null;
	// Long id3 = null;
	// try {
	// lab1Id = labelDao.insert("lab1");
	// lab2Id = labelDao.insert("lab2");
	// lab3Id = labelDao.insert("lab3");
	//
	// id1 = appsLabelDao.insert(appId, lab1Id);
	// id2 = appsLabelDao.insert(appId, lab2Id);
	// id3 = appsLabelDao.insert(otherAppId, lab3Id);
	// List<AppLabelBinding> allLabels = labelDao.getAllLabels(appId);
	// assertEquals(true, getLabel(allLabels, lab1Id).isChecked());
	// assertEquals(true, getLabel(allLabels, lab2Id).isChecked());
	// assertEquals(false, getLabel(allLabels, lab3Id).isChecked());
	// } finally {
	// deleteAppLabel(appsLabelDao, id1);
	// deleteAppLabel(appsLabelDao, id2);
	// deleteAppLabel(appsLabelDao, id3);
	//
	// List<AppLabelBinding> allLabels = labelDao.getAllLabels(appId);
	// assertEquals(false, getLabel(allLabels, lab1Id).isChecked());
	// assertEquals(false, getLabel(allLabels, lab2Id).isChecked());
	// assertEquals(false, getLabel(allLabels, lab3Id).isChecked());
	//
	// deleteLabel(labelDao, lab1Id);
	// deleteLabel(labelDao, lab2Id);
	// deleteLabel(labelDao, lab3Id);
	// }
	// }

	private AppLabelBinding getLabel(List<AppLabelBinding> allLabels, Long o) {
		for (AppLabelBinding l : allLabels) {
			if (l.getLabelId().equals(o)) {
				return l;
			}
		}
		fail("label " + o + " not found");
		throw null;
	}

	private void deleteAppLabel(AppLabelDao appsLabelDao, Long id1) {
		if (id1 != null) {
			appsLabelDao.delete(id1);
		}
	}

	private void deleteLabel(LabelDao labelDao, Long lab1Id) {
		if (lab1Id != null) {
			labelDao.delete(lab1Id);
		}
	}

}
