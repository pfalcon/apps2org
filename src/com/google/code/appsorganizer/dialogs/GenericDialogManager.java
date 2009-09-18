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

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;

public class GenericDialogManager {

	private final Map<Integer, GenericDialogCreator> dialogs = new HashMap<Integer, GenericDialogCreator>();

	private final Activity owner;

	public GenericDialogManager(Activity owner) {
		this.owner = owner;
	}

	public void addDialog(GenericDialogCreator d) {
		int id = dialogs.size();
		d.setDialogId(id);
		d.setOwner(owner);
		dialogs.put(id, d);
	}

	public void onPrepareDialog(int id, Dialog dialog) {
		GenericDialogCreator d = dialogs.get(id);
		if (d != null) {
			d.prepareDialog(dialog);
		}
	}

	public Dialog onCreateDialog(int id) {
		GenericDialogCreator d = dialogs.get(id);
		if (d != null) {
			return d.createDialog();
		}
		return null;
	}

	private SimpleDialog simpleDialog;

	public void showSimpleDialog(String title, boolean showNegativeButton, OnOkClickListener onOkListener) {
		if (simpleDialog == null) {
			simpleDialog = new SimpleDialog();
			addDialog(simpleDialog);
		}
		simpleDialog.setTitle(title);
		simpleDialog.setOnOkListener(onOkListener);
		simpleDialog.setShowNegativeButton(showNegativeButton);
		owner.showDialog(simpleDialog.getDialogId());
	}

	public void showSimpleDialog(int title, int message, boolean showNegativeButton, OnOkClickListener onOkListener) {
		showSimpleDialog(owner.getString(title), owner.getString(message), showNegativeButton, onOkListener);
	}

	public void showSimpleDialog(String title, String message, boolean showNegativeButton, OnOkClickListener onOkListener) {
		if (simpleDialog == null || !message.equals(simpleDialog.getMessage())) {
			simpleDialog = new SimpleDialog(title, message);
			addDialog(simpleDialog);
		}
		simpleDialog.setTitle(title);
		simpleDialog.setOnOkListener(onOkListener);
		simpleDialog.setShowNegativeButton(showNegativeButton);
		owner.showDialog(simpleDialog.getDialogId());
	}

	public void showSimpleDialog(String title, String message, boolean showNegativeButton, OnOkClickListener onOkListener,
			String okMessageText) {
		if (simpleDialog == null || !message.equals(simpleDialog.getMessage()) || !okMessageText.equals(simpleDialog.getOkMessageText())) {
			simpleDialog = new SimpleDialog(title, message);
			addDialog(simpleDialog);
		}
		simpleDialog.setTitle(title);
		simpleDialog.setOnOkListener(onOkListener);
		simpleDialog.setOkMessageText(okMessageText);
		simpleDialog.setShowNegativeButton(showNegativeButton);
		owner.showDialog(simpleDialog.getDialogId());
	}

	public void showSimpleDialog(String title, String message, boolean showNegativeButton, int icon, OnOkClickListener onOkListener) {
		if (simpleDialog == null || !message.equals(simpleDialog.getMessage()) || simpleDialog.getIcon() != icon) {
			simpleDialog = new SimpleDialog(title, message);
			simpleDialog.setIcon(icon);
			addDialog(simpleDialog);
		}
		simpleDialog.setTitle(title);
		simpleDialog.setOnOkListener(onOkListener);
		simpleDialog.setShowNegativeButton(showNegativeButton);
		owner.showDialog(simpleDialog.getDialogId());
	}
}