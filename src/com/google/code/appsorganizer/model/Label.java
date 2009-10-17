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
package com.google.code.appsorganizer.model;

import gnu.trove.TIntIntHashMap;

import com.google.code.appsorganizer.R;
import com.google.code.appsorganizer.db.ObjectWithId;

/**
 * @author fabio
 * 
 */
public class Label extends ObjectWithId implements Comparable<Label> {

	private String name;
	private int iconDb;
	private byte[] imageBytes;

	/**
	 * Map that associate a database id to drawable id (if a drawable is added
	 * the id can change)
	 */
	public static TIntIntHashMap iconsMap;
	private static TIntIntHashMap iconsMapInv;
	private static int[] iconsList;

	public static int convertToIconDb(int icon) {
		initMaps();
		return iconsMapInv.get(icon);
	}

	public static int convertToIcon(int icon) {
		initMaps();
		return iconsMap.get(icon);
	}

	private static void initMaps() {
		if (iconsMap == null) {

			iconsMap = new TIntIntHashMap(65);
			int i = 2130837504;
			iconsMap.put(i++, R.drawable.agt_member);
			iconsMap.put(i++, R.drawable.binary);
			iconsMap.put(i++, R.drawable.cam_unmount);
			iconsMap.put(i++, R.drawable.camera_unmount);
			iconsMap.put(i++, R.drawable.cardgame);
			iconsMap.put(i++, R.drawable.cdimage);
			iconsMap.put(i++, R.drawable.chardevice);
			iconsMap.put(i++, R.drawable.doc);
			iconsMap.put(i++, R.drawable.energy);
			iconsMap.put(i++, R.drawable.favorites);
			iconsMap.put(i++, R.drawable.file_temporary);
			iconsMap.put(i++, R.drawable.floppy_unmount);
			iconsMap.put(i++, R.drawable.globe);
			iconsMap.put(i++, R.drawable.hardware);
			iconsMap.put(i++, R.drawable.icon);
			iconsMap.put(i++, R.drawable.icon_default);
			iconsMap.put(i++, R.drawable.image2);
			iconsMap.put(i++, R.drawable.images);
			iconsMap.put(i++, R.drawable.internet_connection_tools);
			iconsMap.put(i++, R.drawable.joystick);
			iconsMap.put(i++, R.drawable.kbackgammon);
			iconsMap.put(i++, R.drawable.keyboard);
			iconsMap.put(i++, R.drawable.kfm_home);
			iconsMap.put(i++, R.drawable.klaptopdaemon);
			iconsMap.put(i++, R.drawable.knode);
			iconsMap.put(i++, R.drawable.kontact);
			iconsMap.put(i++, R.drawable.kopete);
			iconsMap.put(i++, R.drawable.kpaint);
			iconsMap.put(i++, R.drawable.kpat);
			iconsMap.put(i++, R.drawable.kspread_ksp);
			iconsMap.put(i++, R.drawable.kstars);
			iconsMap.put(i++, R.drawable.kweather);
			iconsMap.put(i++, R.drawable.messenger);
			iconsMap.put(i++, R.drawable.midi);
			iconsMap.put(i++, R.drawable.mp3);
			iconsMap.put(i++, R.drawable.mp3player_alt_unmount);
			iconsMap.put(i++, R.drawable.multimedia);
			iconsMap.put(i++, R.drawable.multimedia2);
			iconsMap.put(i++, R.drawable.news);
			iconsMap.put(i++, R.drawable.package_favorite);
			iconsMap.put(i++, R.drawable.package_games_board);
			iconsMap.put(i++, R.drawable.package_games_strategy);
			iconsMap.put(i++, R.drawable.pda_black);
			iconsMap.put(i++, R.drawable.schedule);
			iconsMap.put(i++, R.drawable.service_manager);
			iconsMap.put(i++, R.drawable.sms);
			iconsMap.put(i++, R.drawable.video);

			iconsMap.put(i++, R.drawable.graphic_design);
			iconsMap.put(i++, R.drawable.iconthemes);
			iconsMap.put(i++, R.drawable.java_jar);
			iconsMap.put(i++, R.drawable.kaboodle);
			iconsMap.put(i++, R.drawable.katomic);
			iconsMap.put(i++, R.drawable.kmail);
			iconsMap.put(i++, R.drawable.mail_generic);
			iconsMap.put(i++, R.drawable.mail_new);
			iconsMap.put(i++, R.drawable.mail);
			iconsMap.put(i++, R.drawable.package_games_arcade);
			iconsMap.put(i++, R.drawable.thumbnail);
			iconsMap.put(i++, R.drawable.wifi);

			iconsMap.put(i++, R.drawable.package_games_kids);
			iconsMap.put(i++, R.drawable.ksmiletris);
			iconsMap.put(i++, R.drawable.kchart);
			iconsMap.put(i++, R.drawable.kwallet);
			iconsMap.put(i++, R.drawable.demo);
			iconsMap.put(i++, R.drawable.blockdevice);

			iconsMapInv = new TIntIntHashMap(iconsMap.size());
			iconsList = new int[iconsMap.size()];
			int[] keys = iconsMap.keys();
			for (int j = 0; j < keys.length; j++) {
				int k = keys[j];
				int v = iconsMap.get(k);
				iconsMapInv.put(v, k);
				iconsList[j] = v;
			}
		}
	}

	public static int[] getIconsList() {
		initMaps();
		return iconsList;
	}

	public Label() {
	}

	public Label(long id, String name) {
		setId(id);
		this.name = name;
	}

	public Label(long id, String name, int icon) {
		setId(id);
		this.name = name;
		setIcon(icon);
	}

	public Label(String name) {
		this.name = name;
	}

	public Label(String name, Integer iconDb) {
		this.name = name;
		this.iconDb = iconDb;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public int compareTo(Label o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Label other = (Label) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public int getIcon() {
		if (iconDb == 0) {
			return R.drawable.icon_default;
		}
		initMaps();
		return iconsMap.get(iconDb);
	}

	public void setIcon(int icon) {
		this.iconDb = convertToIconDb(icon);
	}

	public int getIconDb() {
		return iconDb;
	}

	public void setIconDb(int icon) {
		this.iconDb = icon;
	}

	public String getLabel() {
		return name;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}
}
