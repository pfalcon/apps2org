/**
 * 
 */
package com.google.code.appsorganizer.dialogs;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.code.appsorganizer.R;

/**
 * @author fabio
 * 
 */
public class ChangeLogDialog extends SimpleDialog {

	private static final String LAST_USED_VERSION = "showChangeLog";

	private static final long serialVersionUID = 6310850433343243241L;

	public ChangeLogDialog(GenericDialogManager dialogManager) {
		super(dialogManager, dialogManager.getString(R.string.Change_log), dialogManager.getString(R.string.Change_log_text));
		setShowNegativeButton(false);
	}

	public boolean showDialogIfVersionChanged() {
		SharedPreferences settings = owner.getSharedPreferences("appsOrganizer_pref", 0);
		int lastUsedVersion = settings.getInt(LAST_USED_VERSION, -1);

		try {
			PackageInfo packageInfo = owner.getPackageManager().getPackageInfo(owner.getPackageName(), 0);
			int currentVersion = packageInfo.versionCode;
			if (lastUsedVersion != currentVersion) {
				showDialog();

				saveVersion(settings, currentVersion);
				return true;
			}
		} catch (NameNotFoundException e) {
		}
		return false;
	}

	public void saveVersion() {
		SharedPreferences settings = owner.getSharedPreferences("appsOrganizer_pref", 0);
		try {
			PackageInfo packageInfo = owner.getPackageManager().getPackageInfo(owner.getPackageName(), 0);
			saveVersion(settings, packageInfo.versionCode);
		} catch (NameNotFoundException e) {
		}
	}

	private void saveVersion(SharedPreferences settings, int currentVersion) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(LAST_USED_VERSION, currentVersion);
		editor.commit();
	}
}
