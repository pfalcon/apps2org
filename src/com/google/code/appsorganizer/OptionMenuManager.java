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

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author fabio
 * 
 */
public class OptionMenuManager {

	public static boolean onCreateOptionsMenu(Activity context, Menu menu) {
		// Hold on to this
		// mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = context.getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		menu.getItem(0).setIcon(android.R.drawable.ic_menu_rotate);
		// TODO info dialog
		menu.getItem(1).setVisible(false);
		menu.getItem(1).setIcon(android.R.drawable.ic_menu_info_details);

		return true;
	}

	public static boolean onOptionsItemSelected(Activity context, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reload_apps:
			new AppsReloader(context, false).reload();
			return true;
		case R.id.about:
			return true;
		}
		return false;
	}
}
