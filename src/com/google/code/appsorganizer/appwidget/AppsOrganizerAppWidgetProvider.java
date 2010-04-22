/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.appsorganizer.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.Label;
import com.google.code.appsorganizer.shortcut.ShortcutCreator;

public class AppsOrganizerAppWidgetProvider extends AppWidgetProvider {
	// log tag
	private static final String TAG = "ExampleAppWidgetProvider";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate");
		// For each widget that needs an update, get the text that we should
		// display:
		// - Create a RemoteViews object for it
		// - Set the text in the RemoteViews object
		// - Tell the AppWidgetManager to show that views object for the widget.
		final int N = appWidgetIds.length;
		DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			long labelId = AppWidgetConfigure.loadLabelId(context, appWidgetId);
			Label label = dbHelper.labelDao.queryById(labelId);
			updateAppWidget(context, appWidgetManager, appWidgetId, label);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// Log.d(TAG, "onDeleted");
		// // When the user deletes the widget, delete the preference associated
		// // with it.
		// final int N = appWidgetIds.length;
		// for (int i = 0; i < N; i++) {
		// ExampleAppWidgetConfigure.deleteTitlePref(context, appWidgetIds[i]);
		// }
	}

	@Override
	public void onEnabled(Context context) {
		// Log.d(TAG, "onEnabled");
		// // When the first widget is created, register for the
		// TIMEZONE_CHANGED
		// // and TIME_CHANGED
		// // broadcasts. We don't want to be listening for these if nobody has
		// our
		// // widget active.
		// // This setting is sticky across reboots, but that doesn't matter,
		// // because this will
		// // be called after boot if there is a widget instance for this
		// provider.
		// PackageManager pm = context.getPackageManager();
		// pm.setComponentEnabledSetting(new
		// ComponentName("com.google.code.appsorganizer.appwidget",
		// ".appwidget.ExampleBroadcastReceiver"),
		// PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		// PackageManager.DONT_KILL_APP);
	}

	@Override
	public void onDisabled(Context context) {
		// When the first widget is created, stop listening for the
		// TIMEZONE_CHANGED and
		// TIME_CHANGED broadcasts.
		// Log.d(TAG, "onDisabled");
		// PackageManager pm = context.getPackageManager();
		// pm.setComponentEnabledSetting(new
		// ComponentName("com.example.android.apis",
		// ".appwidget.ExampleBroadcastReceiver"),
		// PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		// PackageManager.DONT_KILL_APP);
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Label label) {
		Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " titlePrefix=" + label);

		if (label != null) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
			views.setTextViewText(R.id.appwidget_text, label.getLabel());
			byte[] imageBytes = label.getImageBytes();
			if (imageBytes != null) {
				views.setImageViewBitmap(R.id.appwidget_image, BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
			} else {
				views.setImageViewResource(R.id.appwidget_image, label.getIcon());
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(context, label.getId().intValue(), ShortcutCreator.createOpenLabelIntent(context,
					label.getId()), 0);
			views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
			views.setOnClickPendingIntent(R.id.appwidget_image, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
