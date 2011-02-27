package com.fullwall.MonsterTamer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Name for Bukkit
 * 
 * @author fullwall
 */
public class MonsterTamer extends JavaPlugin {

	public final PlayerListen pl = new PlayerListen(this);
	public final EntityListen el = new EntityListen(this);

	private static final String codename = "Companions";
	public static Logger log = Logger.getLogger("Minecraft");

	// what monster the player is currently catching.
	public static HashMap<String, String> playerCatching = new HashMap<String, String>();
	// player name, arraylist of monsters, item caught with - grouped in twos
	public static HashMap<String, ArrayList<String>> playerMonsters = new HashMap<String, ArrayList<String>>();
	// name, catch rate
	public static HashMap<String, Double> monsterChances = new HashMap<String, Double>();
	// id, bonus
	public static HashMap<String, Double> catchItems = new HashMap<String, Double>();
	public static HashMap<String, ArrayList<String>> friends = new HashMap<String, ArrayList<String>>();
	public static Integer limit = 50;

	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGED, el, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, el, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pl, Priority.Normal, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		Permission.initialize(getServer());
		readSettings();
		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well.
		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") loaded");

	}

	public void onDisable() {
		writeUsers();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") disabled");
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();

		if (!(sender instanceof Player)) {
			sender.sendMessage("[MonsterTamer]: Must be ingame to use this command.");
			return true;
		}
		Player p = (Player) sender;
		commandName = "/" + commandName;
		String parameters = "";
		for (String i : args) {
			parameters += " " + i;
		}
		String fullCommand = commandName + parameters;
		String[] split = fullCommand.split(" ");
		if (split.length == 2 && split[0].equals("/target")
				&& split[1].equalsIgnoreCase("cancel")) {
			cancelTarget(p, split);
			return true;
		} else if (split.length == 2 && split[0].equalsIgnoreCase("/release")) {
			releaseMonster(p, split);
			return true;
		} else if (split.length == 1
				&& (split[0].equalsIgnoreCase("/monsters") || split[0]
						.equalsIgnoreCase("/ms"))) {
			listMonsters(p, split);
			return true;
		} else if (split.length == 3 && (split[0].equalsIgnoreCase("/target"))
				&& split[1].length() >= 1) {
			targetMonster(p, split);
			return true;
		}
		return false;
	}

	private void targetMonster(Player p, String[] split) {
		List<Player> players = this.getServer().matchPlayer(split[1]);
		Player target = null;
		if (players.size() == 0) {
			p.sendMessage("§cNo matching players were found.");
			return;
		} else if (players.size() != 1) {
			p.sendMessage("§cMatched more than one player!  Be more specific!");
			return;
		} else {
			target = players.get(0);
		}
		String name = split[2].toLowerCase();
		if (PlayerListen.checkMonsters(name).isEmpty()) {
			p.sendMessage(ChatColor.RED + "Incorrect monster name.");
			return;
		}
		List<Entity> entityList = p.getWorld().getEntities();
		Location loc = p.getLocation();
		int count = 0;
		LivingEntity le;
		for (Entity entity : entityList) {
			if (entity instanceof LivingEntity && entity instanceof Creature) {
				le = (LivingEntity) entity;
				if (PlayerListen.checkMonsters(le).equals(name)
						&& ((entity.getLocation().getX() <= loc.getX() + 10 && entity
								.getLocation().getX() >= loc.getX() - 10)
								&& (entity.getLocation().getY() >= loc.getY() - 10 && entity
										.getLocation().getY() <= loc.getY() + 10) && (entity
								.getLocation().getZ() >= loc.getZ() - 10 && entity
								.getLocation().getZ() <= loc.getZ() + 10))
						&& MonsterTamer.friends.containsKey(p.getName())
						&& MonsterTamer.friends.get(p).contains(
								"" + entity.getEntityId())) {
					Creature c = (Creature) entity;
					c.setTarget(target);
					count += 1;
				}
			}
		}
		if (count == 0)
			p.sendMessage(ChatColor.GRAY
					+ "You didn't have any friendly monsters nearby.");
		else if (count == 1)
			p.sendMessage(ChatColor.GREEN + "You sent " + count + " " + name
					+ " after " + target.getName() + "!");
		else
			p.sendMessage(ChatColor.GREEN + "You sent " + count + " " + name
					+ "s after " + target.getName() + "!");
		return;
	}

	private void listMonsters(Player p, String[] split) {
		if (!(Permission.checkMonsters(p))) {
			p.sendMessage(ChatColor.RED
					+ "You don't have permission to use that command.");
			return;
		}
		// if we don't have any monsters
		if (MonsterTamer.playerMonsters.get(p.getName()) == null
				|| MonsterTamer.playerMonsters.get((p.getName())).size() == 0
				|| MonsterTamer.playerMonsters.get((p.getName())).get(0)
						.isEmpty()) {
			p.sendMessage(ChatColor.GRAY + "You don't have any monsters yet!");
			return;
		} else {
			ArrayList<String> array = MonsterTamer.playerMonsters.get(p
					.getName());
			p.sendMessage(ChatColor.GOLD + "A list of your current monsters.");
			p.sendMessage(ChatColor.AQUA + "------------------------------");
			int i2 = 0;
			String monsterName = "";
			String name = "";
			for (int i = 0; i < array.size(); ++i) {
				if (i2 == 0)
					monsterName = array.get(i);
				else if (i2 == 1) {
					name = array.get(i);
				}
				if (!name.isEmpty() && !monsterName.isEmpty() && i2 == 1) {
					Material mat = Material.matchMaterial(name);
					if (mat != null) {
						// String materialName =
						// Material.matchMaterial(name).name().replace(
						// Material.matchMaterial(name).name().substring(1),
						// Material.matchMaterial(name).name().substring(1).toLowerCase());
						p.sendMessage(ChatColor.GREEN + "A " + ChatColor.YELLOW
								+ monsterName + ChatColor.GREEN
								+ ", caught with a " + ChatColor.RED
								+ mat.name() + ChatColor.GREEN + ".");
					}
				}
				if (i2 + 1 > 1) {
					i2 = 0;
					monsterName = "";
					name = "";
				} else
					i2 += 1;

			}
			p.sendMessage(ChatColor.AQUA + "------------------------------");
			return;
		}
	}

	private void releaseMonster(Player p, String[] split) {
		if (!(Permission.release(p))) {
			p.sendMessage(ChatColor.RED
					+ "You don't have permission to use that command.");
			return;
		}
		int id = Integer.parseInt(split[1]);
		ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());

		if (!(id < array.size())) {
			p.sendMessage(ChatColor.GRAY + "You don't have that many monsters!");
			return;
		}
		int caughtWithID = Integer.parseInt(array.get(id - 1));
		PlayerInventory pi = p.getInventory();
		if (pi.contains(caughtWithID, 1)) {
			pi.getItem(pi.first(caughtWithID)).setAmount(
					pi.getItem(pi.first(caughtWithID)).getAmount() - 1);
			PlayerListen.spawnFromLocation(p, caughtWithID);
		} else {
			p.sendMessage(ChatColor.GRAY
					+ "You don't have any of the item you caught that monster with.");
			return;
		}
	}

	private void cancelTarget(Player p, String[] split) {
		String name = split[2].toLowerCase();
		if (PlayerListen.checkMonsters(name).isEmpty()) {
			p.sendMessage(ChatColor.RED + "Incorrect monster name.");
			return;
		}
		List<Entity> entityList = p.getWorld().getEntities();
		Location loc = p.getLocation();
		int count = 0;
		LivingEntity le;
		for (Entity entity : entityList) {
			if (entity instanceof LivingEntity && entity instanceof Creature) {
				le = (LivingEntity) entity;
				if (PlayerListen.checkMonsters(le).equals(name)
						&& ((entity.getLocation().getX() <= loc.getX() + 10 && entity
								.getLocation().getX() >= loc.getX() - 10)
								&& (entity.getLocation().getY() >= loc.getY() - 10 && entity
										.getLocation().getY() <= loc.getY() + 10) && (entity
								.getLocation().getZ() >= loc.getZ() - 10 && entity
								.getLocation().getZ() <= loc.getZ() + 10))
						&& MonsterTamer.friends.containsKey(p.getName())
						&& MonsterTamer.friends.get(p.getName()).contains(
								"" + entity.getEntityId())) {
					Creature c = (Creature) entity;
					c.setTarget(null);
					count += 1;
				}
			}
		}
	}

	public void readSettings() {

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(
					"plugins/MonsterTamer/MonsterTamer.properties"));
		} catch (FileNotFoundException e) {
			log.info("[MonsterTamer]: Couldn't find properties file.");
		} catch (IOException e) {
			log.info("[MonsterTamer]: Couldn't load properties file.");
		}
		if (props.containsKey("items")) {
			String[] split = props.getProperty("items").split(";");
			for (String i : split) {
				String[] newSplit = i.split(":");
				catchItems.put(newSplit[0], Double.parseDouble(newSplit[1]));
			}
		}
		if (props.containsKey("limit")) {
			limit = Integer.parseInt(props.getProperty("limit"));
		}
		if (props.containsKey("monsters")) {
			String[] split = props.getProperty("monsters").split(";");
			for (String i : split) {
				String[] newSplit = i.split(":");
				monsterChances
						.put(newSplit[0], Double.parseDouble(newSplit[1]));
			}
		}
		props.clear();
		try {
			FileReader input = new FileReader(
					"plugins/MonsterTamer/MonsterTamer.users");
			BufferedReader bufRead = new BufferedReader(input);
			String line;
			line = bufRead.readLine();
			while (line != null) {
				if (line.length() == 0) {
					line = bufRead.readLine();
					continue;
				}
				if (line.isEmpty()) {
					line = bufRead.readLine();
					continue;
				}
				if (line.startsWith("#")) {
					line = bufRead.readLine();
					continue;
				}
				if (!line.contains("=")) {
					line = bufRead.readLine();
					continue;
				}

				int equals = line.indexOf("=");

				int commentIndex = line.length();
				String key = line.substring(0, equals).trim();

				if (key.equals("")) {
					line = bufRead.readLine();
					continue;
				}

				String value = line.substring(equals + 1, commentIndex).trim();
				String[] values = value.split(";");
				ArrayList<String> array = new ArrayList<String>();
				for (int i = 0; i < values.length; ++i) {

					String[] player = values[i].split(",");

					for (String i4 : player) {
						array.add(i4);
					}

				}
				MonsterTamer.playerMonsters.put(key, array);
				line = bufRead.readLine();
			}
			bufRead.close();

		} catch (IOException e) {
			log.info("[MonsterTamer]: Error reading MonsterTamer.users.");
		}

	}

	public static void writeUsers() {
		Properties props = new Properties();
		String str = "";
		int i3 = 0;

		// get rid of whitespace
		for (Map.Entry<String, ArrayList<String>> entry : playerMonsters
				.entrySet()) {
			Iterator<String> it = entry.getValue().iterator();
			while (it.hasNext()) {
				String itn = it.next();
				if (itn.contains(" "))
					itn.replace(" ", "");
				if (itn.isEmpty())
					it.remove();
			}
		}
		String first;
		String second;
		// switch around errored monsters
		for (Map.Entry<String, ArrayList<String>> entry : playerMonsters
				.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				first = entry.getValue().get(i);
				if (i != entry.getValue().size() - 1) {
					second = entry.getValue().get(i + 1);
				} else
					break;
				if (Character.isDigit(first.charAt(0))
						&& !Character.isDefined(second.charAt(0))) {
					entry.getValue().add(i, second);
					entry.getValue().add(i + 1, first);
				}

			}
		}

		try {
			for (Map.Entry<String, ArrayList<String>> entry : playerMonsters
					.entrySet()) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					i3 += 1;
					str += entry.getValue().get(i);
					if (i3 != 2)
						str += ",";
					else {
						i3 = 0;
						str += ";";
					}
				}
				props.setProperty(entry.getKey(), str);

			}
			props.store(new FileOutputStream(
					"plugins/MonsterTamer/MonsterTamer.users"), null);
		} catch (FileNotFoundException e) {
			log.info("[MonsterTamer]: Couldn't find MonsterTamer.users.");
		} catch (IOException e) {
			log.info("[MonsterTamer]: Couldn't write to MonsterTamer.users.");
		}
	}
}