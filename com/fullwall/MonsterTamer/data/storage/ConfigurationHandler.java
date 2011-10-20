package com.fullwall.MonsterTamer.data.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.fullwall.MonsterTamer.utils.Messaging;

public class ConfigurationHandler {
	private final Configuration config;
	private final String fileName;

	public ConfigurationHandler(String fileName) {
		this.fileName = fileName;
		File file = getFile();
		this.config = new Configuration(file);
		if (!file.exists()) {
			create();
			save();
		} else {
			load();
		}
	}

	public void load() {
		config.load();
	}

	public void save() {
		this.config.save();
	}

	private void create() {
		File file = getFile();
		try {
			Messaging.log("Creating new config file at " + fileName + ".");
			file.getParentFile().mkdirs();
			file.createNewFile();
		} catch (IOException ex) {
			Messaging.log("Unable to create " + file.getPath() + ".",
					Level.SEVERE);
		}
	}

	private File getFile() {
		return new File(this.fileName);
	}

	public void removeKey(String path) {
		this.config.removeProperty(path);
		save();
	}

	public void removeKey(int path) {
		removeKey("" + path);
	}

	public boolean pathExists(String path) {
		return this.config.getProperty(path) != null;
	}

	public boolean pathExists(int path) {
		return pathExists("" + path);
	}

	public String getString(String path) {
		if (pathExists(path)) {
			return this.config.getString(path);
		}
		return "";
	}

	public String getString(int path) {
		return getString("" + path);
	}

	public String getString(String path, String value) {
		if (pathExists(path)) {
			return this.config.getString(path);
		} else {
			setString(path, value);
		}
		return value;
	}

	public String getString(int path, String value) {
		return getString("" + path, value);
	}

	public void setString(String path, String value) {
		this.config.setProperty(path, value);
		save();
	}

	public void setString(int path, String value) {
		setString("" + path, value);
	}

	public int getInt(String path) {
		if (pathExists(path)) {
			return Integer.parseInt(this.config.getString(path));
		}
		return 0;
	}

	public int getInt(int path) {
		return getInt("" + path);
	}

	public int getInt(String path, int value) {
		return this.config.getInt(path, value);
	}

	public int getInt(int path, int value) {
		return getInt("" + path, value);
	}

	public void setInt(String path, int value) {
		this.config.setProperty(path, String.valueOf(value));
		save();
	}

	public void setInt(int path, int value) {
		setInt("" + path, value);
	}

	public double getDouble(String path) {
		if (pathExists(path)) {
			return Double.parseDouble(this.config.getString(path));
		}
		return 0;
	}

	public double getDouble(int path) {
		return getDouble("" + path);
	}

	public double getDouble(String path, double value) {
		return this.config.getDouble(path, value);
	}

	public double getDouble(int path, double value) {
		return getDouble("" + path, value);
	}

	public void setDouble(String path, double value) {
		this.config.setProperty(path, String.valueOf(value));
		save();
	}

	public void setDouble(int path, double value) {
		setDouble("" + path, value);
	}

	public long getLong(String path) {
		if (pathExists(path)) {
			return Long.parseLong(this.config.getString(path));
		}
		return 0;
	}

	public long getLong(int path) {
		return getLong("" + path);
	}

	public long getLong(String path, long value) {
		return this.config.getInt(path, (int) value);
	}

	public long getLong(int path, long value) {
		return getLong("" + path, value);
	}

	public void setLong(String path, long value) {
		this.config.setProperty(path, String.valueOf(value));
		save();
	}

	public void setLong(int path, long value) {
		setLong("" + path, value);
	}

	public boolean getBoolean(String path) {
		return pathExists(path)
				&& Boolean.parseBoolean(this.config.getString(path));
	}

	public boolean getBoolean(int path) {
		return getBoolean("" + path);
	}

	public boolean getBoolean(String path, boolean value) {
		return this.config.getBoolean(path, value);
	}

	public boolean getBoolean(int path, boolean value) {
		return getBoolean("" + path, value);
	}

	public void setBoolean(String path, boolean value) {
		this.config.setProperty(path, String.valueOf(value));
		save();
	}

	public void setBoolean(int path, boolean value) {
		setBoolean("" + path, value);
	}

	public List<String> getKeys(String path) {
		return this.config.getKeys(path);
	}

	public ConfigurationNode getNode(String path) {
		return this.config.getNode(path);
	}

	public Object getRaw(String path) {
		return config.getProperty(path);
	}

	public void setRaw(String path, Object value) {
		config.setProperty(path, value);
	}
}