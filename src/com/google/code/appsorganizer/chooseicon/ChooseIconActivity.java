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
package com.google.code.appsorganizer.chooseicon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.dialogs.ActivityWithDialog;
import com.google.code.appsorganizer.dialogs.OnOkClickListener;
import com.google.code.appsorganizer.dialogs.SimpleDialog;
import com.google.code.appsorganizer.dialogs.SingleSelectDialog;
import com.google.code.appsorganizer.model.Label;

public class ChooseIconActivity extends ActivityWithDialog {
	private static final int IMAGE_GALLERY = 10;

	private GridView mGrid;

	private SimpleDialog applicationNotFoundDialog;

	private SimpleDialog selectImageDialog;

	private SingleSelectDialog selectAppDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		applicationNotFoundDialog = createApplicationNotFoundDialog();
		selectImageDialog = new SimpleDialog(getGenericDialogManager(), getString(R.string.select_jpg_bmp_title), getString(R.string.select_jpg_bmp));
		selectImageDialog.setShowNegativeButton(false);

		loadIcons();

		final int group = getIntent().getIntExtra("group", -1);
		final long groupId = getIntent().getLongExtra("groupId", -1);
		setContentView(com.google.code.appsorganizer.R.layout.icon_grid);
		mGrid = (GridView) findViewById(com.google.code.appsorganizer.R.id.iconGrid);
		mGrid.setAdapter(new AppsAdapter());
		mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent res = new Intent();
				res.putExtra("icon", mIcons[pos]);
				res.putExtra("group", group);
				res.putExtra("groupId", groupId);
				setResult(RESULT_OK, res);
				finish();
			}
		});

		selectAppDialog = new SingleSelectDialog(getGenericDialogManager(), getString(R.string.choose_app), getString(R.string.alert_dialog_ok),
				new CharSequence[] { getString(R.string.Android_Image_Gallery), "AndExplorer" }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onOkClick(DialogInterface dialog, int selectedItem) {
				if (selectedItem == 0) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, IMAGE_GALLERY);
				} else {
					try {
						openFileDialog();
					} catch (ActivityNotFoundException e) {
						getGenericDialogManager().showDialog(applicationNotFoundDialog);
					}
				}
			}
		};
		findViewById(R.id.loadButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				selectAppDialog.showDialog();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		int group = getIntent().getIntExtra("group", -1);
		long groupId = getIntent().getLongExtra("groupId", -1);
		if (requestCode == IMAGE_GALLERY) {
			if (resultCode == RESULT_OK) {
				Uri uri = intent.getData();
				if (uri != null) {

					try {
						Bitmap bm = Media.getBitmap(getContentResolver(), uri);
						Bitmap bitmap = getScaledImage(bm);
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG, 100, os);

						Intent res = new Intent();
						res.putExtra("image", os.toByteArray());
						res.putExtra("group", group);
						res.putExtra("groupId", groupId);
						setResult(RESULT_OK, res);
						finish();
					} catch (IOException e) {
						getGenericDialogManager().showDialog(selectImageDialog);
					}
				}
			}
		} else if (resultCode == RESULT_OK) {
			Uri uri = intent.getData();
			String type = intent.getType();
			if (uri != null) {
				String path = uri.toString().toLowerCase();
				if (type == null || (!type.equals("image/jpeg") && !type.equals("image/png"))) {
					getGenericDialogManager().showDialog(selectImageDialog);
				} else if (path.startsWith("file://")) {
					File file = new File(URI.create(path));
					Bitmap bitmap = getScaledImage(file);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.PNG, 100, os);

					Intent res = new Intent();
					res.putExtra("image", os.toByteArray());
					res.putExtra("group", group);
					res.putExtra("groupId", groupId);
					setResult(RESULT_OK, res);
					finish();
				}
			}
		}
	}

	private Bitmap getScaledImage(File file) {
		return getScaledImage(BitmapFactory.decodeFile(file.getAbsolutePath()));
	}

	private Bitmap getScaledImage(Bitmap bitmapOrg) {
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();
		int newWidth = 48;
		int newHeight = 48;

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
	}

	private int[] mIcons;

	private void loadIcons() {
		// mIcons = new int[] { android.R.drawable.ic_menu_add,
		// android.R.drawable.ic_menu_agenda,
		// android.R.drawable.ic_menu_always_landscape_portrait,
		// android.R.drawable.ic_menu_call, android.R.drawable.ic_menu_camera,
		// android.R.drawable.ic_menu_close_clear_cancel,
		// android.R.drawable.ic_menu_compass, android.R.drawable.ic_menu_crop,
		// android.R.drawable.ic_menu_day, android.R.drawable.ic_menu_delete,
		// android.R.drawable.ic_menu_directions,
		// android.R.drawable.ic_menu_edit, android.R.drawable.ic_menu_gallery,
		// android.R.drawable.ic_menu_help,
		// android.R.drawable.ic_menu_info_details,
		// android.R.drawable.ic_menu_manage,
		// android.R.drawable.ic_menu_mapmode,
		// android.R.drawable.ic_menu_month, android.R.drawable.ic_menu_more,
		// android.R.drawable.ic_menu_my_calendar,
		// android.R.drawable.ic_menu_mylocation,
		// android.R.drawable.ic_menu_myplaces,
		// android.R.drawable.ic_menu_preferences,
		// android.R.drawable.ic_menu_recent_history,
		// android.R.drawable.ic_menu_report_image,
		// android.R.drawable.ic_menu_revert,
		// android.R.drawable.ic_menu_rotate, android.R.drawable.ic_menu_save,
		// android.R.drawable.ic_menu_search,
		// android.R.drawable.ic_menu_send, android.R.drawable.ic_menu_set_as,
		// android.R.drawable.ic_menu_share,
		// android.R.drawable.ic_menu_slideshow,
		// android.R.drawable.ic_menu_sort_alphabetically,
		// android.R.drawable.ic_menu_sort_by_size,
		// android.R.drawable.ic_menu_today, android.R.drawable.ic_menu_upload,
		// android.R.drawable.ic_menu_upload_you_tube,
		// android.R.drawable.ic_menu_view, android.R.drawable.ic_menu_week,
		// android.R.drawable.ic_menu_zoom
		// };
		mIcons = Label.getIconsList();
	}

	private void openFileDialog() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		Uri startDir = Uri.fromFile(new File("/sdcard"));
		intent.setDataAndType(startDir, "vnd.android.cursor.dir/lysesoft.andexplorer.file");
		// Title
		intent.putExtra("explorer_title", "Select a file");
		// Optional colors
		intent.putExtra("browser_title_background_color", "440000AA");
		intent.putExtra("browser_title_foreground_color", "FFFFFFFF");
		intent.putExtra("browser_list_background_color", "66000000");

		intent.putExtra("browser_filter_extension_whitelist", "*.jpg,*.jpeg,*.png");
		// Optional font scale
		intent.putExtra("browser_list_fontscale", "140%");
		// Optional 0=simple list, 1 = list with filename and size, 2 =
		// list with filename, size and date.
		intent.putExtra("browser_list_layout", "2");
		startActivityForResult(intent, 0);
	}

	private SimpleDialog createApplicationNotFoundDialog() {
		String title = getString(R.string.Application_not_found);
		SimpleDialog applicationNotFoundDialog = new SimpleDialog(getGenericDialogManager(), title,
				getString(R.string.Application_not_found_message), new OnOkClickListener() {
					private static final long serialVersionUID = 1L;

					public void onClick(CharSequence charSequence, DialogInterface dialog, int which) {
						Intent emailIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:lysesoft.andexplorer"));
						startActivity(emailIntent);
					}
				});

		applicationNotFoundDialog.setOkMessageText(getString(R.string.Open_market));
		applicationNotFoundDialog.setShowNegativeButton(true);
		return applicationNotFoundDialog;
	}

	public class AppsAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i;

			if (convertView == null) {
				i = new ImageView(ChooseIconActivity.this);
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				i.setLayoutParams(new GridView.LayoutParams(50, 50));
			} else {
				i = (ImageView) convertView;
			}

			i.setImageResource(mIcons[position]);
			return i;
		}

		public final int getCount() {
			return mIcons.length;
		}

		public final Object getItem(int position) {
			return mIcons[position];
		}

		public final long getItemId(int position) {
			return position;
		}
	}

}
