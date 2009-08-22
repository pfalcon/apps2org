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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.code.appsorganizer.Application;
import com.google.code.appsorganizer.R;

public final class AppGridAdapter extends BaseAdapter {
	private List<Application> applicationList;

	private final Context context;

	public AppGridAdapter(List<Application> applicationList, Context context) {
		this.applicationList = applicationList;
		this.context = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView i;
		TextView t;

		if (convertView == null) {
			LayoutInflater factory = LayoutInflater.from(context);
			convertView = factory.inflate(R.layout.app_cell_with_icon, null);
			i = (ImageView) convertView.findViewById(R.id.image);
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			convertView.setLayoutParams(new GridView.LayoutParams(50, 78));
			t = (TextView) convertView.findViewById(R.id.name);
		} else {
			i = (ImageView) convertView.findViewById(R.id.image);
			t = (TextView) convertView.findViewById(R.id.name);
		}

		Application a = applicationList.get(position);
		i.setImageDrawable(a.getIcon());
		t.setText(a.getLabel());
		return convertView;
	}

	public final int getCount() {
		return applicationList.size();
	}

	public final Application getItem(int position) {
		return applicationList.get(position);
	}

	public final long getItemId(int position) {
		return position;
	}

	public void setApplicationList(List<Application> applicationList) {
		this.applicationList = applicationList;
		this.notifyDataSetChanged();
	}
}