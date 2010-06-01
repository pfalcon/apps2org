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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

public class GenericDialogManager {

	private static final String SIMPLE_DIALOG = "simpleDialog";

	private final Map<Integer, GenericDialogCreator> dialogs = new HashMap<Integer, GenericDialogCreator>();

	private final Activity owner;

	private final Set<Integer> dialogsPrepared = new HashSet<Integer>();
	private final Set<Integer> prepareDialogs = new HashSet<Integer>();

	public GenericDialogManager(Activity owner) {
		this.owner = owner;
	}

	public void onPrepareDialog(int id, Dialog dialog) {
		GenericDialogCreator d = dialogs.get(id);
		if (d != null) {
			d.prepareDialog(dialog);
			dialogsPrepared.add(id);
		}
	}

	public Dialog onCreateDialog(int id) {
		GenericDialogCreator d = dialogs.get(id);
		if (d != null) {
			Dialog dialog = d.createDialog();
			d.setDialog(dialog);
			return dialog;
		} else {
			throw new RuntimeException(owner.getClass().getName() + ": unable to create dialog " + id + " (" + dialogs + ")");
		}
	}

	private SimpleDialog simpleDialog;

	public void onSaveInstanceState(Bundle outState) {
		for (Entry<Integer, GenericDialogCreator> e : dialogs.entrySet()) {
			GenericDialogCreator dialogCreator = e.getValue();
			dialogCreator.onSaveInstanceState(outState);
			Dialog dialog = dialogCreator.getDialog();
			if (dialog != null && dialog.isShowing()) {
				outState.putBoolean("prepareDialog_" + e.getKey(), true);
			}
		}
		if (simpleDialog != null) {
			Dialog dialog = simpleDialog.getDialog();
			if (dialog != null && dialog.isShowing()) {
				outState.putSerializable(SIMPLE_DIALOG, simpleDialog);
			}
		}
	}

	public void onRestoreInstanceState(Bundle state) {
		Serializable serializable = state.getSerializable(SIMPLE_DIALOG);
		if (serializable != null) {
			simpleDialog = (SimpleDialog) serializable;
			addDialog(simpleDialog);
			prepareDialogs.add(simpleDialog.getDialogId());
		}
		for (Entry<Integer, GenericDialogCreator> e : dialogs.entrySet()) {
			GenericDialogCreator dialogCreator = e.getValue();
			dialogCreator.onRestoreInstanceState(state);
			int dialogId = dialogCreator.getDialogId();
			if (!dialogsPrepared.contains(dialogId) && state.getBoolean("prepareDialog_" + e.getKey(), false)) {
				prepareDialogs.add(dialogId);
			}
		}
	}

	public void onResume() {
		for (Entry<Integer, GenericDialogCreator> e : dialogs.entrySet()) {
			GenericDialogCreator dialogCreator = e.getValue();
			if (prepareDialogs.contains(dialogCreator.getDialogId())) {
				dialogCreator.prepareDialog(dialogCreator.getDialog());
			}
		}
	}

	void addDialog(GenericDialogCreator d) {
		int id = dialogs.size();
		d.setDialogId(id);
		d.setOwner(owner);
		dialogs.put(id, d);
	}

	public void showDialog(GenericDialogCreator d) {
		owner.showDialog(d.getDialogId());
	}

	public String getString(int resId) {
		return owner.getString(resId);
	}

	public void onDestroy() {
		for (Entry<Integer, GenericDialogCreator> e : dialogs.entrySet()) {
			GenericDialogCreator dialogCreator = e.getValue();
			Dialog dialog = dialogCreator.getDialog();
			if (dialog != null) {
				dialog.cancel();
			}
		}
	}

}