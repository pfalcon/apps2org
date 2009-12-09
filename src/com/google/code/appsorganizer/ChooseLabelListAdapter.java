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
package com.google.code.appsorganizer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

import com.google.code.appsorganizer.utils.ArrayAdapterSmallRow;

public class ChooseLabelListAdapter extends ArrayAdapterSmallRow<AppLabelBinding> {

	public ChooseLabelListAdapter(Activity context, List<AppLabelBinding> list) {
		super(context, android.R.layout.simple_list_item_multiple_choice, list);
	}

	public void addLabel(String l) {
		AppLabelBinding label = new AppLabelBinding(l, null, false);
		label.checked = true;
		insert(label, 0);
	}

	public List<AppLabelBinding> getModifiedLabels() {
		List<AppLabelBinding> ret = new ArrayList<AppLabelBinding>();
		int count = getCount();
		for (int i = 0; i < count; i++) {
			AppLabelBinding label = getItem(i);
			if (label.isModified()) {
				ret.add(label);
			}
		}
		return ret;
	}
}