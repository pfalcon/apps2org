package com.google.code.appsorganizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.db.DbChangeListener;
import com.google.code.appsorganizer.dialogs.GenericDialogManager;

public class AppsListActivity extends ListActivity {

	private DatabaseHelper dbHelper;

	private ArrayList<Application> apps;

	private ChooseLabelDialogCreator chooseLabelDialog;

	private final GenericDialogManager genericDialogManager = new GenericDialogManager();

	private ApplicationInfoManager applicationInfoManager;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				pd.setMessage(getText(R.string.preparing_apps_list));
			} else if (msg.what == -2) {
				final SimpleAdapter appsAdapter = new AppsListAdapter(AppsListActivity.this, convertToMapArray(apps), R.layout.app_row,
						new String[] { "image", "name", "appInfo" }, new int[] { R.id.image, R.id.name, R.id.labels });
				dbHelper.appsLabelDao.addListener(new DbChangeListener() {
					public void notifyDataSetChanged() {
						appsAdapter.notifyDataSetChanged();
					}
				});
				setListAdapter(appsAdapter);
				pd.dismiss();
			} else {
				pd.setMessage(getString(R.string.total_apps) + ": " + msg.what);
			}
		}

	};

	private List<? extends Map<String, ?>> convertToMapArray(ArrayList<Application> apps) {
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		for (Application application : apps) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("image", application.getIcon());
			m.put("name", application.getName());
			m.put("appInfo", application.getPackage());
			l.add(m);
		}
		return l;
	}

	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		applicationInfoManager = new ApplicationInfoManager(getPackageManager());
		dbHelper = new DatabaseHelper(this);
		setContentView(R.layout.main);
		chooseLabelDialog = new ChooseLabelDialogCreator(this, dbHelper);
		genericDialogManager.addDialog(chooseLabelDialog);

		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				chooseLabelDialog.setCurrentApp(apps.get(position).getPackage());
				showDialog(chooseLabelDialog.getDialogId());
			}
		});
		pd = ProgressDialog.show(this, getText(R.string.preparing_apps_list), getText(R.string.please_wait_loading), true, false);
		Thread t = new Thread() {
			@Override
			public void run() {
				fillData();
				handler.sendEmptyMessage(-1);
				handler.sendEmptyMessage(-2);
			}
		};
		t.start();
	}

	public void fillData() {
		apps = applicationInfoManager.getAppsArray(handler);
	}

	public final class AppsListAdapter extends SimpleAdapter {

		public AppsListAdapter(AppsListActivity homeActivity, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(homeActivity, data, resource, from, to);

			setViewBinder(new SimpleAdapter.ViewBinder() {

				public boolean setViewValue(View view, Object data, String textRepresentation) {
					switch (view.getId()) {
					case R.id.image:
						((ImageView) view).setImageDrawable((Drawable) data);
						return true;
					case R.id.labels:
						((TextView) view).setText(dbHelper.labelDao.getLabelsString(data.toString()));
						return true;
					default:
						return false;
					}
				}
			});
		}
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

}