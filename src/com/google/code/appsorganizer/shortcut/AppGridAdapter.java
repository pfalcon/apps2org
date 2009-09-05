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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.model.Application;
import com.google.code.appsorganizer.model.GridObject;

public final class AppGridAdapter<T extends GridObject> extends BaseAdapter {
	private T[] objectList;

	private final Context context;

	public AppGridAdapter(T[] objectList, Context context) {
		this.objectList = objectList;
		this.context = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater factory = LayoutInflater.from(context);
			convertView = factory.inflate(R.layout.app_cell_with_icon, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.image);
			holder.icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
			convertView.setLayoutParams(new GridView.LayoutParams(50, 78));
			holder.text = (TextView) convertView.findViewById(R.id.name);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		T a = objectList[position];
		if (!(a instanceof Application) || ((Application) a).getIcon() != null) {
			a.showIcon(holder.icon);
		} else {
			holder.icon.setImageDrawable(LabelShortcut.DRAWABLE_DEFAULT);
		}
		holder.text.setText(a.getLabel());
		return convertView;
	}

	private static final class ViewHolder {
		TextView text;
		ImageView icon;
	}

	public final int getCount() {
		return objectList.length;
	}

	public final T getItem(int position) {
		return objectList[position];
	}

	public final long getItemId(int position) {
		return position;
	}

	public void setObjectList(T[] objectList) {
		this.objectList = objectList;
	}
}