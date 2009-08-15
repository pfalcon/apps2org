package com.google.code.appsorganizer.dialogs;

import android.app.Activity;
import android.app.Dialog;

public abstract class GenericDialogCreator<T extends Activity> {

	private int dialogId = 1;

	protected final T owner;

	public GenericDialogCreator(T owner) {
		this.owner = owner;
	}

	public int getDialogId() {
		return dialogId;
	}

	public void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}

	public void prepareDialog(Dialog dialog) {

	}

	public abstract Dialog createDialog();
}