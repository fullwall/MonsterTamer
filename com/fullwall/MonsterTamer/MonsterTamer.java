package com.fullwall.MonsterTamer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
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
	public final EntityListen entityListener = new EntityListen(this);
	public final WorldListen wl = new WorldListen(this);

	private static final String codename = "Commander";
	public static final String noPermissionsMessage = ChatColor.RED
			+ "You don't have permission to use that command!";
	public static Logger log = Logger.getLogger("Minecraft");

	// what monster the player is currently catching.
	public static ConcurrentHashMap<String, String> playerCatching = new ConcurrentHashMap<String, String>();
	// player name, list of monster names, item caught with (grouped in twos)
	public static ConcurrentHashMap<String, ArrayList<String>> playerMonsters = new ConcurrentHashMap<String, ArrayList<String>>();
	// name, catch rate
	public static ConcurrentHashMap<String, Double> monsterChances = new ConcurrentHashMap<String, Double>();
	// id, bonus
	public static ConcurrentHashMap<String, Double> catchItems = new ConcurrentHashMap<String, Double>();
	// player name, list of monster entity ids
	public static ConcurrentHashMap<String, ArrayList<String>> friends = new ConcurrentHashMap<String, ArrayList<String>>();
	// entity id, attacking name
	public static ConcurrentHashMap<String, String> targets = new ConcurrentHashMap<String, String>();
	// player name, entities
	public static ConcurrentHashMap<String, ArrayList<Integer>> followers = new ConcurrentHashMap<String, ArrayList<Integer>>();
	// player name, entities
	public static ConcurrentHashMap<String, ArrayList<Integer>> waiters = new ConcurrentHashMap<String, ArrayList<Integer>>();
	// player name, entity IDs
	public static ConcurrentHashMap<String, ArrayList<Integer>> selectedMonsters = new ConcurrentHashMap<String, ArrayList<Integer>>();
	// entity ID, location to move to
	public static ConcurrentHashMap<Integer, Location> locationMovers = new ConcurrentHashMap<Integer, Location>();
	// list of friendly entity ids
	public static ArrayList<String> friendlies = new ArrayList<String>();
	// limit of monsters
	public static int limit = 50;
	public static int selectTool = 280;
	public static boolean stopDespawning = true;
	public static boolean consumeItems = true;
	public static TickTask handler;

	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ANIMATION, pl, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pl, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, pl, Priority.Normal, this);
		// pm.registerEvent(Event.Type.CHUNK_UNLOADED, wl, Priority.Normal,
		// this);
		// pm.registerEvent(Event.Type.CHUNK_LOADED, wl, Priority.Normal, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		Permission.initialize(getServer());
		readSettings();
		handler = new TickTask(this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, handler, 5,
				1);
		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") loaded");

	}

	public void onDisable() {
		writeUsers();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") disabled");
	}

	@Override
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
		if (split.length == 3 && split[0].equals("/target")
				&& split[1].equalsIgnoreCase("cancel")) {
			cancelTarget(p, split);
			return true;
		} else if (split.length == 2 && split[0].equalsIgnoreCase("/release")) {
			if (Permission.release(p)) {
				releaseMonster(p, split);
			} else
				p.sendMessage(noPermissionsMessage);
			return true;
		} else if (split.length == 1
				&& (split[0].equalsIgnoreCase("/monsters") || split[0]
						.equalsIgnoreCase("/ms"))) {
			listMonsters(p, split);
			return true;
		} else if (split.length == 3 && (split[0].equalsIgnoreCase("/target"))
				&& split[1].length() >= 1
				&& !split[1].equalsIgnoreCase("cancel")) {
			if (Permission.target(p)) {
				targetMonster(p, split);
			} else
				p.sendMessage(noPermissionsMessage);
			return true;
		} else if (split.length == 1 && (split[0].equalsIgnoreCase("/whistle"))) {
			if (Permission.whistle(p))
				whistle(p);
			else
				p.sendMessage(noPermissionsMessage);

			return true;
		} else if (split.length == 3 && split[0].equalsIgnoreCase("/wait")
				&& split[1].equalsIgnoreCase("cancel")) {
			if (Permission.wait(p))
				removeWaiters(p, args);
			else
				p.sendMessage(noPermissionsMessage);
			return true;
		} else if (split.length == 3 && split[0].equalsIgnoreCase("/follow")
				&& split[1].equalsIgnoreCase("cancel")) {
			if (Permission.follow(p))
				removeFollowers(p, args);
			else
				p.sendMessage(noPermissionsMessage);
			return true;
		} else if (split[0].equalsIgnoreCase("/follow")) {
			if (Permission.follow(p)) {
				if (split.length == 2) {
					if (addFollowers(p, args))
						p.sendMessage(ChatColor.GREEN + "PHWEET! Your "
								+ ChatColor.YELLOW + args[0] + ChatColor.GREEN
								+ "s start following you.");
					else
						p.sendMessage(ChatColor.RED
								+ "Couldn't find any monsters of that type.");
				} else if (split.length == 1) {
					addFollowersFromSelected(p, args);
					p.sendMessage(ChatColor.GREEN
							+ "PHWEET! Your selected monsters start following you.");
				}
			} else
				p.sendMessage(noPermissionsMessage);
			return true;
		} else if (split.length == 2 && split[0].equalsIgnoreCase("/wait")) {
			if (Permission.wait(p)) {
				if (addWaiters(p, args))
					p.sendMessage(ChatColor.GREEN
							+ "Your "
							+ args[0]
							+ "s stay where they are, looking a little forlorn.");
				else
					p.sendMessage(ChatColor.RED
							+ "Couldn't find any monsters of that type.");
			} else
				p.sendMessage(noPermissionsMessage);
			return true;
		} else if (split.length == 2 && split[0].equalsIgnoreCase("/select")) {
			if (Permission.select(p)) {
				selectMonsters(p, args, true);
			} else
				p.sendMessage(noPermissionsMessage);
			return true;
		} else if (split.length == 2 && split[0].equalsIgnoreCase("/deselect")) {
			if (Permission.select(p)) {
				selectMonsters(p, args, false);
			} else
				p.sendMessage(noPermissionsMessage);
			return true;
		}
		return false;
	}

	private void selectMonsters(Player player, String[] args, boolean remove) {
		int count = 0;
		ArrayList<Integer> selected = new ArrayList<Integer>();
		if (selectedMonsters.get(player.getName()) != null)
			selected = selectedMonsters.get(player.getName());
		for (LivingEntity livingEntity : player.getWorld().getLivingEntities()) {
			if (livingEntity instanceof Creature
					&& (args[0].equals("all") || EntityListen.checkMonsters(
							livingEntity).equals(args[0]))
					&& friends.containsKey(player.getName())
					&& friends.get(player.getName()).contains(
							"" + livingEntity.getEntityId())) {
				if (remove) {
					selected.add(livingEntity.getEntityId());
					addFollower(player, livingEntity, false);
				} else if (selected.indexOf(livingEntity.getEntityId()) != -1) {
					selected.remove(selected.indexOf(livingEntity.getEntityId()));
					addFollower(player, livingEntity, true);
				}
				count += 1;
			}
		}
		if (count > 0) {
			selectedMonsters.put(player.getName(), selected);
			if (args[0].equals("all"))
				player.sendMessage(ChatColor.GREEN
						+ "Selected all of your monsters (" + count + ").");
			else
				player.sendMessage(ChatColor.GREEN + "Selected " + count
						+ " monster(s) of type " + args[0] + ".");
		} else {
			player.sendMessage(ChatColor.RED
					+ "You don't have any monsters of that type to select!");
		}

	}

	public static void addFollower(Player player, Integer id, boolean remove) {
		ArrayList<Integer> array = MonsterTamer.followers.get(player.getName());
		if (array == null)
			array = new ArrayList<Integer>();
		if (!remove) {
			if (array.indexOf(id) == -1)
				array.add(id);
		} else {
			if (array.indexOf(id) != -1)
				array.remove(array.indexOf(id));
		}
		MonsterTamer.followers.put(player.getName(), array);
	}

	public static void addFollower(Player p, LivingEntity e, boolean remove) {
		ArrayList<Integer> array = MonsterTamer.followers.get(p.getName());
		if (array == null)
			array = new ArrayList<Integer>();
		if (!remove) {
			if (array.indexOf(e.getEntityId()) == -1)
				array.add(e.getEntityId());
		} else {
			if (array.indexOf(e.getEntityId()) != -1)
				array.remove(array.indexOf(e.getEntityId()));
		}
		MonsterTamer.followers.put(p.getName(), array);
	}

	private void removeWaiters(Player p, String[] args) {
		int count = 0;
		for (Entry<String, ArrayList<Integer>> i : MonsterTamer.waiters
				.entrySet()) {
			if (!i.getKey().equalsIgnoreCase(p.getName()))
				continue;
			for (LivingEntity e : p.getWorld().getLivingEntities()) {
				if (e instanceof Creature
						&& i.getValue().contains(e.getEntityId())
						&& EntityListen.checkMonsters(e).equals(args[1])
						&& friends.containsKey(p.getName())
						&& friends.get(p.getName()).contains(
								"" + e.getEntityId())) {
					int index = i.getValue().indexOf(e.getEntityId());
					if (index != -1) {
						i.getValue().remove(index);
						count += 1;
					}
				}
			}
			if (count == 0) {
				p.sendMessage(ChatColor.GRAY
						+ "You don't have any waiters yet!");
			} else
				p.sendMessage(ChatColor.GREEN + "Your " + args[1]
						+ "s stopped waiting.");
			return;
		}
	}

	private void removeFollowers(Player p, String[] args) {
		int count = 0;
		for (Entry<String, ArrayList<Integer>> i : MonsterTamer.followers
				.entrySet()) {
			if (!i.getKey().equalsIgnoreCase(p.getName()))
				continue;
			for (LivingEntity e : p.getWorld().getLivingEntities()) {
				if (e instanceof Creature
						&& i.getValue().contains(e.getEntityId())
						&& EntityListen.checkMonsters(e).equals(args[1])
						&& friends.containsKey(p.getName())
						&& friends.get(p.getName()).contains(
								"" + e.getEntityId())) {
					int index = i.getValue().indexOf(e.getEntityId());
					if (index != -1) {
						i.getValue().remove(index);
						count += 1;
					}
				}
			}
			if (count == 0) {
				p.sendMessage(ChatColor.GRAY
						+ "You don't have any followers yet!");
			} else
				p.sendMessage(ChatColor.GREEN + "Your " + args[1]
						+ "s stopped following you.");
			return;
		}
	}

	private boolean addFollowers(Player p, String[] args) {
		boolean found = false;
		for (LivingEntity le : p.getWorld().getLivingEntities()) {
			if (le instanceof Creature
					&& (args[0].equals("all") || EntityListen.checkMonsters(le)
							.equals(args[0]))
					&& friends.containsKey(p.getName())
					&& friends.get(p.getName()).contains("" + le.getEntityId())) {
				ArrayList<Integer> array = followers.get(p.getName());
				if (array == null)
					array = new ArrayList<Integer>();
				array.add(le.getEntityId());
				followers.put(p.getName(), array);
				found = true;
			}

		}
		return found;
	}

	private void addFollowersFromSelected(Player p, String[] args) {
		if (selectedMonsters.get(p.getName()) != null
				&& selectedMonsters.get(p.getName()).size() > 0)
			for (LivingEntity entity : p.getWorld().getLivingEntities()) {
				for (Integer ID : selectedMonsters.get(p.getName())) {
					if (entity.getEntityId() == ID) {
						addFollower(p, ID, false);
					}
				}
			}
	}

	private boolean addWaiters(Player p, String[] args) {
		boolean found = false;
		for (LivingEntity le : p.getWorld().getLivingEntities()) {
			if (le instanceof Creature
					&& (args[0].equals("all") || EntityListen.checkMonsters(le)
							.equals(args[0]))
					&& friends.containsKey(p.getName())
					&& friends.get(p.getName()).contains("" + le.getEntityId())) {
				ArrayList<Integer> array = waiters.get(p.getName());
				if (array == null)
					array = new ArrayList<Integer>();
				array.add(le.getEntityId());
				waiters.put(p.getName(), array);
				found = true;
			}
		}
		return found;
	}

	private void whistle(Player p) {
		for (LivingEntity le : p.getWorld().getLivingEntities()) {
			if (friendlies.contains("" + le.getEntityId())) {
				locationMovers.put(le.getEntityId(), p.getLocation());
			}
		}
	}

	private void targetMonster(Player p, String[] split) {
		List<Player> players = this.getServer().matchPlayer(split[1]);
		Player target = null;
		if (players.size() == 0) {
			monsterTarget(split, p);
			return;
		} else if (players.size() != 1) {
			p.sendMessage("�cMatched more than one player!  Be more specific!");
			return;
		} else {
			target = players.get(0);
		}
		playerTarget(split, p, target);
	}

	private void playerTarget(String[] split, Player p, Player target) {
		String name = split[2].toLowerCase();
		if (PlayerListen.checkMonsters(name).isEmpty()) {
			p.sendMessage(ChatColor.RED + "Incorrect monster name.");
			return;
		}
		List<LivingEntity> entityList = p.getWorld().getLivingEntities();
		Location loc = p.getLocation();
		int count = 0;
		LivingEntity le;
		if (friends.get(p.getName()) == null) {
			p.sendMessage(ChatColor.GRAY
					+ "You haven't released any friendly monsters yet!");
			return;
		}
		for (LivingEntity entity : entityList) {
			if (entity instanceof Creature) {
				le = entity;
				if ((name.equals("all") || EntityListen.checkMonsters(le)
						.equals(name))
						&& entity.getEntityId() != p.getEntityId()
						&& checkRange(entity.getLocation(), loc, 10)
						&& friends.containsKey(p.getName())
						&& friends.get(p.getName()).contains(
								"" + entity.getEntityId())) {
					Creature c = (Creature) entity;
					c.setTarget(target);
					targets.put("" + c.getEntityId(), target.getName());
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

	public static boolean checkRange(Location loc, Location loc2, double range) {
		range = range / 2;
		return ((loc.getX() <= loc2.getX() + range && loc.getX() >= loc2.getX()
				- range)
				&& (loc.getY() >= loc2.getY() - range && loc.getY() <= loc2
						.getY() + range) && (loc.getZ() >= loc2.getZ() - range && loc
				.getZ() <= loc2.getZ() + range));
	}

	private void monsterTarget(String[] split, Player p) {
		List<LivingEntity> entityList = p.getWorld().getLivingEntities();
		Location loc = p.getLocation();
		LivingEntity le;
		if (friends.get(p.getName()) == null) {
			p.sendMessage(ChatColor.GRAY
					+ "You haven't released any friendly monsters yet!");
			return;
		}
		int count = 0;
		// target fullwall all
		for (LivingEntity entity : entityList) {
			if (entity instanceof Creature) {
				le = entity;
				if ((split[2].equals("all") || EntityListen.checkMonsters(le)
						.equals(split[2]))
						&& checkRange(entity.getLocation(), loc, 10)
						&& friends.containsKey(p.getName())
						&& friends.get(p.getName()).contains(
								"" + entity.getEntityId())) {
					Creature c = (Creature) entity;
					for (LivingEntity e : entityList) {
						if (e instanceof Creature
								&& entity.getEntityId() != p.getEntityId()
								&& (split[1].equals("all") || EntityListen
										.checkMonsters(e).equals(split[1]))
								&& ((e.getLocation().getX() <= loc.getX() + 20 && e
										.getLocation().getX() >= loc.getX() - 20)
										&& (e.getLocation().getY() >= loc
												.getY() - 20 && e.getLocation()
												.getY() <= loc.getY() + 20) && (e
										.getLocation().getZ() >= loc.getZ() - 20 && e
										.getLocation().getZ() <= loc.getZ() + 20))
								&& !friends.get(p.getName()).contains(
										"" + e.getEntityId())) {
							c.setTarget(e);
							targets.put("" + c.getEntityId(), ""
									+ c.getTarget().getEntityId());
							count = 1;
							break;
						}
					}
					break;
				}
			}
		}
		if (count != 1)
			p.sendMessage("�cNo matching monster types were found.");
		else
			p.sendMessage(ChatColor.GREEN + "Targeted a " + split[1]);
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
		} else if (split.length == 1) {
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
		} else if (split.length == 2 && split[1].equals("help")) {
			p.sendMessage(ChatColor.GOLD + "MonsterTamer (by fullwall) Help.");
			p.sendMessage(ChatColor.AQUA + "------------------------------");
			p.sendMessage(ChatColor.GREEN
					+ "/monsters|ms - displays a list of your current monsters.");
			p.sendMessage(ChatColor.GREEN
					+ "/target [playername] [mobname]|all - makes all friendly monsters near you attack that player.");
			p.sendMessage(ChatColor.GREEN
					+ "/target cancel [mobname]|all - cancels the targets of all friendly monsters near you.");
			p.sendMessage(ChatColor.GREEN
					+ "/release [slot ID|monster type]- release the monster in the specified slot or type. NOTE: /release 0 is the FIRST monster on the list.");
			p.sendMessage(ChatColor.GREEN
					+ "/whistle - commands all of your monsters to come to you.");
			p.sendMessage(ChatColor.GREEN
					+ "/follow [mobtype]|all - commands all friendly monsters of that type to follow you.");
			p.sendMessage(ChatColor.GREEN
					+ "/follow cancel [mobtype]|all - stops all friendly monsters of that type from following you.");
			p.sendMessage(ChatColor.GREEN
					+ "/wait [mobtype]|all - commands all friendly monsters of that type to wait at their position.");
			p.sendMessage(ChatColor.GREEN
					+ "/wait cancel [mobtype]|all - stops all friendly monsters of that type from waiting where they are.");
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
		int id = 0;
		try {
			if (!Character.isDigit(split[1].charAt(0)))
				throw new Exception();
			id = Integer.valueOf(split[1]);
		} catch (Exception ex) {
			String name = split[1];
			if (PlayerListen.checkMonsters(name).equals("")) {
				p.sendMessage(ChatColor.GRAY + "Invalid monster name.");
				return;
			}
			ArrayList<String> array = MonsterTamer.playerMonsters.get(p
					.getName());
			int index;
			if (array.contains(name)) {
				index = array.indexOf(name);
				int caughtWithID;
				if (id % 2 == 0)
					caughtWithID = Integer.parseInt(array.get(index + 1));
				else
					caughtWithID = Integer.parseInt(array.get(index));
				PlayerInventory pi = p.getInventory();
				if (pi.contains(caughtWithID, 1)) {
					if (consumeItems)
						pi.getItem(pi.first(caughtWithID))
								.setAmount(
										(pi.getItem(pi.first(caughtWithID))
												.getAmount() - 1));
					PlayerListen.spawnFromLocation(p, caughtWithID);
				} else {
					p.sendMessage(ChatColor.GRAY
							+ "You don't have any of the item you caught that monster with.");
					return;
				}
			} else {
				p.sendMessage(ChatColor.GRAY
						+ "You don't have any monsters of that type.");
				return;
			}
			return;
		}
		ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());

		if (id >= (array.size() / 2)) {
			p.sendMessage(ChatColor.GRAY + "You don't have that many monsters!");
			return;
		}
		if (id == -1) {
			p.sendMessage("Invalid slot ID.");
		}
		int caughtWithID = 0;
		if (id % 2 == 0)
			caughtWithID = Integer.parseInt(array.get(id + 1));
		else
			caughtWithID = Integer.parseInt(array.get(id));
		PlayerInventory pi = p.getInventory();
		if (pi.contains(caughtWithID, 1)) {
			if (consumeItems)
				pi.getItem(pi.first(caughtWithID)).setAmount(
						(pi.getItem(pi.first(caughtWithID)).getAmount() - 1));
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
		Location loc = p.getLocation();
		int count = 0;
		LivingEntity le;
		for (LivingEntity entity : p.getWorld().getLivingEntities()) {
			if (entity instanceof Creature) {
				le = (LivingEntity) entity;
				if ((name.equals("all") || EntityListen.checkMonsters(le)
						.equals(name))
						&& checkRange(entity.getLocation(), loc, 10)
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
		if (props.containsKey("stop-despawning"))
			stopDespawning = Boolean.parseBoolean(props
					.getProperty("stop-despawning"));
		if (props.containsKey("consume-items"))
			consumeItems = Boolean.parseBoolean(props
					.getProperty("consume-items"));
		if (props.containsKey("select-tool"))
			selectTool = Integer.parseInt(props.getProperty("select-tool"));
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
		/*
		 * String first; String second; // switch around errored monsters for
		 * (Map.Entry<String, ArrayList<String>> entry : playerMonsters
		 * .entrySet()) { for (int i = 0; i < entry.getValue().size(); i++) {
		 * first = entry.getValue().get(i); if (i != entry.getValue().size() -
		 * 1) { second = entry.getValue().get(i + 1); } else break; if
		 * (Character.isDigit(first.charAt(0)) &&
		 * !Character.isDefined(second.charAt(0))) { entry.getValue().add(i,
		 * second); entry.getValue().add(i + 1, first); }
		 * 
		 * } }
		 */
		try {
			for (Map.Entry<String, ArrayList<String>> entry : playerMonsters
					.entrySet()) {
				str = "";
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