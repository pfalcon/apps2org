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
import java.util.List;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.Label;
import com.google.code.appsorganizer.shortcut.LabelShortcut;
import com.google.code.appsorganizer.utils.ArrayAdapterSmallRow;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class AppWidgetConfigure extends ListActivity {
	static final String TAG = "ExampleAppWidgetConfigure";

	private static final String PREF_PREFIX_KEY = "label_id_";

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	EditText mAppWidgetPrefix;

	public AppWidgetConfigure() {
		super();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Set the result to CANCELED. This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		// // Set the view layout resource to use.
		// setContentView(R.layout.appwidget_configure);
		//
		// // Find the EditText
		// mAppWidgetPrefix = (EditText) findViewById(R.id.appwidget_prefix);
		//
		// // Bind the action for the save button.
		// findViewById(R.id.save_button).setOnClickListener(mOnClickListener);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

		// mAppWidgetPrefix.setText(loadTitlePref(ExampleAppWidgetConfigure.this,
		// mAppWidgetId));

		showCreateShortcutView();
	}

	public void showCreateShortcutView() {
		final List<Label> labels = DatabaseHelper.initOrSingleton(this).labelDao.getLabels();
		labels.add(0, new Label(LabelShortcut.ALL_LABELS_ID, getString(R.string.all_labels), R.drawable.icon));
		labels.add(1, new Label(LabelShortcut.ALL_STARRED_ID, getString(R.string.Starred_apps), R.drawable.favorites));
		labels.add(2, new Label(LabelShortcut.OTHER_APPS, getString(R.string.other_label), 0));
		setTitle(R.string.choose_labels_for_shortcut);
		setListAdapter(new ArrayAdapterSmallRow<Label>(this, android.R.layout.simple_list_item_1, labels));

		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				Label labelObject = labels.get(pos);
				setupShortcut(labelObject);
				finish();
			}
		});
	}

	private void setupShortcut(Label label) {
		final Context context = AppWidgetConfigure.this;

		saveLabelIdInPref(context, mAppWidgetId, label.getId());

		// Push widget update to surface with newly set prefix
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		AppsOrganizerAppWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId, label);

		// Make sure we pass back the original appWidgetId
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		resultValue.putExtra("LABEL_ID", label.getId());
		setResult(RESULT_OK, resultValue);
		finish();
	}

	static void saveLabelIdInPref(Context context, int appWidgetId, long labelId) {
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefs.putLong(PREF_PREFIX_KEY + appWidgetId, labelId);
		prefs.commit();
	}

	static long loadLabelId(Context context, int appWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1);
	}

	static void deleteTitlePref(Context context, int appWidgetId) {
	}

	static void loadAllTitlePrefs(Context context, ArrayList<Integer> appWidgetIds, ArrayList<String> texts) {
	}
}
