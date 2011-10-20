package com.fullwall.MonsterTamer.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.data.DataHandler;
import com.fullwall.MonsterTamer.data.Monster;
import com.fullwall.MonsterTamer.data.Tamer;
import com.fullwall.MonsterTamer.sk89q.Command;
import com.fullwall.MonsterTamer.sk89q.CommandContext;
import com.fullwall.MonsterTamer.sk89q.CommandPermissions;
import com.fullwall.MonsterTamer.utils.LocationUtils;
import com.fullwall.MonsterTamer.utils.Messaging;
import com.fullwall.MonsterTamer.utils.PageUtils;
import com.fullwall.MonsterTamer.utils.PageUtils.PageInstance;
import com.fullwall.MonsterTamer.utils.StringUtils;

public class ManagementCommands {
	@Command(
			aliases = { "monsters", "ms" },
			usage = "reload",
			desc = "reloads configuration",
			modifiers = "reload",
			min = 1,
			max = 1)
	@CommandPermissions("monstertamer.admin.reload")
	public static void reload(CommandContext args, Tamer tamer, Player player) {
		Messaging.log("Reloading settings...");
		DataHandler.loadSettings();
		player.sendMessage(ChatColor.GREEN + "Settings reloaded.");
		Messaging.log("Completed.");
	}

	@Command(
			aliases = { "release" },
			usage = "release [name]",
			desc = "release a monster from holding",
			modifiers = "",
			min = 1,
			max = 1)
	@CommandPermissions("monstertamer.player.release")
	public static void releaseMonster(CommandContext args, Tamer tamer,
			Player player) {
		if (!tamer.hasMonsters()) {
			player.sendMessage(ChatColor.GRAY + "You don't have any monsters.");
			return;
		}
		String name = args.getString(0);
		if (tamer.getMonster(name) == null) {
			player.sendMessage(ChatColor.GRAY
					+ "You don't own a monster of that name.");
			return;
		}
		if (!tamer.isStored(name)) {
			player.sendMessage(ChatColor.GRAY
					+ "That monster has already been released.");
			return;
		}
		tamer.spawn(player.getLocation(), name);
		player.sendMessage(ChatColor.GREEN + "Released "
				+ StringUtils.wrap(name) + ".");
	}

	@Command(
			aliases = { "ms", "monster", "monsters" },
			usage = "ms|monster|monsters (page)",
			desc = "list owned monsters",
			modifiers = "",
			min = 0,
			max = 1)
	@CommandPermissions("monstertamer.player.list")
	public static void listMonsters(CommandContext args, Tamer tamer,
			Player player) {
		if (!tamer.hasMonsters()) {
			player.sendMessage(ChatColor.GRAY
					+ "You don't have any monsters yet.");
			return;
		}
		int page = 1;
		if (args.argsLength() == 2)
			page = args.getInteger(1);
		PageInstance instance = PageUtils.newInstance(player);
		instance.header(StringUtils.defaultListify(ChatColor.GOLD
				+ "Monster list for "
				+ StringUtils.wrap(player.getName(), ChatColor.GOLD)
				+ " <%x/%y>"));
		for (Monster monster : tamer.getMonsters()) {
			instance.push(ChatColor.GREEN + "   - "
					+ StringUtils.wrap(monster.getName()) + ": a "
					+ StringUtils.wrap(monster.formattedType())
					+ ", caught with a "
					+ StringUtils.wrap(monster.formattedCaughtWith()) + ".");
		}
		instance.process(page);
		return;
	}

	@Command(
			aliases = { "ms", "monster", "monsters" },
			usage = "ms|monster|monsters [help] (page)",
			desc = "list MonsterTamer commands",
			modifiers = "help",
			min = 1,
			max = 2)
	@CommandPermissions("monstertamer.player.help")
	public static void sendHelp(CommandContext args, Tamer tamer, Player player) {
		int page = 1;
		if (args.argsLength() == 2)
			page = args.getInteger(1);
		PageInstance instance = PageUtils.newInstance(player);
		instance.header(StringUtils.defaultListify("MonsterTamer Help <%x/%y>"));
		instance.push(format("monsters|ms", "(page)",
				"displays a list of owned monsters"));
		instance.push(format("target", "-p|-m [name|mobtype]",
				"selected monsters target command"));
		instance.push(format("target", "cancel [name|mobtype]",
				"cancel selected monsters' target"));
		instance.push(format("follow", "[mobtype|all] (cancel)",
				"follower control commands"));
		instance.push(format("wait", "[mobtype|all] (cancel)",
				"waiter control commands"));
		instance.push(format("release", "[name]",
				"release the specified monster"));
		instance.push(format("whistle", "", "monsters walk back to you"));
		instance.push(ChatColor.DARK_GRAY
				+ StringUtils.listify("Coded by fullwall"));
		instance.process(page);
		return;
	}

	@Command(
			aliases = { "select" },
			usage = "select [mobtype|all] (range) (-d)",
			desc = "select monsters in range (default 10)",
			flags = "d",
			modifiers = "*",
			min = 1,
			max = 2)
	@CommandPermissions("monstertamer.player.select")
	public static void selectMonsters(CommandContext args, Tamer tamer,
			Player player) {
		if (!tamer.hasMonsters()) {
			player.sendMessage(ChatColor.GRAY
					+ "You don't have any monsters to select.");
			return;
		}
		boolean deselect = args.getFlags().contains('d');
		String name = StringUtils.capitalise(args.getString(0).toLowerCase());
		if (!(name.equals("All")) && CreatureType.fromName(name) == null) {
			player.sendMessage(ChatColor.RED + "Incorrect monster type.");
			return;
		}
		int count = 0, range = 10;
		if (args.argsLength() == 2)
			range = args.getInteger(range);
		for (Monster monster : tamer.getMonsters()) {
			if (!monster.isSelected()
					&& monster.getMonster() != null
					&& LocationUtils.withinRange(player, monster.getMonster(),
							range)) {
				monster.setSelected(true);
				++count;
			}
		}
		String prefix = deselect ? "Dese" : "Se";
		if (count > 0) {
			player.sendMessage(ChatColor.GREEN + prefix + " "
					+ StringUtils.wrap(count) + " monsters within range.");
		} else {
			player.sendMessage(ChatColor.GRAY + "No monsters within range.");
		}
	}

	/**
	 * Formats commands to fit a uniform style
	 * 
	 * @param sender
	 * @param command
	 * @param desc
	 */
	private static String format(String command, String args, String desc) {
		String message = "";
		if (args.isEmpty()) {
			message = StringUtils.wrap("/") + command + StringUtils.wrap(" - ")
					+ desc;
		} else {
			message = StringUtils.wrap("/") + command + ChatColor.RED + " "
					+ args + StringUtils.wrap(" - ") + desc;
		}
		return message;
	}
}
