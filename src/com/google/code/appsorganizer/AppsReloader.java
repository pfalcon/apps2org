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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.code.appsorganizer.db.DatabaseHelper;

public class AppsReloader {

	private final Context context;

	private final boolean discardCache;

	public AppsReloader(Context context, boolean discardCache) {
		this.context = context;
		this.discardCache = discardCache;
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 != 0) {
				pd.setMax(msg.arg1);
			} else if (msg.what == -1) {
				pd.setMessage(context.getText(R.string.preparing_apps_list));
				try {
					pd.dismiss();
				} catch (IllegalArgumentException ignored) {
				}
			} else {
				pd.incrementProgressBy(1);
			}
		}
	};

	private ProgressDialog pd;

	public void reload() {
		pd = new ProgressDialog(context);
		pd.setTitle(context.getText(R.string.preparing_apps_list));
		pd.setMessage(context.getText(R.string.please_wait_loading));
		pd.setIndeterminate(false);
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.show();

		Thread t = new Thread() {
			@Override
			public void run() {
				DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);
				ApplicationInfoManager.reloadAll(context.getPackageManager(), dbHelper, handler, discardCache, null);
				handler.sendEmptyMessage(-1);
			}
		};
		t.start();
	}
}
