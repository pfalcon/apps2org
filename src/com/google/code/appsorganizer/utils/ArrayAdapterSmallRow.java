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
package com.google.code.appsorganizer.utils;

import java.util.List;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

public class ArrayAdapterSmallRow<T> extends ArrayAdapter<T> {

	public ArrayAdapterSmallRow(Activity context, int resource, int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public ArrayAdapterSmallRow(Activity context, int resource, int textViewResourceId, T[] objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public ArrayAdapterSmallRow(Activity context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}

	public ArrayAdapterSmallRow(Activity context, int textViewResourceId, List<T> objects) {
		super(context, textViewResourceId, objects);
	}

	public ArrayAdapterSmallRow(Activity context, int textViewResourceId, T[] objects) {
		super(context, textViewResourceId, objects);
	}

	public ArrayAdapterSmallRow(Activity context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		if (convertView == null) {
			DisplayMetrics dm = new DisplayMetrics();
			((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
			view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, (int) (44 * dm.density)));
		}
		return view;
	}
}