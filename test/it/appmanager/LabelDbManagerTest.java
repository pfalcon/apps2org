package it.appmanager;

import com.google.code.appsorganizer.AppsListActivity;

import android.test.ActivityInstrumentationTestCase2;

public class LabelDbManagerTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public LabelDbManagerTest() {
		super("it.appmanager", AppsListActivity.class);
	}

	private static final String appId = "aaaaaa";

	// public void testGetAllLabels() throws Exception {
	// LabelDbManager labelDbManager = new LabelDbManager(getActivity());
	// String nomeLabel = "labAAA";
	// labelDbManager.addLabel(appId, nomeLabel);
	//
	// try {
	// List<Label> l = labelDbManager.getAllLabelsList(appId);
	// for (Label label : l) {
	// if (label.getName().equals(nomeLabel)) {
	// assertEquals(true, label.isChecked());
	// assertEquals(true, label.isOriginalChecked());
	// }
	// }
	// } finally {
	// labelDbManager.deleteLabel(appId, nomeLabel);
	// }
	// }
	//
	// public void testGetLabelsString() throws Exception {
	// LabelDbManager labelDbManager = new LabelDbManager(getActivity());
	// String s = labelDbManager.getLabelsString(appId);
	// assertEquals("", s);
	//
	// labelDbManager.addLabel(appId, "lab1");
	// labelDbManager.addLabel(appId, "lab2");
	// s = labelDbManager.getLabelsString(appId);
	// assertEquals("lab1, lab2", s);
	//
	// labelDbManager.deleteLabel(appId, "lab1");
	// labelDbManager.deleteLabel(appId, "lab2");
	// s = labelDbManager.getLabelsString(appId);
	// assertEquals("", s);
	// }
}
