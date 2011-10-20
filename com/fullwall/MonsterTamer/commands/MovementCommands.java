package com.fullwall.MonsterTamer.commands;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.data.Monster;
import com.fullwall.MonsterTamer.data.Tamer;
import com.fullwall.MonsterTamer.sk89q.Command;
import com.fullwall.MonsterTamer.sk89q.CommandContext;
import com.fullwall.MonsterTamer.sk89q.CommandPermissions;
import com.fullwall.MonsterTamer.utils.StringUtils;

public class MovementCommands {
	@Command(
			aliases = { "follow" },
			usage = "follow [type] (-c)",
			desc = "tamed monsters of the given type follow you",
			flags = "c",
			modifiers = "*",
			min = 2,
			max = 2)
	@CommandPermissions("monstertamer.player.follow")
	public static void addFollowers(CommandContext args, Tamer tamer,
			Player player) {
		boolean remove = args.getFlags().contains('c');
		short count = 0;
		Collection<Monster> matched = tamer.getMonsters(args.getString(0));
		if (matched.size() == 0) {
			player.sendMessage(ChatColor.RED
					+ "None of your tamed monsters matched that type.");
			return;
		} else {
			for (Monster monster : matched) {
				if (!remove) {
					if (!monster.isFollow()) {
						monster.setFollow(true);
						++count;
					}
				} else if (monster.isFollow()) {
					monster.setFollow(false);
					--count;
				}
			}
			String verb = remove ? " stopped " : " started ";
			player.sendMessage(StringUtils.wrap(count + " "
					+ StringUtils.capitalise(args.getString(0)))
					+ "s" + verb + "following you.");
		}
	}

	@Command(
			aliases = { "wait" },
			usage = "wait [type] (-c)",
			desc = "tamed monsters of the given type follow you",
			flags = "c",
			modifiers = "*",
			min = 2,
			max = 2)
	@CommandPermissions("monstertamer.player.wait")
	public static void addWaiters(CommandContext args, Tamer tamer,
			Player player) {
		boolean remove = args.getFlags().contains('c');
		short count = 0;
		Collection<Monster> matched = tamer.getMonsters(args.getString(0));
		if (matched.size() == 0) {
			player.sendMessage(ChatColor.RED
					+ "None of your tamed monsters matched that type.");
			return;
		} else {
			for (Monster monster : matched) {
				if (!remove) {
					if (!monster.isFollow()) {
						monster.setWait(true);
						++count;
					}
				} else if (monster.isFollow()) {
					monster.setWait(false);
					--count;
				}
			}
			String verb = remove ? " stopped " : " started ";
			player.sendMessage(StringUtils.wrap(count + " "
					+ StringUtils.capitalise(args.getString(0)))
					+ "s" + verb + "waiting.");
		}
	}

	@Command(
			aliases = { "whistle" },
			usage = "whistle",
			desc = "tamed monsters come to your current location",
			modifiers = "",
			min = 1,
			max = 1)
	@CommandPermissions("monstertamer.player.whistle")
	public static void whistle(CommandContext args, Tamer tamer, Player player) {
		for (Monster monster : tamer.getMonsters()) {
			monster.setMoveTo(player.getLocation());
		}
		player.sendMessage(ChatColor.GREEN
				+ "Your monsters are en-route to your location.");
	}
}
