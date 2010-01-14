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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @author fabio
 * 
 */
public class BugReportActivity extends Activity {

	private static final String EXCEPTION = "exception";
	private static final String LAST_EXCEPTION = "lastException";
	private static final String LAST_EXCEPTION_VERSION = "lastExceptionVersion";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bug_report);
		findViewById(R.id.sendEmailButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String exceptionString = getExceptionString();
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "appsorganizer@gmail.com" });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Bug report");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, exceptionString);
				startActivity(Intent.createChooser(emailIntent, getText(R.string.Bug_report)));
			}
		});
		saveExceptionToPreferences(this, null);
	}

	private String getExceptionString() {
		return "Version: " + getIntent().getStringExtra(LAST_EXCEPTION_VERSION) + "\nCurrent version: " + AboutDialogCreator.getVersionName(this)
				+ "\nAndroid version: " + Build.VERSION.SDK + "\n" + getIntent().getStringExtra(EXCEPTION);
	}

	private static void startBugreportActivity(final Context context, String exceptionString, String version) {
		Intent intent = new Intent(context, BugReportActivity.class);
		intent.putExtra(EXCEPTION, exceptionString);
		intent.putExtra(LAST_EXCEPTION_VERSION, version);
		context.startActivity(intent);
	}

	private static void saveExceptionToPreferences(final Context context, Throwable ex) {
		SharedPreferences settings = context.getSharedPreferences("appsOrganizer_pref", 0);
		Editor edit = settings.edit();
		if (ex != null) {
			edit.putString(LAST_EXCEPTION, convertToString(ex));
			edit.putString(LAST_EXCEPTION_VERSION, AboutDialogCreator.getVersionName(context));
		} else {
			edit.remove(LAST_EXCEPTION);
		}
		edit.commit();
	}

	private static String convertToString(final Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	public static void registerExceptionHandler(final Context context) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable ex) {
				ex.printStackTrace();
				saveExceptionToPreferences(context, ex);
				throw new RuntimeException(ex);
				// startBugreportActivity(context, convertToString(ex));
			}
		});
	}

	public static void showLastException(Context context) {
		SharedPreferences settings = context.getSharedPreferences("appsOrganizer_pref", 0);
		String lastException = settings.getString(LAST_EXCEPTION, null);
		if (lastException != null) {
			startBugreportActivity(context, lastException, settings.getString(LAST_EXCEPTION_VERSION, "Version not specified"));
		}
	}
}
