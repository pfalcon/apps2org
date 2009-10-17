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
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.google.code.appsorganizer.R;

public class ProgressDialogCreator extends GenericDialogCreator {

	private String title;
	private String label;

	public ProgressDialogCreator(GenericDialogManager dialogManager) {
		super(dialogManager);
	}

	public ProgressDialogCreator(GenericDialogManager dialogManager, String title, String label) {
		super(dialogManager);
		this.title = title;
		this.label = label;
	}

	@Override
	public void prepareDialog(Dialog dialog) {
	}

	@Override
	public Dialog createDialog() {
		ProgressDialog pd = new ProgressDialog(owner);
		pd.setTitle(title);
		pd.setMessage(label);
		pd.setIndeterminate(true);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		return pd;
	}

	public View getChooseDialogBody() {
		LayoutInflater factory = LayoutInflater.from(owner);
		View body = factory.inflate(R.layout.choose_label_dialog_body, null);
		return body;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
