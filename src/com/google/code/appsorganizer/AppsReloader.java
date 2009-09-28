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
			if (msg.what == -1) {
				pd.setMessage(context.getText(R.string.preparing_apps_list));
				ApplicationChangeListenerManager.notifyDataSetChanged(this);
				pd.hide();
			} else {
				pd.setMessage(context.getString(R.string.total_apps) + ": " + msg.what);
			}
		}
	};

	private ProgressDialog pd;

	public void reload() {
		pd = ProgressDialog.show(context, context.getText(R.string.preparing_apps_list), context.getText(R.string.please_wait_loading),
				true, false);
		Thread t = new Thread() {
			@Override
			public void run() {
				DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);
				ApplicationInfoManager.reloadAll(context.getPackageManager(), dbHelper, handler, discardCache);
				handler.sendEmptyMessage(-1);
			}
		};
		t.start();
	}
}
