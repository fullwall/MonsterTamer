package com.fullwall.MonsterTamer;

import org.bukkit.entity.Player;

public class Permission {
	private static boolean permission(Player player, String string) {
		return player.hasPermission(string);
	}

	public static boolean isAdmin(Player player) {
		return permission(player, "monstertamer.admin");
	}

	public static boolean generic(Player player, String permission) {
		return permission(player, permission);
	}
}