package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

public class WorldListen extends WorldListener {
	private static MonsterTamer plugin;
	public static ConcurrentHashMap<Location, ArrayList<String>> toRespawn = new ConcurrentHashMap<Location, ArrayList<String>>();

	@SuppressWarnings("static-access")
	public WorldListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onChunkUnload(ChunkUnloadEvent e) {
		if (MonsterTamer.stopDespawning == true) {
			for (Entity entity : e.getChunk().getEntities()) {
				if (!(entity instanceof Player)
						&& entity instanceof LivingEntity
						&& entity instanceof Creature
						&& MonsterTamer.friendlies.contains(entity
								.getEntityId())) {
					LivingEntity living = (LivingEntity) entity;
					String playerName = "";
					Location loc = living.getLocation();
					ArrayList<String> toPut = new ArrayList<String>();
					toPut.add(EntityListen.checkMonsters(living));
					for (Entry<String, ArrayList<Integer>> i : MonsterTamer.friends
							.entrySet()) {
						if (i.getValue().contains(living.getEntityId())) {
							playerName = i.getKey();
							i.getValue().remove(
									i.getValue().indexOf(living.getEntityId()));
							break;
						}
					}
					toPut.add(playerName);

					if (MonsterTamer.targets.containsKey(""
							+ living.getEntityId())) {
						toPut.add(MonsterTamer.targets.get(""
								+ living.getEntityId()));
						MonsterTamer.targets.remove("" + living.getEntityId());
					} else
						toPut.add("");

					String ownerName = "";
					for (Entry<String, ArrayList<Integer>> i : MonsterTamer.followers
							.entrySet()) {
						if (i.getValue().contains("" + living.getEntityId())) {
							ownerName = i.getKey();
							i.getValue().remove(
									i.getValue().indexOf(living.getEntityId()));
							break;
						}
					}
					toPut.add(ownerName);
					String waiter = "";
					for (Entry<String, ArrayList<Integer>> i : MonsterTamer.waiters
							.entrySet()) {
						if (i.getValue().contains("" + living.getEntityId())) {
							ownerName = i.getKey();
							i.getValue().remove(
									i.getValue().indexOf(living.getEntityId()));
							break;
						}
					}
					toPut.add(waiter);
					String selectedMonster = "";
					for (Entry<String, ArrayList<Integer>> i : MonsterTamer.selectedMonsters
							.entrySet()) {
						if (i.getValue().contains("" + living.getEntityId())) {
							ownerName = i.getKey();
							i.getValue().remove(
									i.getValue().indexOf(living.getEntityId()));
							break;
						}
					}
					toPut.add(selectedMonster);
					MonsterTamer.friendlies.remove(MonsterTamer.friendlies
							.indexOf(living.getEntityId()));
					toRespawn.put(loc, toPut);
				}
			}
			return;
		}
	}

	@Override
	public void onChunkLoad(ChunkLoadEvent e) {
		if (MonsterTamer.stopDespawning == true && toRespawn.size() > 0) {
			Bukkit.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new RespawnTask(e), 5);

		}
	}

	public class RespawnTask implements Runnable {

		private final ChunkLoadEvent e;

		public RespawnTask(ChunkLoadEvent e) {
			this.e = e;
		}

		@Override
		public void run() {
			for (Entry<Location, ArrayList<String>> entry : toRespawn
					.entrySet()) {
				if (e.getChunk().getX() == e.getWorld()
						.getChunkAt(entry.getKey()).getX()
						&& e.getChunk().getZ() == e.getWorld()
								.getChunkAt(entry.getKey()).getZ()) {
					LivingEntity monster = entry
							.getKey()
							.getWorld()
							.spawnCreature(
									entry.getKey(),
									CreatureType.fromName(entry.getValue().get(
											0)));
					int entityID = monster.getEntityId();
					MonsterTamer.friendlies.add(entityID);
					String name = entry.getValue().get(1);
					if (!name.isEmpty()) {
						ArrayList<Integer> array = MonsterTamer.friends
								.get(name);
						if (array == null)
							array = new ArrayList<Integer>();
						array.add(entityID);
						MonsterTamer.friends.put(name, array);
					}
					name = entry.getValue().get(2);
					if (!name.isEmpty()) {
						MonsterTamer.targets.put("" + entityID, name);
					}
					name = entry.getValue().get(3);
					if (!name.isEmpty()) {
						ArrayList<Integer> array = MonsterTamer.followers
								.get(name);
						if (array == null)
							array = new ArrayList<Integer>();
						array.add(entityID);
						MonsterTamer.followers.put(name, array);
					}
					name = entry.getValue().get(4);
					if (!name.isEmpty()) {
						ArrayList<Integer> array = MonsterTamer.waiters
								.get(name);
						if (array == null)
							array = new ArrayList<Integer>();
						array.add(entityID);
						MonsterTamer.waiters.put(name, array);
					}
					name = entry.getValue().get(5);
					if (!name.isEmpty()) {
						ArrayList<Integer> array = MonsterTamer.selectedMonsters
								.get(name);
						if (array == null)
							array = new ArrayList<Integer>();
						array.add(entityID);
						MonsterTamer.selectedMonsters.put(name, array);
					}
					toRespawn.remove(entry.getKey());
					// friends(!) targets, followers, friendlies
				}
			}
		}

	}
}
