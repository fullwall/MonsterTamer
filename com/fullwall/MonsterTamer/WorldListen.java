package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

public class WorldListen extends WorldListener {
	@SuppressWarnings("unused")
	private static MonsterTamer plugin;

	@SuppressWarnings("static-access")
	public WorldListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	public void onChunkUnloaded(ChunkUnloadEvent e) {
		for (Entry<String, ArrayList<String>> entry : MonsterTamer.friends
				.entrySet()) {
			for (String i : entry.getValue()) {
				List<LivingEntity> ab = e.getWorld().getLivingEntities();
				for (LivingEntity le : ab) {
					if (Math.abs(e.getChunk().getX()
							- le.getLocation().getBlock().getChunk().getX()) > 1) {
						continue;
					}
					if (Math.abs(e.getChunk().getZ()
							- le.getLocation().getBlock().getChunk().getZ()) > 1) {
						continue;
					}
					if (le.getEntityId() == Integer.parseInt(i))
						e.setCancelled(true);
				}
			}
		}
		return;
	}
}
