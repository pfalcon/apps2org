package com.google.code.appsorganizer.dialogs;

import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;

public class GenericDialogManager {

	private final Map<Integer, GenericDialogCreator<?>> dialogs = new HashMap<Integer, GenericDialogCreator<?>>();

	public void addDialog(GenericDialogCreator<?> d) {
		int id = dialogs.size();
		d.setDialogId(id);
		dialogs.put(id, d);
	}

	public void onPrepareDialog(int id, Dialog dialog) {
		dialogs.get(id).prepareDialog(dialog);
	}

	public Dialog onCreateDialog(int id) {
		return dialogs.get(id).createDialog();
	}
}