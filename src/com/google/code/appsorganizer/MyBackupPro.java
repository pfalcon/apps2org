//
// Copyright (C) 2009 RerWare, LLC
// This code is the property of RerWare, LLC. You are not allowed to change or use this code 
// outside of the described usage below.
// v2.2.1

// This is a sample Content Provider for applications that want to provide an interface
// to MyBackup Pro application

// Instructions:
// - Add this file to your project
// - Change the package name to your package name
// - Change the CONTENT_AUTHORITY to match your URI  ex: aTrackDog.MyBackupPro
//   this must be named YourPackageName.MyBackupPro,  
//   make sure the CONTENT_AUTHORITY ends with MyBackupPro
// - Add your files that need to be backed up to the filedirpath array below 
// - Only change where it says "CHANGE_THIS" , nothing else
// - Add the following tag:
// <provider android:name="YourPackageName.MyBackupPro"  
// 		android:authorities="YourPackageName.MyBackupPro"/> 
// 
// to your manifest file under the <application> tag. Make sure you change 
// android:name and android:authorities to be YourPackageName.MyBackupPro
// 
// If you have other content provider(s), this will not affect it, because you can have
// one or more content providers

// Once you finish and compile your application, use MyBackup Pro to test it:
// Backup=>SDCard=>Data you should see your application at the bottom of the list
// backup your files, change something and try to restore them
// Thanks for your cooperation
// if you have any questions please email support@rerware.com
//

package com.google.code.appsorganizer;

//**********       Expand the help above *********************

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class MyBackupPro extends ContentProvider {

	// CHANGE_THIS: This is the URI name for MyBackup, this has to be
	// YourPackageName.MyBackupPro
	// Make sure the CONTENT_AUTHORITY ends with MyBackupPro
	// Make sure you put the <provider> tag in the AndroidManifest.xml file as
	// described above
	private final String CONTENT_AUTHORITY = "com.google.code.appsorganizer.MyBackupPro";

	// CHANGE_THIS: This is a list of files and their paths that you want to
	// backup up
	// this could be an SQLite DB, an xml preference file in the shared_prefs
	// directory or any other data file
	// if you want to backup up a whole directory, look at the third example
	// The directory could have sub-directories and files underneath
	// Make sure the directory name ends with /
	// you can have a mix and match of any of these types and as many files as
	// you want
	private String[] filedirpath = new String[] { "/data/data/com.google.code.appsorganizer/databases/data",
			"/data/data/com.google.code.appsorganizer/shared_prefs/appsOrganizer_pref.xml" };

	// CHANGE_THIS: This is the minimum supported version code that your program
	// supports;
	// If you are not sure leave this -1
	// This flag is used to prevent crashes with older backup files
	// ex: if user backups v1.0.0 of your App, then
	// you update the App to v1.0.1 and your DB doesn't change, then
	// you can leave the MinSupportedVersion to v1.0.0 or integer equivalent(ex
	// 1)
	// lets say now you update to v2.0.0 , and you do database changes that are
	// not compatible with older files
	// now you change MinSupportedVersion to v2.0.0 or integer equivalent(ex 2),
	// this will prevent MyBackup from restoring older backups
	private int MinSupportedVersion = -1;

	// Use this method if you want to do some extra work before the backup is
	// done (most of the times this is not needed)
	private void backupStarted() {

	}

	// Use this method if you want to do some extra work after the backup is
	// done (most of the times this is not needed)
	private void backupDone() {

	}

	// Use this method if you want to do some extra work before the restore is
	// done (most of the times this is not needed)
	private void restoreStarted() {

	}

	// Use this method if you want to do some extra work after the restore is
	// done (most of the times this is not needed)
	private void restoreDone() {

	}

	/************************* Don't change anything else in this file ***********************************/

	private final String MybackupContentPro = "content://com.rerware.android.MyBackupPro";
	private final String MybackupContentTrial = "content://com.rerware.android.MyBackup";

	private final int MyBackupQuery = 1000;
	private final int MyBackupQueryVersion = 1001;
	private final int MyBackupQueryInflate = 1002;
	private final int MyBackupBackupStarted = 1003;
	private final int MyBackupBackupDone = 1004;
	private final int MyBackupRestoreStarted = 1005;
	private final int MyBackupRestoreDone = 1006;

	private UriMatcher sURIMatcher;
	private String tempFile = "";
	private List<fileInfo> listUris;
	private fileInfo fi;

	public MyBackupPro() {

		listUris = new ArrayList<fileInfo>();

		for (int i = 0; i < filedirpath.length; i++) {
			fi = new fileInfo();
			fi.filepath = filedirpath[i];
			listUris.add(fi);
		}

		sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		tempFile = "/sdcard/MyBackupTemp.zip";
		sURIMatcher.addURI(CONTENT_AUTHORITY, "MyBackupQuery", MyBackupQuery);
		sURIMatcher.addURI(CONTENT_AUTHORITY, "MyBackupQueryVersion/#", MyBackupQueryVersion);
		sURIMatcher.addURI(CONTENT_AUTHORITY, "MyBackupQueryInflate/#", MyBackupQueryInflate);
		sURIMatcher.addURI(CONTENT_AUTHORITY, "MyBackupBackupStarted", MyBackupBackupStarted);
		sURIMatcher.addURI(CONTENT_AUTHORITY, "MyBackupBackupDone", MyBackupBackupDone);
		sURIMatcher.addURI(CONTENT_AUTHORITY, "MyBackupRestoreStarted", MyBackupRestoreStarted);
		sURIMatcher.addURI(CONTENT_AUTHORITY, "MyBackupRestoreDone", MyBackupRestoreDone);

		int intUnique = -1;
		for (int i = 0; i < listUris.size(); i++) {
			++intUnique;
			sURIMatcher.addURI(CONTENT_AUTHORITY, "getfile" + i + "/#", intUnique);
			++intUnique;
			sURIMatcher.addURI(CONTENT_AUTHORITY, "putfile" + i + "/#", intUnique);
		}

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// TODO Auto-generated method stub
		int match = sURIMatcher.match(uri);
		switch (match) {
		case MyBackupQuery:
			Object[] rowObject;
			String[] Uris = new String[1];
			Uris[0] = "URI_NAME";
			MatrixCursor mc = new MatrixCursor(Uris);

			for (int i = 0; i < listUris.size(); i++) {
				rowObject = new Object[1];
				rowObject[0] = "file" + i;
				mc.addRow(rowObject);
			}

			return mc;
		case MyBackupQueryVersion:
			int BackupVersionCode = Integer.parseInt(uri.getPathSegments().get(1));
			int intOk;
			// if version is compatible
			if (BackupVersionCode >= MinSupportedVersion) {
				intOk = 1;
			} else {
				intOk = 0;
			}
			Object[] rowObject2;
			String[] Uris2 = new String[1];
			Uris2[0] = "VERSION_COMPATIBLE";
			MatrixCursor mc2 = new MatrixCursor(Uris2);

			rowObject2 = new Object[1];
			rowObject2[0] = intOk;
			mc2.addRow(rowObject2);

			return mc2;
		case MyBackupQueryInflate:
			int inflateFile = Integer.parseInt(uri.getPathSegments().get(1));
			fi = listUris.get(inflateFile);
			if (fi.filepath.endsWith("/")) {

				utilities.deleteEverythingInDir(fi.filepath);
				File dir = new File(fi.filepath);

				if (!dir.exists()) {
					dir.mkdirs();
				}
				utilities.Unzip(tempFile, fi.filepath);

				File temp = new File(tempFile);
				temp.delete();

			}
			return null;
		case MyBackupBackupStarted:
			try {
				backupStarted();
			} catch (Exception ex) {

			}
			return null;
		case MyBackupBackupDone:
			try {
				backupDone();
			} catch (Exception ex) {

			}
			return null;
		case MyBackupRestoreStarted:
			try {
				restoreStarted();
			} catch (Exception ex) {

			}
			return null;
		case MyBackupRestoreDone:
			try {
				restoreDone();
			} catch (Exception ex) {

			}
			return null;
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {

		ParcelFileDescriptor parcel = null;
		int match = sURIMatcher.match(uri);

		int authCode = Integer.parseInt(uri.getPathSegments().get(1));
		if (auth(MybackupContentPro, authCode) == 1 || auth(MybackupContentTrial, authCode) == 1) {

			// GET FILE
			if (match % 2 == 0) {

				String getfile = "";
				fi = listUris.get((int) (Math.floor(match / 2)));
				if (fi.filepath.endsWith("/")) {

					getfile = tempFile;
				} else {

					getfile = fi.filepath;
				}

				if (fi.filepath.endsWith("/")) {
					try {
						// zip the directory into the one file before the backup
						ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile));

						utilities.zipDir(fi.filepath, fi.filepath, zos);
						// close the stream
						zos.close();

					} catch (Exception ex) {
						System.out.print(ex.getMessage());
					}

				}

				File getFile = new File(getfile);
				parcel = ParcelFileDescriptor.open(getFile, ParcelFileDescriptor.MODE_READ_WRITE);

				if (fi.filepath.endsWith("/")) {
					File temp = new File(tempFile);
					temp.delete();
				}

			}

			// PUT FILE
			else {
				String putfile = "";
				fi = listUris.get((int) (Math.floor(match / 2)));
				if (fi.filepath.endsWith("/")) {

					putfile = tempFile;
				} else {

					putfile = fi.filepath;
				}
				File putFile = new File(putfile);
				putFile.delete();

				File parentputFile = new File(putFile.getParent());

				if (!parentputFile.exists()) {
					parentputFile.mkdirs();
				}

				if (!putFile.exists()) {
					try {
						putFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				parcel = ParcelFileDescriptor.open(putFile, ParcelFileDescriptor.MODE_READ_WRITE);

			}

		}

		return parcel;
	}

	private int auth(String content, int authcode) {

		int intRet = 0;
		Cursor uriCur;
		try {

			uriCur = this.getContext().getContentResolver().query(Uri.parse(content + "/MyBackupAuth/" + authcode), null, null, null, null);

			if (uriCur != null) {

				if (uriCur.moveToFirst()) {
					do {

						intRet = uriCur.getInt(0);

					} while (uriCur.moveToNext());

				}

				uriCur.close();

			}

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return intRet;
	}

	public class fileInfo {
		public String filepath;
	}

	private static class utilities {
		// here is the code for the method
		private static void zipDir(String dir2zip, String originalDir, ZipOutputStream zos) {
			try {
				// create a new File object based on the directory we have to
				// zip File
				File zipDir = new File(dir2zip);
				// get a listing of the directory content
				String[] dirList = zipDir.list();
				byte[] readBuffer = new byte[2156];
				int bytesIn = 0;
				// loop through dirList, and zip the files
				for (int i = 0; i < dirList.length; i++) {
					File f = new File(zipDir, dirList[i]);
					if (f.isDirectory()) {
						// if the File object is a directory, call this
						// function again to add its content recursively
						String filePath = f.getPath();
						zipDir(filePath, originalDir, zos);
						// loop again
						continue;
					}

					// if we reached here, the File object f was not a directory
					// create a FileInputStream on top of f
					FileInputStream fis = new FileInputStream(f);
					// create a new zip entry
					ZipEntry anEntry = new ZipEntry(f.getPath().substring(originalDir.length()));
					// place the zip entry in the ZipOutputStream object
					zos.putNextEntry(anEntry);
					// now write the content of the file to the ZipOutputStream
					while ((bytesIn = fis.read(readBuffer)) != -1) {
						zos.write(readBuffer, 0, bytesIn);
					}
					// close the Stream
					fis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		private static void Unzip(String zipFile, String targetDir) {
			int BUFFER = 2048;
			String strEntry;

			try {
				BufferedOutputStream dest = null;
				FileInputStream fis = new FileInputStream(zipFile);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
				ZipEntry entry;

				while ((entry = zis.getNextEntry()) != null) {

					try {
						System.out.println("Extracting: " + entry);
						int count;
						byte data[] = new byte[BUFFER];
						// write the files to the disk

						strEntry = entry.getName();

						File entryFile = new File(targetDir + strEntry);
						File entryDir = new File(entryFile.getParent());
						if (!entryDir.exists()) {
							entryDir.mkdirs();
						}

						FileOutputStream fos = new FileOutputStream(entryFile);
						dest = new BufferedOutputStream(fos, BUFFER);
						while ((count = zis.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
						dest.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				zis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private static boolean deleteEverythingInDir(String strdir) {
			File dir = null;
			try {
				dir = new File(strdir);
				if (dir.isDirectory()) {
					String[] children = dir.list();
					for (int i = 0; i < children.length; i++) {
						boolean success = deleteEverythingInDir(dir.getPath() + "/" + children[i]);
						if (!success) {
							return false;
						}
					}
				}
			} catch (Exception ex) {

			}
			// The directory is now empty so delete it
			return dir.delete();
		}

	}
}
