package com.google.code.appsorganizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.code.appsorganizer.db.DatabaseHelper;

/**
 * @author fabio
 * 
 */
public class MountReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("AppsOrganizerMountReceiver", intent.getAction());
		try {
			// aspetto un secondo, potrebbero non essere ancora disponibili le
			// app su sd
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		DatabaseHelper dbHelper = DatabaseHelper.initOrSingleton(context);
		ApplicationInfoManager.reloadAll(context.getPackageManager(), dbHelper, null, false, null);
		// rimetto first time in modo da far ricaricare la lista delle
		// applicazioni
		SplashScreenActivity.firstTime = true;
	}
}