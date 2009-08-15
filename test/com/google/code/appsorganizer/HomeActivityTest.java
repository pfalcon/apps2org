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

import android.R;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.ListView;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<AppsListActivity> {

	public HomeActivityTest() {
		super("com.google.code.appsorganizer", AppsListActivity.class);
	}

	public void testButton() throws Exception {
		AppsListActivity activity = getActivity();
		ListView list = (ListView) activity.findViewById(R.id.list);
		getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
		// getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_DPAD_CENTER);
		// activity.showDialog(ChooseLabelDialog.DIALOG_CHOOSE_LABEL);
	}

}
