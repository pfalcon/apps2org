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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import com.google.code.appsorganizer.R;

public class ConfirmDialog extends GenericDialogCreator {

	private String title;
	private OnOkClickListener onOkListener;

	public ConfirmDialog() {
	}

	public ConfirmDialog(String title) {
		this.title = title;
	}

	public ConfirmDialog(String title, OnOkClickListener onOkListener) {
		this.title = title;
		this.onOkListener = onOkListener;
	}

	@Override
	public Dialog createDialog() {
		return new AlertDialog.Builder(owner).setIcon(R.drawable.alert_dialog_icon).setTitle(title).setPositiveButton(
				R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						onOkListener.onClick(null, dialog, which);
					}
				}).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).create();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public OnOkClickListener getOnOkListener() {
		return onOkListener;
	}

	public void setOnOkListener(OnOkClickListener onOkListener) {
		this.onOkListener = onOkListener;
	}
}
