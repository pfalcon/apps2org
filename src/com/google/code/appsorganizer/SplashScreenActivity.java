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

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;
import com.google.code.appsorganizer.dialogs.GenericDialogManagerActivity;
import com.google.code.appsorganizer.model.AppLabelSaver;
import com.google.code.appsorganizer.model.Application;

public class SplashScreenActivity extends ListActivity implements DbChangeListener, GenericDialogManagerActivity {

	private DatabaseHelper dbHelper;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private GenericDialogManager genericDialogManager;

	private ApplicationInfoManager applicationInfoManager;

	private ToggleButton labelButton;

	private ToggleButton appButton;

	private OptionMenuManager optionMenuManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Debug.startMethodTracing("splash");
		genericDialogManager = new GenericDialogManager(SplashScreenActivity.this);

		applicationInfoManager = ApplicationInfoManager.singleton(getPackageManager());
		dbHelper = DatabaseHelper.initOrSingleton(SplashScreenActivity.this);
		chooseLabelDialog = new ChooseLabelDialogCreator(dbHelper, applicationInfoManager);
		optionMenuManager = new OptionMenuManager(SplashScreenActivity.this, dbHelper);
		genericDialogManager.addDialog(chooseLabelDialog);

		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				chooseLabelDialog.setCurrentApp(((Application) getListAdapter().getItem(position)));
				showDialog(chooseLabelDialog.getDialogId());
			}
		});
		getListView().setClickable(true);

		labelButton = (ToggleButton) findViewById(R.id.labelButton);
		appButton = (ToggleButton) findViewById(R.id.appButton);
		labelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(SplashScreenActivity.this, LabelListActivity.class));
			}
		});

		setProgressBarIndeterminateVisibility(true);
		reload();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (labelButton.isChecked()) {
			labelButton.setChecked(false);
		}
		if (!appButton.isChecked()) {
			appButton.setChecked(true);
		}
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				pd.setMessage(getText(R.string.preparing_apps_list));
			} else if (msg.what == -2) {
				setListAdapter(appsAdapter);
			} else if (msg.what == -3) {
				pd.hide();
			} else {
				pd.setMessage(getString(R.string.total_apps) + ": " + msg.what);
			}
		}
	};

	private ProgressDialog pd;

	private ArrayAdapter<Application> appsAdapter;

	public void reload() {
		pd = ProgressDialog.show(this, getText(R.string.preparing_apps_list), getText(R.string.please_wait_loading), true, false);
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}

				applicationInfoManager.reloadAll(dbHelper.appCacheDao, dbHelper.labelDao, handler);
				handler.sendEmptyMessage(-1);

				final Application[] appsArray = applicationInfoManager.getAppsArray();
				appsAdapter = new ArrayAdapter<Application>(SplashScreenActivity.this, R.layout.app_row, appsArray) {
					@Override
					public View getView(int position, View v, ViewGroup parent) {
						return getAppView(SplashScreenActivity.this, dbHelper, applicationInfoManager, v, getItem(position),
								chooseLabelDialog);
					}

				};
				appsAdapter.setNotifyOnChange(false);
				handler.sendEmptyMessage(-2);

				registerForContextMenu(getListView());
				handler.sendEmptyMessage(-3);
				loadIcons(appsArray);
				applicationInfoManager.addListener(SplashScreenActivity.this);
			}
		};
		t.start();
	}

	static class ViewHolder {
		ImageView image;
		TextView labels;
		TextView name;
		CheckBox starred;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		applicationInfoManager.removeListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Application app = (Application) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.singleton().createMenu(menu, app);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Application app = (Application) getListAdapter().getItem(info.position);
		ApplicationContextMenuManager.singleton().onContextItemSelected(item, app, this, chooseLabelDialog, applicationInfoManager);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ApplicationContextMenuManager.singleton().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		genericDialogManager.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return genericDialogManager.onCreateDialog(id);
	}

	public ChooseLabelDialogCreator getChooseLabelDialog() {
		return chooseLabelDialog;
	}

	private final Handler listHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			} else if (msg.what == -2) {
				setProgressBarIndeterminateVisibility(false);
			}
		}
	};

	public void dataSetChanged(Object source, short type) {
		if (type == CHANGED_STARRED) {
			if (source != this) {
				appsAdapter.notifyDataSetChanged();
			}
		} else {
			appsAdapter.setNotifyOnChange(false);
			appsAdapter.clear();
			Application[] appsArray = applicationInfoManager.getAppsArray();
			for (int i = 0; i < appsArray.length; i++) {
				appsAdapter.add(appsArray[i]);
			}
			appsAdapter.notifyDataSetChanged();
			loadInconsInThread(appsArray);
		}
	}

	private void loadInconsInThread(final Application[] appsArray) {
		Thread t = new Thread() {
			@Override
			public void run() {
				loadIcons(appsArray);
			}
		};
		t.start();
	}

	private void loadIcons(final Application[] appsArray) {
		int pos = 0;
		for (Application ab : appsArray) {
			if (ab.getIcon() == null) {
				ab.loadIcon(getPackageManager());
				ab.setIcon(ab.getIcon());
				pos++;
			}
			if (pos == 50) {
				listHandler.sendEmptyMessage(-1);
				pos = 0;
			}
		}
		if (pos > 0) {
			listHandler.sendEmptyMessage(-1);
		}
		listHandler.sendEmptyMessage(-2);
		// Debug.stopMethodTracing();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return optionMenuManager.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionMenuManager.onOptionsItemSelected(item);
	}

	public GenericDialogManager getGenericDialogManager() {
		return genericDialogManager;
	}

	public static View getAppView(final Activity context, final DatabaseHelper dbHelper,
			final ApplicationInfoManager applicationInfoManager, View v, final Application a,
			final ChooseLabelDialogCreator chooseLabelDialog) {
		ViewHolder viewHolder;
		if (v == null) {
			LayoutInflater factory = LayoutInflater.from(context);
			v = factory.inflate(R.layout.app_row, null);
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView) v.findViewById(R.id.image);
			viewHolder.labels = (TextView) v.findViewById(R.id.labels);
			viewHolder.name = (TextView) v.findViewById(R.id.name);
			viewHolder.starred = (CheckBox) v.findViewById(R.id.starCheck);
			v.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}
		OnClickListener onClickListener = new OnClickListener() {
			public void onClick(View v) {
				chooseLabelDialog.setCurrentApp(a);
				context.showDialog(chooseLabelDialog.getDialogId());
			}
		};
		viewHolder.labels.setOnClickListener(onClickListener);
		viewHolder.image.setOnClickListener(onClickListener);
		viewHolder.name.setOnClickListener(onClickListener);

		OnLongClickListener onLongClickListener = new OnLongClickListener() {
			public boolean onLongClick(View v) {
				return false;
			}
		};
		viewHolder.labels.setOnLongClickListener(onLongClickListener);
		viewHolder.image.setOnLongClickListener(onLongClickListener);
		viewHolder.name.setOnLongClickListener(onLongClickListener);

		viewHolder.image.setImageDrawable(a.getIcon());
		viewHolder.labels.setText(a.getLabelListString());
		viewHolder.name.setText(a.getLabel());
		viewHolder.starred.setOnCheckedChangeListener(null);
		viewHolder.starred.setChecked(a.isStarred());
		viewHolder.starred.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Debug.startMethodTracing("splash");
				a.setStarred(isChecked);
				AppLabelSaver.saveStarred(dbHelper, applicationInfoManager, a, isChecked, context);
				// Debug.stopMethodTracing();
			}
		});
		return v;
	}
}