package com.google.code.appsorganizer;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(getText(R.string.tab_apps)).setContent(
				new Intent(this, AppsListActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(getText(R.string.tab_labels)).setContent(
				new Intent(this, LabelListActivity.class)));
	}
}
