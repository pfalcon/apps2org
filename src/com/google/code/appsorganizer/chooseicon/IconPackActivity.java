package com.google.code.appsorganizer.chooseicon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.dialogs.ListActivityWithDialog;
import com.google.code.appsorganizer.utils.ArrayAdapterSmallRow;

public class IconPackActivity extends ListActivityWithDialog {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		retrieveList();
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent intent = new Intent(IconPackActivity.this, ChooseIconFromPackActivity.class);
				AppBinding item = (AppBinding) getListAdapter().getItem(pos);
				intent.putExtra("apkName", item.apkName);
				startActivityForResult(intent, 1);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Intent res = new Intent();
			res.putExtra("image", data.getByteArrayExtra("image"));
			setResult(RESULT_OK, res);
			finish();
		}
	}

	private ProgressDialog pd;

	private final List<AppBinding> iconPacks = new ArrayList<AppBinding>();

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -3) {
				pd.hide();
				setListAdapter(new ArrayAdapterSmallRow<AppBinding>(IconPackActivity.this, android.R.layout.simple_list_item_1, iconPacks));
			} else {
				pd.incrementProgressBy(1);
			}
		}
	};

	private void retrieveList() {
		final List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
		createProgressDialog(installedPackages.size());
		Thread t = new Thread() {
			@Override
			public void run() {
				String abcPackageName = getPackageName();
				for (PackageInfo p : installedPackages) {
					String packageName = p.packageName;
					if (!packageName.startsWith("com.android") && !abcPackageName.equals(packageName) && p.applicationInfo.enabled) {
						String dir = p.applicationInfo.publicSourceDir;
						ZipFile z = null;
						try {
							z = new ZipFile(dir);
							Enumeration<? extends ZipEntry> entries = z.entries();
							while (entries.hasMoreElements()) {
								ZipEntry zipEntry = entries.nextElement();
								if (ChooseIconFromPackActivity.isAssetImage(zipEntry.getName())) {
									iconPacks.add(new AppBinding(dir, p.applicationInfo.loadLabel(getPackageManager())));
									break;
								}
							}
						} catch (Throwable e) {
						} finally {
							if (z != null) {
								try {
									z.close();
								} catch (IOException e) {
								}
							}
						}
					}
					handler.sendEmptyMessage(-1);
				}
				Collections.sort(iconPacks);
				handler.sendEmptyMessage(-3);
			}
		};
		t.start();
	}

	private void createProgressDialog(int size) {
		pd = new ProgressDialog(this);
		pd.setTitle(getText(R.string.looking_for_icons));
		pd.setMessage(getText(R.string.please_wait_loading));
		pd.setIndeterminate(false);
		pd.setCancelable(false);
		pd.setMax(size);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.show();
	}

	private static class AppBinding implements Comparable<AppBinding> {
		String apkName;
		CharSequence name;

		public AppBinding(String apkName, CharSequence name) {
			super();
			this.apkName = apkName;
			this.name = name;
		}

		@Override
		public String toString() {
			return name.toString();
		}

		public int compareTo(AppBinding another) {
			return name.toString().compareToIgnoreCase(another.name.toString());
		}

	}

}
