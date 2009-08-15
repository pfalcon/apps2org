package com.google.code.appsorganizer;

public class AppLabelBinding {

	private Long appLabelId;
	private Long labelId;
	private String label;
	private boolean checked;

	private boolean originalChecked;

	public AppLabelBinding() {
	}

	public AppLabelBinding(String label, boolean checked) {
		this.label = label;
		this.checked = checked;
	}

	public boolean isModified() {
		return checked != originalChecked;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isOriginalChecked() {
		return originalChecked;
	}

	public void setOriginalChecked(boolean originalChecked) {
		this.originalChecked = originalChecked;
	}

	public Long getAppLabelId() {
		return appLabelId;
	}

	public void setAppLabelId(Long appLabelId) {
		this.appLabelId = appLabelId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AppLabelBinding other = (AppLabelBinding) obj;
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		return true;
	}

	public Long getLabelId() {
		return labelId;
	}

	public void setLabelId(Long labelId) {
		this.labelId = labelId;
	}

}
