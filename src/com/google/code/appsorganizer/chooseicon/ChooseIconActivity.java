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
package com.google.code.appsorganizer.chooseicon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.code.appsorganizer.model.Label;

public class ChooseIconActivity extends Activity {
	private GridView mGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadIcons();

		final int group = getIntent().getIntExtra("group", -1);
		setContentView(com.google.code.appsorganizer.R.layout.icon_grid);
		mGrid = (GridView) findViewById(com.google.code.appsorganizer.R.id.iconGrid);
		mGrid.setAdapter(new AppsAdapter());
		mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent res = new Intent();
				res.putExtra("icon", mIcons[pos]);
				res.putExtra("group", group);
				setResult(RESULT_OK, res);
				finish();
			}
		});
	}

	private int[] mIcons;

	private void loadIcons() {
		// mIcons = new int[] { android.R.drawable.ic_menu_add,
		// android.R.drawable.ic_menu_agenda,
		// android.R.drawable.ic_menu_always_landscape_portrait,
		// android.R.drawable.ic_menu_call, android.R.drawable.ic_menu_camera,
		// android.R.drawable.ic_menu_close_clear_cancel,
		// android.R.drawable.ic_menu_compass, android.R.drawable.ic_menu_crop,
		// android.R.drawable.ic_menu_day, android.R.drawable.ic_menu_delete,
		// android.R.drawable.ic_menu_directions,
		// android.R.drawable.ic_menu_edit, android.R.drawable.ic_menu_gallery,
		// android.R.drawable.ic_menu_help,
		// android.R.drawable.ic_menu_info_details,
		// android.R.drawable.ic_menu_manage,
		// android.R.drawable.ic_menu_mapmode,
		// android.R.drawable.ic_menu_month, android.R.drawable.ic_menu_more,
		// android.R.drawable.ic_menu_my_calendar,
		// android.R.drawable.ic_menu_mylocation,
		// android.R.drawable.ic_menu_myplaces,
		// android.R.drawable.ic_menu_preferences,
		// android.R.drawable.ic_menu_recent_history,
		// android.R.drawable.ic_menu_report_image,
		// android.R.drawable.ic_menu_revert,
		// android.R.drawable.ic_menu_rotate, android.R.drawable.ic_menu_save,
		// android.R.drawable.ic_menu_search,
		// android.R.drawable.ic_menu_send, android.R.drawable.ic_menu_set_as,
		// android.R.drawable.ic_menu_share,
		// android.R.drawable.ic_menu_slideshow,
		// android.R.drawable.ic_menu_sort_alphabetically,
		// android.R.drawable.ic_menu_sort_by_size,
		// android.R.drawable.ic_menu_today, android.R.drawable.ic_menu_upload,
		// android.R.drawable.ic_menu_upload_you_tube,
		// android.R.drawable.ic_menu_view, android.R.drawable.ic_menu_week,
		// android.R.drawable.ic_menu_zoom
		// };
		mIcons = Label.getIconsList();
	}

	public class AppsAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i;

			if (convertView == null) {
				i = new ImageView(ChooseIconActivity.this);
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				i.setLayoutParams(new GridView.LayoutParams(50, 50));
			} else {
				i = (ImageView) convertView;
			}

			i.setImageResource(mIcons[position]);
			return i;
		}

		public final int getCount() {
			return mIcons.length;
		}

		public final Object getItem(int position) {
			return mIcons[position];
		}

		public final long getItemId(int position) {
			return position;
		}
	}

}
