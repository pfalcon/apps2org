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
import android.os.Bundle;

import com.google.code.appsorganizer.R;

public class SimpleDialog extends GenericDialogCreator implements Serializable {

	private static final long serialVersionUID = 1L;
	private String title;
	private String message;
	private String okMessageText;
	private boolean showNegativeButton = true;
	protected OnOkClickListener onOkListener;
	protected OnOkClickListener onCancelListener;
	private int icon = R.drawable.alert_dialog_icon;

	public SimpleDialog(GenericDialogManager dialogManager) {
		super(dialogManager);
	}

	public SimpleDialog(GenericDialogManager dialogManager, OnOkClickListener onOkListener) {
		super(dialogManager);
		this.onOkListener = onOkListener;
	}

	public SimpleDialog(GenericDialogManager dialogManager, String title) {
		super(dialogManager);
		this.title = title;
	}

	public SimpleDialog(GenericDialogManager dialogManager, String title, String message) {
		super(dialogManager);
		this.title = title;
		this.message = message;
	}

	public SimpleDialog(GenericDialogManager dialogManager, String title, String message, OnOkClickListener onOkListener) {
		super(dialogManager);
		this.title = title;
		this.message = message;
		this.onOkListener = onOkListener;
	}

	public SimpleDialog(GenericDialogManager dialogManager, String title, OnOkClickListener onOkListener) {
		super(dialogManager);
		this.title = title;
		this.onOkListener = onOkListener;
	}

	public SimpleDialog(GenericDialogManager dialogManager, String title, boolean showNegativeButton) {
		super(dialogManager);
		this.title = title;
		this.showNegativeButton = showNegativeButton;
	}

	public SimpleDialog(GenericDialogManager dialogManager, String title, boolean showNegativeButton, OnOkClickListener onOkListener) {
		super(dialogManager);
		this.title = title;
		this.showNegativeButton = showNegativeButton;
		this.onOkListener = onOkListener;
	}

	@Override
	public void prepareDialog(Dialog dialog) {
		super.prepareDialog(dialog);
		dialog.setTitle(title);
	}

	@Override
	public Dialog createDialog() {
		Builder d = new AlertDialog.Builder(owner).setTitle(title);
		DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (onOkListener != null) {
					onOkListener.onClick(null, dialog, which);
				}
			}
		};
		if (okMessageText != null) {
			d = d.setPositiveButton(okMessageText, onClickListener);
		} else {
			d = d.setPositiveButton(R.string.alert_dialog_ok, onClickListener);
		}
		if (icon > 0) {
			d = d.setIcon(icon);
		}
		if (showNegativeButton) {
			d = d.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (onCancelListener != null) {
						onCancelListener.onClick(null, dialog, which);
					}
				}
			});
		}
		if (message != null) {
			d = d.setMessage(message);
		}
		return d.create();
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

	public boolean isShowNegativeButton() {
		return showNegativeButton;
	}

	public void setShowNegativeButton(boolean showNegativeButton) {
		this.showNegativeButton = showNegativeButton;
	}

	public String getMessage() {
		return message;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getOkMessageText() {
		return okMessageText;
	}

	public void setOkMessageText(String okMessageText) {
		this.okMessageText = okMessageText;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("title__" + getDialogId(), title);
		outState.putString("message__" + getDialogId(), message);
		outState.putString("okMessageText__" + getDialogId(), okMessageText);
		outState.putBoolean("showNegativeButton__" + getDialogId(), showNegativeButton);
		// outState.putSerializable("onOkListener__" + getDialogId(),
		// onOkListener);
		outState.putInt("icon__" + getDialogId(), icon);
	}

	@Override
	public void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		title = state.getString("title__" + getDialogId());
		message = state.getString("message__" + getDialogId());
		okMessageText = state.getString("okMessageText__" + getDialogId());
		showNegativeButton = state.getBoolean("showNegativeButton__" + getDialogId());
		// onOkListener = (OnOkClickListener)
		// state.getSerializable("onOkListener__" + getDialogId());
		icon = state.getInt("icon__" + getDialogId());
	}

	public OnOkClickListener getOnCancelListener() {
		return onCancelListener;
	}

	public void setOnCancelListener(OnOkClickListener onCancelListener) {
		this.onCancelListener = onCancelListener;
	}
}
