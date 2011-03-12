package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

public class WorldListen extends WorldListener {
	private static MonsterTamer plugin;
	private HashMap<Location, ArrayList<String>> toRespawn = new HashMap<Location, ArrayList<String>>();

	@SuppressWarnings("static-access")
	public WorldListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	public void onChunkUnloaded(ChunkUnloadEvent e) {
		if (MonsterTamer.stopDespawning == true) {
			List<LivingEntity> ab = e.getWorld().getLivingEntities();
			for (LivingEntity le : ab) {
				if (!(le instanceof Player)
						&& le instanceof Creature
						&& MonsterTamer.friendlies.contains(""
								+ le.getEntityId())
						&& le.getWorld().getChunkAt(le.getLocation()).getX() == e
								.getChunk().getX()) {
					MonsterTamer.log.info("Unloaded.");
					String playerName = "";
					Location loc = le.getLocation();
					ArrayList<String> toPut = new ArrayList<String>();
					toPut.add(plugin.el.checkMonsters(le));
					for (Entry<String, ArrayList<String>> i : MonsterTamer.friends
							.entrySet()) {
						if (i.getValue().contains("" + le.getEntityId())) {
							playerName = i.getKey();
							i.getValue()
									.remove(i.getValue().indexOf(
											"" + le.getEntityId()));
							break;
						}
					}
					toPut.add(playerName);

					if (MonsterTamer.targets.containsKey("" + le.getEntityId())) {
						toPut.add(MonsterTamer.targets.get(""
								+ le.getEntityId()));
						MonsterTamer.targets.remove("" + le.getEntityId());
					} else
						toPut.add("");

					for (Entry<String, ArrayList<Integer>> i : MonsterTamer.followers
							.entrySet()) {
						if (i.getValue().contains("" + le.getEntityId())) {
							playerName = i.getKey();
							i.getValue().remove(
									i.getValue().indexOf(le.getEntityId()));
							break;
						}
					}
					MonsterTamer.friendlies.remove(MonsterTamer.friendlies
							.indexOf("" + le.getEntityId()));
					toRespawn.put(loc, toPut);
				}
			}
			return;
		}
	}

	public void onChunkLoaded(ChunkLoadEvent e) {
		if (MonsterTamer.stopDespawning == true) {
			for (Entry<Location, ArrayList<String>> entry : toRespawn
					.entrySet()) {
				if (e.getChunk().getWorld().getChunkAt(entry.getKey())
						.equals(e.getChunk())) {
					MonsterTamer.log.info("Triggered");
					Creature monster = entry
							.getKey()
							.getWorld()
							.spawnCreature(
									entry.getKey(),
									CreatureType.fromName(entry.getValue().get(
											0)));
					MonsterTamer.friendlies.add("" + monster.getEntityId());
					String name = entry.getValue().get(1);
					if (!name.isEmpty()) {
						ArrayList<String> array = MonsterTamer.friends
								.get(name);
						array.add("" + monster.getEntityId());
						MonsterTamer.friends.put(name, array);
					}
					name = entry.getValue().get(2);
					if (!name.isEmpty()) {
						MonsterTamer.targets.put("" + monster.getEntityId(),
								name);
					}
					name = entry.getValue().get(3);
					if (!name.isEmpty()) {
						ArrayList<Integer> array = MonsterTamer.followers
								.get(name);
						array.add(monster.getEntityId());
						MonsterTamer.followers.put(name, array);
					}
					toRespawn.remove(entry.getKey());
					// friends(!) targets, followers, friendlies
				}
			}
		}
	}
}
