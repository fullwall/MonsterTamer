package com.fullwall.MonsterTamer;

import java.util.Map;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.data.Catchable;
import com.fullwall.MonsterTamer.data.Monster;
import com.fullwall.MonsterTamer.data.Tamer;
import com.google.common.collect.Maps;

public class MonsterManager {
	public static Map<String, Tamer> tamers = Maps.newConcurrentMap();
	public static Map<Integer, Monster> monsters = Maps.newConcurrentMap();
	public static Map<CreatureType, Catchable> catchables = Maps
			.newEnumMap(CreatureType.class);

	public static Tamer getTamer(Player player) {
		return getTamer(player.getName());
	}

	public static Tamer getTamer(String name) {
		return tamers.get(name);
	}

	public static Monster getMonster(Entity entity) {
		return monsters.get(entity.getEntityId());
	}

	public static void addTamer(Player player, Tamer tamer) {
		addTamer(player.getName(), tamer);
	}

	public static void addTamer(String name, Tamer tamer) {
		tamers.put(name, tamer);
	}

	public static void addCatchable(CreatureType type, Catchable catchable) {
		catchables.put(type, catchable);
	}
}
