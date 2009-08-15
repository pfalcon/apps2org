package com.google.code.appsorganizer.livefolder;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.DatabaseHelper;
import com.google.code.appsorganizer.model.Label;

public class LabelLiveFolder extends ListActivity {
	public static final Uri CONTENT_URI = Uri.parse("content://it.appmanager/live_folders/");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final String action = intent.getAction();

		DatabaseHelper labelDbManager = new DatabaseHelper(this);

		if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
			final List<Label> labels = labelDbManager.labelDao.getLabels();
			setListAdapter(new ArrayAdapter<Label>(this, android.R.layout.simple_list_item_1, labels));

			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
					CharSequence label = ((TextView) v).getText();
					long labelId = getLabelId(label);
					setResult(RESULT_OK, createLiveFolder(LabelLiveFolder.this, Uri
							.parse("content://it.appmanager/live_folders/" + labelId), label.toString(), R.drawable.address_48));
					finish();
				}

				private long getLabelId(CharSequence label) {
					for (Label l : labels) {
						if (l.getName().equals(label.toString())) {
							return l.getId();
						}
					}
					return -1;
				}
			});
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	private static Intent createLiveFolder(Context context, Uri uri, String name, int icon) {
		final Intent intent = new Intent();
		intent.setData(uri);
		intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, name);
		intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, Intent.ShortcutIconResource.fromContext(context, icon));
		intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE, LiveFolders.DISPLAY_MODE_GRID);
		return intent;
	}
}
