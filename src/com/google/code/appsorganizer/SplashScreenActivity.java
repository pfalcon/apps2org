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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SplashScreenActivity extends Activity {
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				pd.setMessage(getText(R.string.preparing_apps_list));
				pd.dismiss();
				Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
				startActivity(intent);
			} else {
				pd.setMessage(getString(R.string.total_apps) + ": " + msg.what);
			}
		}
	};

	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pd = ProgressDialog.show(this, getText(R.string.preparing_apps_list), getText(R.string.please_wait_loading), true, false);
		Thread t = new Thread() {
			@Override
			public void run() {
				ApplicationInfoManager.singleton(getPackageManager()).getAppsArray(handler);
				handler.sendEmptyMessage(-1);
			}
		};
		t.start();
	}
}
