package com.fullwall.MonsterTamer.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.utils.StringUtils;

public class Tamer {
	private final String name;
	private final Map<String, Monster> owned = new HashMap<String, Monster>();
	private final Map<String, Monster> selected = new HashMap<String, Monster>();

	public Tamer(String name) {
		this.name = name;
	}

	public void addMonster(Monster monster) {
		this.owned.put(monster.getName(), monster);
	}

	public Collection<Monster> getMonsters() {
		return this.owned.values();
	}

	public Collection<Monster> getMonsters(String type) {
		CreatureType parsed = CreatureType.fromName(StringUtils
				.capitalise(type));
		Collection<Monster> matched = new ArrayList<Monster>();
		for (Monster monster : this.getMonsters()) {
			if (type.equalsIgnoreCase("all") || monster.matches(parsed))
				matched.add(monster);
		}
		return matched;
	}

	public Monster getMonster(String name) {
		return owned.get(name);
	}

	public String getName() {
		return name;
	}

	public boolean hasSelected() {
		return this.selected.size() > 0;
	}

	public void addSelected(Monster selected) {
		this.selected.put(selected.getName(), selected);
		selected.setSelected(true);
	}

	public Collection<Monster> getSelected() {
		return this.selected.values();
	}

	public void removeSelected(String name) {
		Monster monster = this.selected.remove(name);
		monster.setSelected(false);
	}

	public boolean hasCaughtWith(Material type) {
		for (Monster monster : this.owned.values()) {
			if (monster.getCaughtWith() == type) {
				return true;
			}
		}
		return false;
	}

	public Monster removeFirst(Material type) {
		for (Monster monster : this.owned.values()) {
			if (monster.getCaughtWith() == type) {
				return removeMonster(monster.getName());
			}
		}
		return null;
	}

	public Monster removeMonster(String name) {
		return this.owned.remove(name);
	}

	public Monster spawn(Location location, Material type) {
		for (Monster monster : this.owned.values()) {
			if (monster.getMonster() == null && monster.getCaughtWith() == type) {
				LivingEntity creature = location.getWorld().spawnCreature(
						location, monster.getType());
				if (creature == null)
					return null;
				monster.setMonster(creature);
				return monster;
			}
		}
		return null;
	}

	public void spawn(Location location, String name) {
		Monster monster = owned.get(name);
		monster.setMonster(location.getWorld().spawnCreature(location,
				monster.getType()));
	}

	public boolean hasMonsters() {
		return owned.size() > 0;
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(this.name);
	}

	public boolean owns(int ID) {
		return owned.get(ID) != null;
	}

	public boolean hasReleased() {
		for (Monster monster : this.owned.values()) {
			if (!monster.isDead())
				return true;
		}
		return false;
	}

	public int setMonstersOn(LivingEntity target) {
		int count = 0;
		for (Monster monster : getSelected()) {
			if (monster.withinRange(target)) {
				monster.setTarget(target);
				++count;
			}
		}
		return count;
	}

	public int setMonstersOn(LivingEntity target, CreatureType type) {
		int count = 0;
		for (Monster monster : getSelected()) {
			if ((type == null || monster.getType() == type)
					&& monster.withinRange(target)) {
				monster.setTarget(target);
				++count;
			}
		}
		return count;
	}

	public Monster getMonster(Monster monster) {
		return monster == null ? null : owned.get(monster.getName());
	}

	public Collection<Monster> getStored() {
		List<Monster> stored = new ArrayList<Monster>();
		for (Monster monster : getMonsters()) {
			if (monster.isDead())
				stored.add(monster);
		}
		return stored;
	}

	public boolean hasStoredMonsters() {
		return this.getStored().size() > 0;
	}

	public boolean isStored(String name) {
		return getStored(name) == null;
	}

	public Monster getStored(String name) {
		Monster temp = owned.get(name);
		return temp != null && temp.isDead() ? temp : null;
	}
}
