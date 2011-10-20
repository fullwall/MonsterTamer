package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.fullwall.MonsterTamer.data.storage.ConfigurationHandler;

public class Constants {
	private enum Config {
		SETTINGS;

		private final List<Constant> settings = new ArrayList<Constant>();

		public void add(Constant constant) {
			this.settings.add(constant);
		}

		public List<Constant> get() {
			return this.settings;
		}
	}

	public enum Constant {
		ConsumeItems("general.items.dropped.consume", true),
		Limit("general.monsters.limit", 50),
		SelectTool("general.tools.select", 280);

		private final String path;
		private Object value;

		Constant(Config config, String path, Object value) {
			this.path = path;
			this.value = value;
			config.add(this);
		}

		Constant(String path, Object value) {
			this(Config.SETTINGS, path, value);
		}

		public boolean getBoolean() {
			return (Boolean) this.getValue();
		}

		public double getDouble() {
			return (Double) this.getValue();
		}

		public int getInt() {
			return (Integer) this.getValue();
		}

		public String getString() {
			return (String) this.getValue();
		}

		public Object getValue() {
			return this.value;
		}

		public void set(Object value) {
			this.value = value;
		}

		public String getPath() {
			return path;
		}
	}

	public static final String noPermissionsMessage = ChatColor.RED
			+ "You don't have permission to use that command.";

	public static void readSettings(ConfigurationHandler properties) {
		boolean found = false;
		for (Config config : Config.values()) {
			for (Constant constant : config.get()) {
				if (!properties.pathExists(constant.getPath())) {
					properties.setRaw(constant.getPath(), constant.getValue());
					found = true;
				} else
					constant.set(properties.getRaw(constant.getPath()));
			}
		}
		if (found)
			properties.save();
	}
}