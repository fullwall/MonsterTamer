package com.fullwall.MonsterTamer.sk89q;

import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.Permission;

public class MonsterTamerCommandsManager<T extends Player> extends
		CommandsManager<T> {

	@Override
	public boolean hasPermission(T player, String perm) {
		if (perm.equalsIgnoreCase("admin")) {
			return Permission.isAdmin(player);
		} else {
			return Permission.generic(player, perm);
		}
	}
}