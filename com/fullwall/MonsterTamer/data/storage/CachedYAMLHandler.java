package com.fullwall.MonsterTamer.data.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;

import com.fullwall.MonsterTamer.utils.Messaging;
import com.fullwall.MonsterTamer.utils.StringUtils;

public class CachedYAMLHandler {
	private final SettingsTree tree = new SettingsTree();
	private final Configuration config;
	private final String fileName;

	public CachedYAMLHandler(String fileName) {
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
		clear();
		config.load();
		for (Entry<String, Object> entry : this.config.getAll().entrySet()) {
			tree.set(entry.getKey(), entry.getValue().toString());
		}
		clear();
	}

	public void save() {
		clear();
		for (Entry<String, String> entry : tree.getTree().entrySet()) {
			if (entry.getValue() != null && !entry.getValue().isEmpty()
					&& !StringUtils.isNumber(entry.getKey())) {
				this.config.setProperty(entry.getKey(), entry.getValue());
			}
		}
		this.config.save();
		clear();
	}

	private void clear() {
		for (String path : config.getAll().keySet()) {
			config.removeProperty(path);
		}
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
		this.tree.remove(path);
	}

	public void removeKey(int path) {
		removeKey("" + path);
	}

	public boolean pathExists(String path) {
		return this.tree.get(path) != null;
	}

	public boolean valueExists(String path) {
		return pathExists(path) && !this.tree.get(path).isEmpty();
	}

	public boolean pathExists(int path) {
		return pathExists("" + path);
	}

	private String get(String path) {
		return this.tree.get(path);
	}

	public String getString(String path) {
		if (valueExists(path)) {
			return get(path);
		}
		return "";
	}

	public String getString(int path) {
		return getString("" + path);
	}

	public String getString(String path, String value) {
		if (valueExists(path)) {
			return get(path);
		} else {
			setString(path, value);
		}
		return value;
	}

	public String getString(int path, String value) {
		return getString("" + path, value);
	}

	public void setString(String path, String value) {
		this.tree.set(path, value);
	}

	public void setString(int path, String value) {
		setString("" + path, value);
	}

	public void forceSetString(String path, String value) {
		setString(path, value);
		this.config.setProperty(path, value);
	}

	public int getInt(String path) {
		if (valueExists(path)) {
			return Integer.parseInt(get(path));
		}
		return 0;
	}

	public int getInt(int path) {
		return getInt("" + path);
	}

	public int getInt(String path, int value) {
		if (valueExists(path)) {
			return getInt(path);
		} else {
			setInt(path, value);
		}
		return value;
	}

	public int getInt(int path, int value) {
		return getInt("" + path, value);
	}

	public void setInt(String path, int value) {
		this.tree.set(path, String.valueOf(value));
	}

	public void setInt(int path, int value) {
		setInt("" + path, value);
	}

	public double getDouble(String path) {
		if (valueExists(path)) {
			return Double.parseDouble(get(path));
		}
		return 0;
	}

	public double getDouble(int path) {
		return getDouble("" + path);
	}

	public double getDouble(String path, double value) {
		if (valueExists(path)) {
			return getDouble(path);
		} else {
			setDouble(path, value);
		}
		return value;
	}

	public double getDouble(int path, double value) {
		return getDouble("" + path, value);
	}

	public void setDouble(String path, double value) {
		this.tree.set(path, String.valueOf(value));
	}

	public void setDouble(int path, double value) {
		setDouble("" + path, value);
	}

	public long getLong(String path) {
		if (valueExists(path)) {
			return Long.parseLong(get(path));
		}
		return 0;
	}

	public long getLong(int path) {
		return getLong("" + path);
	}

	public long getLong(String path, long value) {
		if (valueExists(path)) {
			return getLong(path);
		} else {
			setLong(path, value);
		}
		return value;
	}

	public long getLong(int path, long value) {
		return getLong("" + path, value);
	}

	public void setLong(String path, long value) {
		this.tree.set(path, String.valueOf(value));
	}

	public void setLong(int path, long value) {
		setLong("" + path, value);
	}

	public boolean getBoolean(String path) {
		return pathExists(path) && Boolean.parseBoolean(get(path));
	}

	public boolean getBoolean(int path) {
		return getBoolean("" + path);
	}

	public boolean getBoolean(String path, boolean value) {
		if (valueExists(path)) {
			return getBoolean(path);
		} else {
			setBoolean(path, value);
		}
		return value;
	}

	public boolean getBoolean(int path, boolean value) {
		return getBoolean("" + path, value);
	}

	public void setBoolean(String path, boolean value) {
		this.tree.set(path, String.valueOf(value));
	}

	public void setBoolean(int path, boolean value) {
		setBoolean("" + path, value);
	}

	public List<String> getKeys(String path) {
		return this.config.getKeys(path);
	}

	public List<Integer> getIntegerKeys(String path) {
		load();
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (String str : this.config.getKeys(path)) {
			try {
				ret.add(Integer.parseInt(str.replace("'", "")));
			} catch (NumberFormatException ex) {

			}
		}
		return ret;
	}
}