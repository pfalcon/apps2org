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
package com.google.code.appsorganizer.shortcut;

import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.Label;
import com.google.code.appsorganizer.utils.ArrayAdapterSmallRow;

/**
 * @author fabio
 * 
 */
public class ShortcutCreator extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		Intent intent = createIntent(this, label.getId(), label.getName(), label.getImageBytes(), label.getIcon());
		setResult(LabelShortcut.RESULT_OK, intent);
	}

	public static Intent createIntent(Activity a, Long id, String name, byte[] imageBytes, int icon) {
		Intent shortcutIntent = createOpenLabelIntent(a, id);

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

		if (imageBytes != null) {
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
		} else {
			Parcelable iconResource = Intent.ShortcutIconResource.fromContext(a, icon);
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
		}
		return intent;
	}

	public static Intent createOpenLabelIntent(Context a, Long id) {
		Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		shortcutIntent.setClassName(a, LabelShortcut.class.getName());
		shortcutIntent.putExtra(LabelShortcut.LABEL_ID, id);
		return shortcutIntent;
	}
}
