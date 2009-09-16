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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @author fabio
 * 
 */
public class BugReportActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bug_report);
		findViewById(R.id.sendEmailButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "appsorganizer@gmail.com" });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Bug report");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getExceptionString());
				startActivity(Intent.createChooser(emailIntent, getText(R.string.Bug_report)));
			}
		});
	}

	private String getExceptionString() {
		final Throwable t = (Throwable) getIntent().getSerializableExtra("exception");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return "Version: " + AboutDialogCreator.getVersionName(this) + "\n" + sw.toString();
	}

	public static void registerExceptionHandler(final Context context) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable ex) {
				Intent intent = new Intent(context, BugReportActivity.class);
				intent.putExtra("exception", ex);
				ex.printStackTrace();
				context.startActivity(intent);
			}
		});
	}
}
