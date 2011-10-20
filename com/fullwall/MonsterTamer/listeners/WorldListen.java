package com.fullwall.MonsterTamer.listeners;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

import com.fullwall.MonsterTamer.MonsterManager;
import com.fullwall.MonsterTamer.data.Monster;

public class WorldListen extends WorldListener {
	public static ConcurrentHashMap<Monster, Location> toRespawn = new ConcurrentHashMap<Monster, Location>();

	@Override
	public void onChunkUnload(ChunkUnloadEvent e) {
		for (Entity entity : e.getChunk().getEntities()) {
			if (MonsterManager.monsters.containsKey(entity.getEntityId())) {
				Monster monster = MonsterManager.getMonster(entity);
				monster.setMonster(null);
				toRespawn.put(monster, entity.getLocation());
				monster.setRespawnPoint(entity.getLocation());
			}
		}
		return;

	}

	@Override
	public void onChunkLoad(ChunkLoadEvent e) {
		if (toRespawn.size() > 0) {
			Monster monster;
			Location loc;
			for (Entry<Monster, Location> entry : toRespawn.entrySet()) {
				monster = entry.getKey();
				loc = entry.getValue();
				monster.setMonster(loc.getWorld().spawnCreature(loc,
						monster.getType()));
				monster.setRespawnPoint(null);
			}
		}
	}
}
