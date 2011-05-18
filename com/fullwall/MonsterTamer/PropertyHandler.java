package com.fullwall.MonsterTamer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * iConomy v1.x
 * Copyright (C) 2010  Nijikokun <nijikokun@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * PropertyHandler
 * 
 * Reads & Writes properties files.
 * 
 * @author Nijiko
 */
public final class PropertyHandler {

	private static final Logger log = Logger.getLogger("Minecraft");
	private Properties properties;
	private String fileName;
	private boolean isNew = false;

	public PropertyHandler(String fileName) {
		this.fileName = fileName;
		this.properties = new Properties();
		File file = new File(fileName);

		if (file.exists()) {
			load();
		} else {
			createFile(file);
			save();
		}
	}

	private void createFile(File file) {
		try {
			log.info(this.fileName + " not found! Creating empty file at "
					+ file.getPath() + ".");
			file.getParentFile().mkdirs();
			file.createNewFile();
			this.isNew = true;
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Unable to create " + file.getPath(), ex);
		}
	}

	public void load() {
		try {
			this.properties.load(new FileInputStream(this.fileName));
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Unable to load " + this.fileName, ex);
		}
	}

	public void save() {
		try {
			this.properties.store(new FileOutputStream(this.fileName),
					"Minecraft Properties File");
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Unable to save " + this.fileName, ex);
		}
	}

	public Map<String, String> returnMap() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(
				new FileReader(this.fileName));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			int delimPosition = line.indexOf('=');
			String key = line.substring(0, delimPosition).trim();
			String value = line.substring(delimPosition + 1).trim();
			map.put(key, value);
		}
		reader.close();
		return map;
	}

	public void removeKey(String key) {
		this.properties.remove(key);
		save();
	}

	public void removeKey(int key) {
		removeKey("" + key);
	}

	public boolean keyExists(String key) {
		return this.properties.containsKey(key);
	}

	public boolean keyExists(int key) {
		return keyExists("" + key);
	}

	public String getString(String key) {
		if (this.properties.containsKey(key)) {
			return this.properties.getProperty(key);
		}

		return "";
	}

	public String getString(int key) {
		return getString("" + key);
	}

	public String getString(String key, String value) {
		if (this.properties.containsKey(key)) {
			return this.properties.getProperty(key);
		}
		setString(key, value);
		return value;
	}

	public String getString(int key, String value) {
		return getString("" + key, value);
	}

	public void setString(String key, String value) {
		this.properties.setProperty(key, value);
		save();
	}

	public void setString(int key, String value) {
		setString("" + key, value);
	}

	public int getInt(String key) {
		if (this.properties.containsKey(key)) {
			return Integer.parseInt(this.properties.getProperty(key));
		}

		return 0;
	}

	public int getInt(int key) {
		return getInt("" + key);
	}

	public int getInt(String key, int value) {
		if (this.properties.containsKey(key)) {
			return Integer.parseInt(this.properties.getProperty(key));
		}
		setInt(key, value);
		return value;
	}

	public int getInt(int key, int value) {
		return getInt("" + key, value);
	}

	public void setInt(String key, int value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}

	public void setInt(int key, int value) {
		setInt("" + key, value);
	}

	public double getDouble(String key) {
		if (this.properties.containsKey(key)) {
			return Double.parseDouble(this.properties.getProperty(key));
		}

		return 0;
	}

	public double getDouble(int key) {
		return getDouble("" + key);
	}

	public double getDouble(String key, double value) {
		if (this.properties.containsKey(key)) {
			return Double.parseDouble(this.properties.getProperty(key));
		}

		setDouble(key, value);
		return value;
	}

	public double getDouble(int key, double value) {
		return getDouble("" + key, value);
	}

	public void setDouble(String key, double value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}

	public void setDouble(int key, double value) {
		setDouble("" + key, value);
	}

	public long getLong(String key) {
		if (this.properties.containsKey(key)) {
			return Long.parseLong(this.properties.getProperty(key));
		}

		return 0;
	}

	public long getLong(int key) {
		return getLong("" + key);
	}

	public long getLong(String key, long value) {
		if (this.properties.containsKey(key)) {
			return Long.parseLong(this.properties.getProperty(key));
		}

		setLong(key, value);
		return value;
	}

	public long getLong(int key, long value) {
		return getLong("" + key, value);
	}

	public void setLong(String key, long value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}

	public void setLong(int key, long value) {
		setLong("" + key, value);
	}

	public boolean getBoolean(String key) {
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean(this.properties.getProperty(key));
		}

		return false;
	}

	public boolean getBoolean(int key) {
		return getBoolean("" + key);
	}

	public boolean getBoolean(String key, boolean value) {
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean(this.properties.getProperty(key));
		}

		setBoolean(key, value);
		return value;
	}

	public boolean getBoolean(int key, boolean value) {
		return getBoolean("" + key, value);
	}

	public void setBoolean(String key, boolean value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}

	public void setBoolean(int key, boolean value) {
		setBoolean("" + key, value);
	}

	public boolean isNew() {
		return this.isNew;
	}
}
