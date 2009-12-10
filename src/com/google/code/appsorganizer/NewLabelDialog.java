/**
 * 
 */
package com.google.code.appsorganizer;

import android.content.DialogInterface;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.SimpleDialog;
import com.google.code.appsorganizer.dialogs.TextEntryDialog;

public class NewLabelDialog extends TextEntryDialog {

	private final SimpleDialog labelAlreadExistsDialog;

	public NewLabelDialog(GenericDialogManager dialogManager, final OnOkClickListener onLabelAdded) {
		super(dialogManager, dialogManager.getString(R.string.label_name), null);

		labelAlreadExistsDialog = new SimpleDialog(dialogManager, dialogManager.getString(R.string.label_already_exists));
		labelAlreadExistsDialog.setShowNegativeButton(false);

		setOnOkListener(new OnOkClickListener() {

			private static final long serialVersionUID = 2019601442580318350L;

			public void onClick(CharSequence t, DialogInterface dialog, int which) {
				if (t != null && t.length() > 0) {
					if (DatabaseHelper.initOrSingleton(getOwner()).labelDao.labelAlreadyExists(t.toString())) {
						labelAlreadExistsDialog.showDialog();
					} else {
						onLabelAdded.onClick(t, dialog, which);
					}
				}
			}
		});
	}

}