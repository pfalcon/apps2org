package it.appmanager;

import com.google.code.appsorganizer.AppsListActivity;

import android.R;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.ListView;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public HomeActivityTest() {
		super("it.appmanager", AppsListActivity.class);
	}

	public void testButton() throws Exception {
		AppsListActivity activity = getActivity();
		ListView list = (ListView) activity.findViewById(R.id.list);
		getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
		// getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_DPAD_CENTER);
		// activity.showDialog(ChooseLabelDialog.DIALOG_CHOOSE_LABEL);
	}

}
