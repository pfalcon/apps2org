/**
 * 
 */
package com.google.code.appsorganizer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ChooseLabelListAdapter extends ArrayAdapter<AppLabelBinding> {

	public ChooseLabelListAdapter(Context context, List<AppLabelBinding> list) {
		super(context, R.layout.label_row, list);
		setNotifyOnChange(true);
	}

	public void addLabel(String l) {
		AppLabelBinding label = new AppLabelBinding();
		label.setLabel(l);
		label.setChecked(true);
		label.setOriginalChecked(false);
		insert(label, 0);
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		if (v == null) {
			LayoutInflater factory = LayoutInflater.from(getContext());
			v = factory.inflate(R.layout.label_row, null);
		}
		final AppLabelBinding item = getItem(position);
		CheckBox labelCheck = (CheckBox) v.findViewById(R.id.labelCheck);
		labelCheck.setText(item.getLabel());
		labelCheck.setOnCheckedChangeListener(null);
		labelCheck.setChecked(item.isChecked());
		labelCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				item.setChecked(isChecked);
			}
		});
		return v;
	}

	public List<AppLabelBinding> getModifiedLabels() {
		List<AppLabelBinding> ret = new ArrayList<AppLabelBinding>();
		int count = getCount();
		for (int i = 0; i < count; i++) {
			AppLabelBinding label = getItem(i);
			if (label.isModified()) {
				ret.add(label);
			}
		}
		return ret;
	}
}