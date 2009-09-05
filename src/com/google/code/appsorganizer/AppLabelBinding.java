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

	public final Long labelId;
	public final String label;
	public boolean checked;

	public final boolean originalChecked;

	public AppLabelBinding(String label, Long labelId, boolean originalChecked) {
		this.label = label;
		this.labelId = labelId;
		this.originalChecked = originalChecked;
	}

	public boolean isModified() {
		return checked != originalChecked;
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
