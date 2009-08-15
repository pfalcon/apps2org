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

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.code.appsorganizer.R;

public class ChooseIconActivity extends Activity {
	private GridView mGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadIcons();

		setContentView(com.google.code.appsorganizer.R.layout.icon_grid);
		mGrid = (GridView) findViewById(com.google.code.appsorganizer.R.id.iconGrid);
		mGrid.setAdapter(new AppsAdapter());
		mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent res = new Intent();
				res.putExtra("icon", mIcons.get(pos));
				setResult(RESULT_OK, res);
				finish();
			}
		});
	}

	private List<Integer> mIcons;

	private void loadIcons() {
		mIcons = Arrays.asList(R.drawable.agt_member, R.drawable.binary, R.drawable.cam_unmount, R.drawable.camera_unmount,
				R.drawable.cardgame, R.drawable.cdimage, R.drawable.chardevice, R.drawable.doc, R.drawable.energy, R.drawable.favorites,
				R.drawable.file_temporary, R.drawable.floppy_unmount, R.drawable.globe, R.drawable.hardware, R.drawable.icon_default,
				R.drawable.image2, R.drawable.images, R.drawable.internet_connection_tools, R.drawable.joystick, R.drawable.kbackgammon,
				R.drawable.keyboard, R.drawable.kfm_home, R.drawable.klaptopdaemon, R.drawable.knode, R.drawable.kontact,
				R.drawable.kopete, R.drawable.kpaint, R.drawable.kpat, R.drawable.kspread_ksp, R.drawable.kstars, R.drawable.kweather,
				R.drawable.messenger, R.drawable.midi, R.drawable.mp3, R.drawable.mp3player_alt_unmount, R.drawable.multimedia,
				R.drawable.multimedia2, R.drawable.news, R.drawable.package_favorite, R.drawable.package_games_board,
				R.drawable.package_games_strategy, R.drawable.pda_black, R.drawable.schedule, R.drawable.service_manager, R.drawable.sms,
				R.drawable.video);
	}

	public class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i;

			if (convertView == null) {
				i = new ImageView(ChooseIconActivity.this);
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				i.setLayoutParams(new GridView.LayoutParams(50, 50));
			} else {
				i = (ImageView) convertView;
			}

			i.setImageResource(mIcons.get(position));
			return i;
		}

		public final int getCount() {
			return mIcons.size();
		}

		public final Object getItem(int position) {
			return mIcons.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

}
