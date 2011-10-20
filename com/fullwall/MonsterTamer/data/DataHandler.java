package com.fullwall.MonsterTamer.data;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;

import com.fullwall.MonsterTamer.Constants;
import com.fullwall.MonsterTamer.MonsterManager;
import com.fullwall.MonsterTamer.data.storage.CachedYAMLHandler;
import com.fullwall.MonsterTamer.data.storage.ConfigurationHandler;
import com.fullwall.MonsterTamer.utils.StringUtils;
import com.google.common.collect.Maps;

public class DataHandler {
	public static final ConfigurationHandler properties = new ConfigurationHandler(
			"plugins/MonsterTamer/monstertamer.yml");
	public static final CachedYAMLHandler users = new CachedYAMLHandler(
			"plugins/MonsterTamer/users.yml");

	public static void saveAll() {
		saveUsers();
		users.save();
	}

	public static void loadSettings() {
		Constants.readSettings(properties);
		if (!properties.pathExists("catchables")) {
			return;
		}
		for (String property : properties.getKeys("catchables")) {
			if (property.equals("global"))
				continue;
			String path = "catchables." + property;
			CreatureType type = CreatureType.fromName(StringUtils
					.capitalise(property.toLowerCase()));
			double monsterChance = properties.getDouble(path + ".multiplier");
			Map<Integer, Double> itemChances = Maps.newHashMap();
			populate("catchables.global", itemChances);
			if (properties.pathExists(path + ".inherit")) {
				String inheritFrom = properties.getString(path + ".inherit");
				for (String prop : inheritFrom.split(",")) {
					populate("catchables." + prop, itemChances);
				}
			}
			populate(path, itemChances);
			MonsterManager.addCatchable(type, new Catchable(monsterChance,
					itemChances));
		}
		loadUsers();
	}

	public static void loadUsers() {
		String path = "", temp = "";
		for (String key : users.getKeys(null)) {
			path = key;
			String[] split = key.split("->");
			Tamer tamer = MonsterManager.getTamer(split[1]);
			if (tamer == null)
				MonsterManager.addTamer(split[1], tamer = new Tamer(split[1]));
			Material caughtWith = Material.getMaterial(users.getInt(path
					+ ".caughtwith"));
			CreatureType type = CreatureType.fromName(users.getString(path
					+ ".type"));
			Monster monster = new Monster(tamer, type, caughtWith);
			if (users.pathExists(path + ".monster")) {
				temp = path + ".monster";
				boolean follow = users.getBoolean(temp + ".follow");
				boolean selected = users.getBoolean(temp + ".selected");
				boolean wait = users.getBoolean(temp + ".wait");
				Location moveTo = null;
				if (users.pathExists(temp + ".moveto")) {
					moveTo = loadLocation(temp + ".moveto");
				}
				temp += ".location";
				Location loc = loadLocation(temp);
				LivingEntity living = loc.getWorld().spawnCreature(loc, type);
				if (living != null) {
					monster.setMonster(living);
					monster.setFollow(follow);
					monster.setWait(wait);
					monster.setSelected(selected);
					monster.setMoveTo(moveTo);
				}
				MonsterManager.monsters.put(living.getEntityId(), monster);
			}
			tamer.addMonster(monster);
		}
	}

	public static void saveUsers() {
		for (Tamer tamer : MonsterManager.tamers.values()) {
			String path = "", temp = "";
			for (Monster monster : tamer.getMonsters()) {
				path = monster.getName() + "->" + tamer.getName();
				users.setInt(path + ".caughtwith", monster.getCaughtWith()
						.getId());
				users.setString(path + ".type", monster.getType().getName());
				if (monster.getMonster() != null
						|| monster.getRespawnPoint() != null) {
					temp = path + ".monster";
					users.setBoolean(temp + ".follow", monster.isFollow());
					users.setBoolean(temp + ".selected", monster.isSelected());
					users.setBoolean(temp + ".wait", monster.isWait());

					if (monster.getMoveTo() != null) {
						temp += ".moveto";
						saveLocation(monster.getMoveTo(), temp);
					}
					temp = path + ".monster.location";
					Location loc = monster.getMonster() == null ? monster
							.getRespawnPoint() : monster.getMonster()
							.getLocation();
					saveLocation(loc, temp);
				} else {
					users.removeKey(path + ".monster");
				}
			}
		}
	}

	private static void populate(String path, Map<Integer, Double> itemChances) {
		path += ".items";
		if (!properties.pathExists(path))
			return;
		for (Object itemChance : properties.getKeys(path)) {
			itemChances.put(
					itemChance instanceof String ? Integer
							.parseInt((String) itemChance)
							: (Integer) itemChance, properties.getDouble(path
							+ "." + itemChance + ".multiplier"));
		}
		return;

	}

	private static Location loadLocation(String path) {
		String world = users.getString(path + ".world");
		double x = users.getDouble(path + ".x");
		double y = users.getDouble(path + ".y");
		double z = users.getDouble(path + ".z");
		float pitch = (float) users.getDouble(path + ".pitch");
		float yaw = (float) users.getDouble(path + ".yaw");
		return new Location(Bukkit.getServer().getWorld(world), x, y, z, pitch,
				yaw);
	}

	private static void saveLocation(Location loc, String path) {
		users.setString(path + ".world", loc.getWorld().getName());
		users.setDouble(path + ".x", loc.getX());
		users.setDouble(path + ".y", loc.getY());
		users.setDouble(path + ".z", loc.getZ());
		users.setDouble(path + ".pitch", loc.getPitch());
		users.setDouble(path + ".yaw", loc.getYaw());
	}
}
