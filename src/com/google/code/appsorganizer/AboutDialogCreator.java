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
package com.google.code.appsorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.code.appsorganizer.dialogs.GenericDialogCreator;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;

/**
 * @author fabio
 * 
 */
public class AboutDialogCreator extends GenericDialogCreator {

	public AboutDialogCreator(GenericDialogManager dialogManager) {
		super(dialogManager);
	}

	@Override
	public Dialog createDialog() {
		LayoutInflater factory = LayoutInflater.from(owner);
		View body = factory.inflate(R.layout.about, null);
		TextView authorText = (TextView) body.findViewById(R.id.author);
		authorText.setText(authorText.getText() + " Fabio Collini");
		AlertDialog.Builder builder = new AlertDialog.Builder(owner);
		builder = builder.setIcon(R.drawable.icon);
		String versionName = getVersionName(owner);
		if (versionName != null) {
			int indexOf = versionName.indexOf(':');
			if (indexOf != -1) {
				versionName = versionName.substring(0, indexOf);
			}
			builder = builder.setTitle(owner.getString(R.string.app_name) + " " + versionName);
		} else {
			builder = builder.setTitle(R.string.app_name);
		}
		builder = builder.setView(body);
		builder = builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		return builder.create();
	}

	public static String getVersionName(Context owner) {
		try {
			PackageInfo packageInfo = owner.getPackageManager().getPackageInfo(owner.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		return null;
	}

}
