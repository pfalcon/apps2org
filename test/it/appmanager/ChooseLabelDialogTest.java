package it.appmanager;

import com.google.code.appsorganizer.AppsListActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

public class ChooseLabelDialogTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public ChooseLabelDialogTest() {
		super("it.appmanager", AppsListActivity.class);
	}

	public void testButton() throws Exception {
		AppsListActivity activity = getActivity();
		getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
		activity.showDialog(activity.getChooseLabelDialog().getDialogId());
	}

}
