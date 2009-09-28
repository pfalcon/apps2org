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
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.code.appsorganizer.R;

public class TextEntryDialog extends GenericDialogCreator {

	private static final String DEFAULT_VALUE = "TextEntry_default";
	private String title;
	private String label;
	private String defaultValue;
	private OnOkClickListener onOkListener;

	public TextEntryDialog(GenericDialogManager dialogManager) {
		super(dialogManager);
	}

	public TextEntryDialog(GenericDialogManager dialogManager, String title, String label) {
		super(dialogManager);
		this.title = title;
		this.label = label;
	}

	public TextEntryDialog(GenericDialogManager dialogManager, String title, String label, OnOkClickListener onOkListener) {
		super(dialogManager);
		this.title = title;
		this.label = label;
		this.onOkListener = onOkListener;
	}

	@Override
	public void prepareDialog(Dialog dialog) {
		TextView labelView = (TextView) dialog.findViewById(R.id.text_entry_label);
		labelView.setText(label);

		TextView editView = (TextView) dialog.findViewById(R.id.text_entry_edit);
		editView.setText(defaultValue);
		editView.setSelectAllOnFocus(true);
	}

	@Override
	public Dialog createDialog() {
		LayoutInflater factory = LayoutInflater.from(owner);
		final View textEntryView = factory.inflate(R.layout.dialog_text_entry, null);
		final AlertDialog dialog = new AlertDialog.Builder(owner).setView(textEntryView).setTitle(title).setPositiveButton(
				R.string.alert_dialog_ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						TextView editView = (TextView) ((AlertDialog) dialog).findViewById(R.id.text_entry_edit);
						onOkListener.onClick(editView.getText(), dialog, which);
					}
				}).setNegativeButton(R.string.alert_dialog_cancel, null).create();
		return dialog;
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

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public OnOkClickListener getOnOkListener() {
		return onOkListener;
	}

	public void setOnOkListener(OnOkClickListener onOkListener) {
		this.onOkListener = onOkListener;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(DEFAULT_VALUE, defaultValue);
	}

	@Override
	public void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		defaultValue = state.getString(DEFAULT_VALUE);
	}
}
