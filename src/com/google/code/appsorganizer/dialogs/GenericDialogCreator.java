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

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

public abstract class GenericDialogCreator {

	private int dialogId;

	protected Activity owner;

	private Dialog dialog;

	private final GenericDialogManager dialogManager;

	public GenericDialogCreator(GenericDialogManager dialogManager) {
		dialogManager.addDialog(this);
		this.dialogManager = dialogManager;
	}

	int getDialogId() {
		return dialogId;
	}

	void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}

	public void prepareDialog(Dialog dialog) {

	}

	public abstract Dialog createDialog();

	public Activity getOwner() {
		return owner;
	}

	public void setOwner(Activity owner) {
		this.owner = owner;
	}

	public Dialog getDialog() {
		return dialog;
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	public void onSaveInstanceState(Bundle outState) {
	}

	public void onRestoreInstanceState(Bundle state) {
	}

	public void showDialog() {
		dialogManager.showDialog(this);
	}
}