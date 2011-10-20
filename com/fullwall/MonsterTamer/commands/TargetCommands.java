package com.fullwall.MonsterTamer.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.data.Monster;
import com.fullwall.MonsterTamer.data.Tamer;
import com.fullwall.MonsterTamer.sk89q.Command;
import com.fullwall.MonsterTamer.sk89q.CommandContext;
import com.fullwall.MonsterTamer.sk89q.CommandPermissions;
import com.fullwall.MonsterTamer.utils.EntityUtils;
import com.fullwall.MonsterTamer.utils.Messaging;
import com.fullwall.MonsterTamer.utils.StringUtils;

public class TargetCommands {

	@Command(
			aliases = { "target" },
			usage = "target [-p|-m] [name]",
			desc = "set selected monsters on a player",
			flags = "pm",
			modifiers = "",
			min = 1,
			max = 1)
	@CommandPermissions("monstertamer.player.target")
	public static void targetMonster(CommandContext args, Tamer tamer,
			Player player) {
		if (!tamer.hasSelected()) {
			player.sendMessage(ChatColor.GRAY
					+ "You don't have any monsters selected.");
			return;
		}
		if (args.getFlags().contains('p')) {
			String name = args.getString(0);
			Player target = matchPlayer(player, name);
			if (target == null)
				return;
			int count = tamer.setMonstersOn(target);
			if (count == 0)
				player.sendMessage(ChatColor.GRAY
						+ "You don't have any monsters nearby.");
			else {
				player.sendMessage(ChatColor.GREEN + "You sent "
						+ StringUtils.wrap(count) + " "
						+ StringUtils.pluralise("monster", count) + " after "
						+ StringUtils.wrap(target.getName()) + ".");
			}
		} else if (args.getFlags().contains('m')) {
			String name = StringUtils.capitalise(args.getString(0)
					.toLowerCase());
			if (!(name.equals("All")) && CreatureType.fromName(name) == null) {
				player.sendMessage(ChatColor.RED + "Incorrect monster type.");
				return;
			}
			CreatureType search = CreatureType.fromName(name);
			LivingEntity target = null;
			for (Entity entity : player.getNearbyEntities(5, 10, 5)) {
				if (entity instanceof LivingEntity
						&& (search == null || EntityUtils.getType(entity) == search)) {
					target = (LivingEntity) entity;
					break;
				}
			}
			int count = tamer.setMonstersOn(target, search);
			if (count == 0)
				player.sendMessage(ChatColor.RED
						+ "No nearby monsters found matching that type.");
			else {
				player.sendMessage(ChatColor.GREEN + "Set the target of "
						+ StringUtils.wrap(count + " ")
						+ StringUtils.pluralise("monster", count) + ".");
			}
			return;
		} else {
			Messaging.send(player, ChatColor.GRAY + "Invalid flags specified.");
		}
		return;
	}

	@Command(
			aliases = { "target" },
			usage = "target",
			desc = "cancel the targets of selected monsters",
			modifiers = "cancel",
			min = 2,
			max = 2)
	@CommandPermissions("monstertamer.player.target")
	public static void cancelTarget(CommandContext args, Tamer tamer,
			Player player) {
		if (!tamer.hasSelected()) {
			player.sendMessage(ChatColor.GRAY
					+ "You don't have any monsters selected.");
			return;
		}
		String name = StringUtils.capitalise(args.getString(0).toLowerCase());
		if (!(name.equals("All")) && CreatureType.fromName(name) == null) {
			player.sendMessage(ChatColor.RED + "Incorrect monster name.");
			return;
		}
		int count = 0;
		for (Monster monster : tamer.getSelected()) {
			if (monster.hasTarget()) {
				monster.setTarget(null);
				++count;
			}
		}
		player.sendMessage(ChatColor.GREEN + "Cancelled the targets of "
				+ StringUtils.wrap(count) + " "
				+ StringUtils.pluralise("monsters", count) + ".");
	}

	private static Player matchPlayer(Player player, String name) {
		List<Player> players = Bukkit.getServer().matchPlayer(name);
		if (players.size() == 0) {
			player.sendMessage(ChatColor.GRAY + "Couldn't match any players.");
			return null;
		} else if (players.size() > 1) {
			player.sendMessage(ChatColor.GRAY + "Matched more than one player.");
			return null;
		}
		return players.get(0);
	}
}
