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
package com.google.code.appsorganizer.livefolder;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.Label;

public class LabelLiveFolder extends ListActivity {
	public static final Uri CONTENT_URI = Uri.parse("content://com.google.code.appsorganizer/live_folders/");

	private DatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final String action = intent.getAction();

		dbHelper = DatabaseHelper.initOrSingleton(this);

		if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
			final List<Label> labels = dbHelper.labelDao.getLabels();
			setListAdapter(new ArrayAdapter<Label>(this, android.R.layout.simple_list_item_1, labels));

			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
					CharSequence label = ((TextView) v).getText();
					Label labelObject = getLabelId(label);
					setResult(RESULT_OK, createLiveFolder(LabelLiveFolder.this, Uri
							.parse("content://com.google.code.appsorganizer/live_folders/" + labelObject.getId()), labelObject.getName(),
							labelObject.getIcon()));
					finish();
				}

				private Label getLabelId(CharSequence label) {
					for (Label l : labels) {
						if (l.getName().equals(label.toString())) {
							return l;
						}
					}
					throw new RuntimeException("Label " + label + " non trovata");
				}
			});
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	private static Intent createLiveFolder(Context context, Uri uri, String name, int icon) {
		final Intent intent = new Intent();
		intent.setData(uri);
		intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, name);
		intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, Intent.ShortcutIconResource.fromContext(context, icon));
		intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE, LiveFolders.DISPLAY_MODE_GRID);
		return intent;
	}
}
