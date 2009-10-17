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
package com.google.code.appsorganizer.download;

import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;

/**
 * @author fabio
 * 
 */
public class ConfirmLabelDownloadDialog extends GenericDialogCreator implements Serializable {

	private static final long serialVersionUID = 1L;

	private final OnClickListener onClickListener;

	public ConfirmLabelDownloadDialog(GenericDialogManager dialogManager, OnClickListener onClickListener) {
		super(dialogManager);
		this.onClickListener = onClickListener;
	}

	@Override
	public void prepareDialog(Dialog d) {
		super.prepareDialog(d);
	}

	@Override
	public Dialog createDialog() {
		Builder d = new AlertDialog.Builder(owner).setTitle(R.string.Download_labels_from_Cyrket);
		CharSequence[] items = new CharSequence[] { owner.getString(R.string.All_apps), owner.getString(R.string.Apps_with_no_label) };
		d.setSingleChoiceItems(items, 0, onClickListener);
		d = d.setPositiveButton(R.string.Download, onClickListener);
		d = d.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		return d.create();
	}

}
