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

public class AppLabelBinding implements Comparable<AppLabelBinding> {

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

	@Override
	public String toString() {
		return label;
	}

	public int compareTo(AppLabelBinding another) {
		if (checked != another.checked) {
			return checked ? -1 : 1;
		}
		return label.compareToIgnoreCase(another.label);
	}
}
