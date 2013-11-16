/**
 * 
 */
package com.google.code.appsorganizer.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.code.appsorganizer.R;

/**
 * @author fabio
 * 
 */
public class FullVersionDialog extends SimpleDialog {

	public static final String FOLDER_ORGANIZER_MARKET_QUERY = "market://search?q=Folder Organizer pub:\"Fabio Collini\"";

	private static final String FULL_DIALOG_SHOWN = "fullDialogShown_2";

	private static final long serialVersionUID = 6310850433343243241L;

	public FullVersionDialog(GenericDialogManager dialogManager) {
		super(dialogManager, dialogManager.getString(R.string.FolderOrganizer_title), dialogManager.getString(R.string.FolderOrganizer_message));
		setIcon(0);
		setOkMessageText(dialogManager.getString(R.string.Open_market));
		onOkListener = new OnOkClickListener() {
			private static final long serialVersionUID = 2629468426417628139L;

			public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(FOLDER_ORGANIZER_MARKET_QUERY));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getOwner().startActivity(intent);
			}
		};
	}

	public boolean showDialogIfFirstTime() {
/*
		SharedPreferences settings = owner.getSharedPreferences("appsOrganizer_pref", 0);
		if (!settings.getBoolean(FULL_DIALOG_SHOWN, false)) {
			showDialog();

			saveVersion(settings);
			return true;
		}
*/
		return false;
	}

	private void saveVersion(SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(FULL_DIALOG_SHOWN, true);
		editor.commit();
	}
}
