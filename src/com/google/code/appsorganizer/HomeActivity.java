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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {

	private static final String ALERT_0_4_PREF = "alert_0_4";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Debug.startMethodTracing("splash");

		// requestWindowFeature(Window.FEATURE_PROGRESS);
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(getText(R.string.tab_apps)).setContent(
				new Intent(this, AppsListActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(getText(R.string.tab_labels)).setContent(
				new Intent(this, LabelListActivity.class)));
		// setProgressBarIndeterminateVisibility(true);

	}

	private void reloadApps() {
		new AppsReloader(this, true).reload();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(HomeActivity.this).setMessage(R.string.alert_0_4).setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						SharedPreferences pref = getPreferences(Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = pref.edit();
						editor.putBoolean(ALERT_0_4_PREF, true);
						editor.commit();

						reloadApps();
					}
				}).create();
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// rewrite this method to avoid ClassCastException on device rotation
		getTabHost().setCurrentTabByTag(state.getString("currentTab"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		// mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		menu.getItem(0).setIcon(android.R.drawable.ic_menu_rotate);
		// TODO info dialog
		menu.getItem(1).setVisible(false);
		menu.getItem(1).setIcon(android.R.drawable.ic_menu_info_details);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reload_apps:
			new AppsReloader(this, false).reload();
			return true;
		case R.id.about:
			return true;
		}
		return false;
	}
}
