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
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.code.appsorganizer.dialogs.SimpleDialog;
import com.google.code.appsorganizer.model.Label;

public class ChooseIconActivity extends ActivityWithDialog {
	private GridView mGrid;

	private SimpleDialog selectImageDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		selectImageDialog = new SimpleDialog(getGenericDialogManager(), getString(R.string.select_jpg_bmp_title),
				getString(R.string.select_jpg_bmp));
		selectImageDialog.setShowNegativeButton(false);

		loadIcons();

		final int group = getIntent().getIntExtra("group", -1);
		setContentView(com.google.code.appsorganizer.R.layout.icon_grid);
		mGrid = (GridView) findViewById(com.google.code.appsorganizer.R.id.iconGrid);
		mGrid.setAdapter(new AppsAdapter());
		mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent res = new Intent();
				res.putExtra("icon", mIcons[pos]);
				res.putExtra("group", group);
				setResult(RESULT_OK, res);
				finish();
			}
		});

		findViewById(R.id.loadButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, 0);
				// try {
				// openFileDialog();
				// } catch (ActivityNotFoundException e) {
				// getGenericDialogManager().showDialog(applicationNotFoundDialog);
				// }
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			Uri uri = intent.getData();
			if (uri != null) {
				final int group = getIntent().getIntExtra("group", -1);
				// String type = intent.getType();
				// String path = uri.toString().toLowerCase();
				// genericDialogManager.showSimpleDialog(path + " " + type,
				// false, null);
				// if (type == null || (!type.equals("image/jpeg") &&
				// !type.equals("image/png"))) {
				// getGenericDialogManager().showDialog(selectImageDialog);
				// } else if (path.startsWith("file://")) {
				// File file = new File(URI.create(path));
				// Bitmap bitmap = getScaledImage(file);

				try {
					Bitmap bm = Media.getBitmap(getContentResolver(), uri);
					Bitmap bitmap = getScaledImage(bm);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.PNG, 100, os);

					Intent res = new Intent();
					res.putExtra("image", os.toByteArray());
					res.putExtra("group", group);
					setResult(RESULT_OK, res);
					finish();
				} catch (IOException e) {
					getGenericDialogManager().showDialog(selectImageDialog);
				}
				// }
			}
		}
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
