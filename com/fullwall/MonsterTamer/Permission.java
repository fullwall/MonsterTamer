package com.fullwall.MonsterTamer;

import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Permission {
	@SuppressWarnings("unused")
	private static Permissions permissionsPlugin;
	private static boolean permissionsEnabled = false;

	public static void initialize(Server server) {
		Plugin test = server.getPluginManager().getPlugin("Permissions");
		if (test != null) {
			@SuppressWarnings("unused")
			Logger log = Logger.getLogger("Minecraft");
			permissionsPlugin = (Permissions) test;
			permissionsEnabled = true;
		} else {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE,
					"[MonsterTamer]: Permissions plugin isn't loaded. Features are usable by all (or by ops only if configured).");
		}
	}

	public static boolean checkMonsters(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.check");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	private static boolean permission(Player player, String string) {
		return Permissions.Security.permission(player, string);
	}

	public static boolean check(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.catch");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	public static boolean friendly(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.befriend");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	public static boolean target(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.target");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	public static boolean release(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.release");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	public static boolean follow(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.follow");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	public static boolean whistle(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.whistle");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	public static boolean wait(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.wait");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

	public static boolean select(Player player) {
		if (permissionsEnabled) {
			return permission(player, "monstertamer.player.select");
		}
		return MonsterTamer.useOps ? player.isOp() : true;
	}

}