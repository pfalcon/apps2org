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

import java.util.ArrayList;

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
import com.google.code.appsorganizer.shortcut.LabelShortcut;
import com.google.code.appsorganizer.shortcut.ShortcutCreator;

public class AppsOrganizerAppWidgetProvider extends AppWidgetProvider {
	// log tag
	private static final String TAG = "AppsOrganizer.AppWidgetProvider";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate");
		final int N = appWidgetIds.length;
		DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			long labelId = AppWidgetConfigure.loadLabelId(context, appWidgetId);
			Label label = getLabel(context, dbHelper, labelId);
			updateAppWidget(context, appWidgetManager, appWidgetId, label);
		}
	}

	private Label getLabel(Context context, DatabaseHelper dbHelper, long labelId) {
		if (labelId == LabelShortcut.ALL_LABELS_ID) {
			return new Label(labelId, context.getString(R.string.all_labels), R.drawable.icon);
		}
		if (labelId == LabelShortcut.ALL_STARRED_ID) {
			return new Label(labelId, context.getString(R.string.Starred_apps), R.drawable.favorites);
		}
		if (labelId == LabelShortcut.OTHER_APPS) {
			return new Label(labelId, context.getString(R.string.other_label), 0);
		}
		return dbHelper.labelDao.queryById(labelId);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.d(TAG, "onDeleted");
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			AppWidgetConfigure.deleteWidgetPref(context, appWidgetIds[i]);
		}
	}

	@Override
	public void onEnabled(Context context) {
	}

	@Override
	public void onDisabled(Context context) {
	}

	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Label label) {
		Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " titlePrefix=" + label);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
		if (label != null) {
			views.setTextViewText(R.id.appwidget_text, label.getLabel());
			byte[] imageBytes = label.getImageBytes();
			if (imageBytes != null) {
				views.setImageViewBitmap(R.id.appwidget_image, BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
			} else {
				views.setImageViewResource(R.id.appwidget_image, label.getIcon());
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(context, label.getId().intValue(), ShortcutCreator.createOpenLabelIntent(context,
					label.getId()), 0);
			views.setOnClickPendingIntent(R.id.widget, pendingIntent);
		} else {
			views.setTextViewText(R.id.appwidget_text, context.getText(R.string.Deleted_label));
			views.setImageViewResource(R.id.appwidget_image, R.drawable.icon_default);
		}
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	public static void updateAppWidget(Context context, Label label) {
		ArrayList<Integer> widgets = AppWidgetConfigure.getWidgets(context, label.getId());
		if (!widgets.isEmpty()) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			for (Integer widgetId : widgets) {
				updateAppWidget(context, appWidgetManager, widgetId, label);
			}
		}
	}
}
