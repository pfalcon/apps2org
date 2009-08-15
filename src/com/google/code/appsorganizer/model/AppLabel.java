package com.google.code.appsorganizer.model;

import com.google.code.appsorganizer.db.ObjectWithId;

public class AppLabel extends ObjectWithId {

	private String app;
	private Long labelId;

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Long getLabelId() {
		return labelId;
	}

	public void setLabelId(Long labelId) {
		this.labelId = labelId;
	}

}
