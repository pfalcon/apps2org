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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.dialogs.ActivityWithDialog;

public class ChooseIconFromPackActivity extends ActivityWithDialog {

	private static final int BUFFER_SIZE = 4096;

	private GridView mGrid;

	private String apkName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		apkName = getIntent().getStringExtra("apkName");
		loadIcons();

		setContentView(R.layout.icon_grid);
		mGrid = (GridView) findViewById(R.id.iconGrid);
		mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent res = new Intent();
				res.putExtra("image", SelectAppDialog.convertToByteArray(((BitmapDrawable) mIcons[pos]).getBitmap()));
				setResult(RESULT_OK, res);
				finish();
			}
		});
	}

	private Drawable[] mIcons;

	private void loadIcons() {
		createProgressDialog(1);
		new Thread() {
			@Override
			public void run() {
				List<Drawable> l = new ArrayList<Drawable>();
				ZipFile z = null;
				try {
					z = new ZipFile(apkName);
					ArrayList<ZipEntry> images = filterImages(z);
					handler.sendEmptyMessage(images.size());
					for (ZipEntry entry : images) {
						Bitmap bitmap = loadBitmap(z, entry);
						if (bitmap != null) {
							l.add(new BitmapDrawable(bitmap));
						}
						handler.sendEmptyMessage(-1);
					}
				} catch (ZipException e1) {
				} catch (IOException e1) {
				} finally {
					if (z != null) {
						try {
							z.close();
						} catch (IOException e) {
						}
					}
				}
				mIcons = l.toArray(new Drawable[l.size()]);
				handler.sendEmptyMessage(-3);
			}
		}.start();
	}

	private ProgressDialog pd;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -3) {
				pd.hide();
				mGrid.setAdapter(new IconsAdapter());
			} else if (msg.what == -1) {
				pd.incrementProgressBy(1);
			} else {
				pd.setMax(msg.what);
			}
		}
	};

	private void createProgressDialog(int size) {
		pd = new ProgressDialog(this);
		pd.setTitle(getText(R.string.loading_icons));
		pd.setMessage(getText(R.string.please_wait_loading));
		pd.setIndeterminate(false);
		pd.setCancelable(false);
		pd.setMax(size);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.show();
	}

	private Bitmap loadBitmap(ZipFile z, ZipEntry entry) {
		BufferedInputStream is = null;
		try {
			is = new BufferedInputStream(z.getInputStream(entry));
			ArrayList<byte[]> bytes = new ArrayList<byte[]>();
			int tot = readBytes(is, bytes);
			if (tot > 0) {
				byte[] imageBytes = createByteArray(bytes, tot);
				return BitmapFactory.decodeByteArray(imageBytes, 0, tot);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public static boolean isAssetImage(String name) {
		name = name.toLowerCase();
		return name.startsWith("assets") && (name.endsWith(".png") || name.endsWith(".jpg"));
	}

	private byte[] createByteArray(ArrayList<byte[]> bytes, int tot) {
		byte[] imageBytes;
		if (tot > BUFFER_SIZE) {
			imageBytes = new byte[tot];
			int i = 0;
			for (byte[] bs : bytes) {
				int start = BUFFER_SIZE * (i++);
				System.arraycopy(bs, 0, imageBytes, start, Math.min(tot - start, BUFFER_SIZE));
			}
		} else {
			imageBytes = bytes.get(0);
		}
		return imageBytes;
	}

	private int readBytes(BufferedInputStream is, ArrayList<byte[]> bytes) throws IOException {
		byte[] tmp = new byte[BUFFER_SIZE];
		int tot = 0;
		int readedBytes = 0;
		while ((readedBytes = is.read(tmp, 0, BUFFER_SIZE)) != -1) {
			bytes.add(tmp);
			tot += readedBytes;
			tmp = new byte[BUFFER_SIZE];
		}
		return tot;
	}

	private ArrayList<ZipEntry> filterImages(ZipFile z) {
		ArrayList<ZipEntry> images = new ArrayList<ZipEntry>();
		Enumeration<? extends ZipEntry> entries = z.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (isAssetImage(entry.getName())) {
				images.add(entry);
			}
		}
		return images;
	}

	public class IconsAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i;

			if (convertView == null) {
				i = new ImageView(ChooseIconFromPackActivity.this);
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				i.setLayoutParams(new GridView.LayoutParams(50, 50));
			} else {
				i = (ImageView) convertView;
			}

			i.setImageDrawable(mIcons[position]);
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
