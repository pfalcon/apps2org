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
package com.google.code.appsorganizer.dialogs;

import android.app.Dialog;
import android.app.ListActivity;
import android.os.Bundle;

/**
 * @author fabio
 * 
 */
public class ListActivityWithDialog extends ListActivity implements GenericDialogManagerActivity {

	private GenericDialogManager dialogManager;

	public GenericDialogManager getGenericDialogManager() {
		if (dialogManager == null) {
			dialogManager = new GenericDialogManager(this);
		}
		return dialogManager;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getGenericDialogManager().onResume();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		getGenericDialogManager().onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return getGenericDialogManager().onCreateDialog(id);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getGenericDialogManager().onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		getGenericDialogManager().onRestoreInstanceState(state);
	}

	public void showDialog(GenericDialogCreator d) {
		getGenericDialogManager().showDialog(d);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getGenericDialogManager().onDestroy();
	}

}
