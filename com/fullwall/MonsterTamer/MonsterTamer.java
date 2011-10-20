package com.fullwall.MonsterTamer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.fullwall.MonsterTamer.commands.ManagementCommands;
import com.fullwall.MonsterTamer.commands.MovementCommands;
import com.fullwall.MonsterTamer.commands.TargetCommands;
import com.fullwall.MonsterTamer.data.DataHandler;
import com.fullwall.MonsterTamer.listeners.EntityListen;
import com.fullwall.MonsterTamer.listeners.PlayerListen;
import com.fullwall.MonsterTamer.listeners.WorldListen;
import com.fullwall.MonsterTamer.sk89q.CommandPermissionsException;
import com.fullwall.MonsterTamer.sk89q.CommandUsageException;
import com.fullwall.MonsterTamer.sk89q.MissingNestedCommandException;
import com.fullwall.MonsterTamer.sk89q.MonsterTamerCommandsManager;
import com.fullwall.MonsterTamer.sk89q.RequirementMissingException;
import com.fullwall.MonsterTamer.sk89q.UnhandledCommandException;
import com.fullwall.MonsterTamer.sk89q.WrappedCommandException;
import com.fullwall.MonsterTamer.utils.Messaging;

/**
 * Name for Bukkit
 * 
 * @author fullwall
 */
public class MonsterTamer extends JavaPlugin {
	private static PlayerListen playerListener = new PlayerListen();
	private static EntityListen entityListener = new EntityListen();
	private static WorldListen worldListener = new WorldListen();

	public static MonsterTamerCommandsManager<Player> commands = new MonsterTamerCommandsManager<Player>();

	public static TickTask handler;
	public static MonsterTamer plugin;
	private static final String codename = "Reborn";

	@Override
	public void onEnable() {
		plugin = this;
		commands.register(TargetCommands.class);
		commands.register(MovementCommands.class);
		commands.register(ManagementCommands.class);

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.CHUNK_UNLOAD, worldListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.CHUNK_LOAD, worldListener, Priority.Normal,
				this);
		PluginDescriptionFile pdfFile = this.getDescription();
		DataHandler.loadSettings();
		DataHandler.loadUsers();

		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new TickTask(), 5, 1);
		Messaging.log("version [" + pdfFile.getVersion() + "] (" + codename
				+ ") loaded");

	}

	@Override
	public void onDisable() {
		DataHandler.saveAll();
		PluginDescriptionFile pdfFile = this.getDescription();
		Messaging.log("version [" + pdfFile.getVersion() + "] (" + codename
				+ ") disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Must be ingame to perform this command.");
			return true;
		}
		Player player = (Player) sender;
		try {
			// must put command into split.
			String[] split = new String[args.length + 1];
			System.arraycopy(args, 0, split, 1, args.length);
			split[0] = command.getName().toLowerCase();

			String modifier = "";
			if (args.length > 0)
				modifier = args[0];

			// No command found!
			if (!commands.hasCommand(split[0], modifier)) {
				return false;
			}

			try {
				commands.execute(split, player,
						MonsterManager.getTamer(player), player);
			} catch (CommandPermissionsException e) {
				Messaging.sendError(player, Constants.noPermissionsMessage);
			} catch (MissingNestedCommandException e) {
				Messaging.sendError(player, e.getUsage());
			} catch (CommandUsageException e) {
				Messaging.sendError(player, e.getMessage());
				Messaging.sendError(player, e.getUsage());
			} catch (RequirementMissingException e) {
				Messaging.sendError(player, e.getMessage());
			} catch (WrappedCommandException e) {
				throw e.getCause();
			} catch (UnhandledCommandException e) {
				return false;
			}
		} catch (NumberFormatException e) {
			Messaging.sendError(player, "That is not a valid number.");
		} catch (Throwable excp) {
			excp.printStackTrace();
			Messaging.sendError(player,
					"Please report this error: [See console]");
			Messaging.sendError(player,
					excp.getClass().getName() + ": " + excp.getMessage());
		}
		return true;
	}

}