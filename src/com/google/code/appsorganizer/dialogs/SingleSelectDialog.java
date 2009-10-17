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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.google.code.appsorganizer.R;

/**
 * @author fabio
 * 
 */
public abstract class SingleSelectDialog extends GenericDialogCreator implements Serializable {

	private static final long serialVersionUID = 1L;

	private final CharSequence[] items;

	private int selectedItem;

	private final String title;

	private final String okButtonText;

	public SingleSelectDialog(GenericDialogManager dialogManager, String title, String okButtonText, CharSequence[] items, int selectedItem) {
		super(dialogManager);
		this.items = items;
		this.selectedItem = selectedItem;
		this.title = title;
		this.okButtonText = okButtonText;
	}

	@Override
	public void prepareDialog(Dialog d) {
		super.prepareDialog(d);
	}

	@Override
	public Dialog createDialog() {
		Builder d = new AlertDialog.Builder(owner).setTitle(title);
		d.setSingleChoiceItems(items, selectedItem, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				selectedItem = which;
			}
		});
		d = d.setPositiveButton(okButtonText, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				onOkClick(dialog, selectedItem);
			}

		});
		d = d.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		return d.create();
	}

	protected abstract void onOkClick(DialogInterface dialog, int selectedItem);
}
