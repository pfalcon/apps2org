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
package com.google.code.appsorganizer.livefolder;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.LiveFolders;

import com.google.code.appsorganizer.ApplicationInfoManager;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.AppLabel;

public class AppLabelProvider extends ContentProvider {

	public static final String AUTHORITY = "com.google.code.appsorganizer";

	// To distinguish this URI
	private static final int TYPE_MY_URI = 0;
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "live_folders/*", TYPE_MY_URI);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int bulkInsert(Uri arg0, ContentValues[] values) {
		return 0; // nothing to insert
	}

	// Set of columns needed by a LiveFolder
	// This is the live folder contract
	private static final String[] CURSOR_COLUMNS = new String[] { BaseColumns._ID, LiveFolders.NAME, LiveFolders.ICON_PACKAGE,
			LiveFolders.ICON_RESOURCE,
			// LiveFolders.ICON_BITMAP,
			LiveFolders.INTENT };

	// In case there are no rows
	// use this stand in as an error message
	// Notice it has the same set of columns of a live folder
	private static final String[] CURSOR_ERROR_COLUMNS = new String[] { BaseColumns._ID, LiveFolders.NAME, LiveFolders.DESCRIPTION };

	// The error message row
	private static final Object[] ERROR_MESSAGE_ROW = new Object[] { -1, // id
			"No application found", // name
			"Check your contacts database" // description
	};

	// The error cursor to use
	private static MatrixCursor sErrorCursor = new MatrixCursor(CURSOR_ERROR_COLUMNS);
	static {
		sErrorCursor.addRow(ERROR_MESSAGE_ROW);
	}

	private DatabaseHelper labelDbManager;
	private ApplicationInfoManager applicationInfoManager;

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Figure out the uri and return error if not matching
		int type = URI_MATCHER.match(uri);
		if (type == UriMatcher.NO_MATCH) {
			return sErrorCursor;
		}

		if (labelDbManager == null) {
			labelDbManager = new DatabaseHelper(getContext());
		}
		String p = uri.getPath();
		Long labelId = Long.parseLong(p.substring(p.lastIndexOf('/') + 1));
		try {
			List<AppLabel> apps = labelDbManager.appsLabelDao.getApps(labelId);
			PackageManager pm = getContext().getPackageManager();
			applicationInfoManager = new ApplicationInfoManager(pm);
			return applicationInfoManager.convertToCursor(apps, CURSOR_COLUMNS);
		} catch (Throwable e) {
			return sErrorCursor;
		}
	}

	@Override
	public String getType(Uri uri) {
		// indicates the MIME type for a given URI
		// targeted for this wrapper provider
		// This usually looks like
		// "vnd.android.cursor.dir/vnd.google.note"
		// return People.CONTENT_TYPE;
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		throw new UnsupportedOperationException("no insert as this is just a wrapper");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("no delete as this is just a wrapper");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("no update as this is just a wrapper");
	}

}
