package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;

public class PlayerListen extends PlayerListener {
	private static MonsterTamer plugin;
	public static Timer t = new Timer();

	@SuppressWarnings("static-access")
	public PlayerListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	public void onPlayerCommand(PlayerChatEvent e) {
		String[] split = e.getMessage().split(" ");
		if (split.length == 3 && (split[0].equalsIgnoreCase("/target"))
				&& split[1].length() >= 1) {
			List<Player> players = plugin.getServer().matchPlayer(split[1]);
			Player target = null;
			if (players.size() == 0) {
				e.getPlayer().sendMessage("§cNo matching players were found.");
				e.setCancelled(true);
				return;
			} else if (players.size() != 1) {
				e.getPlayer().sendMessage(
						"§cMatched more than one player!  Be more specific!");
				e.setCancelled(true);
				return;
			} else {
				target = players.get(0);
			}
			String name = split[2].toLowerCase();
			if (checkMonsters(name).isEmpty()) {
				e.getPlayer().sendMessage(
						ChatColor.RED + "Incorrect monster name.");
				e.setCancelled(true);
				return;
			}
			List<Entity> entityList = e.getPlayer().getWorld().getEntities();
			Location loc = e.getPlayer().getLocation();
			int count = 0;
			LivingEntity le;
			for (Entity entity : entityList) {
				if (entity instanceof LivingEntity
						&& entity instanceof Creature) {
					le = (LivingEntity) entity;
					if (checkMonsters(le).equals(name)
							&& ((entity.getLocation().getX() <= loc.getX() + 10 && entity
									.getLocation().getX() >= loc.getX() - 10)
									&& (entity.getLocation().getY() >= loc
											.getY() - 10 && entity
											.getLocation().getY() <= loc.getY() + 10) && (entity
									.getLocation().getZ() >= loc.getZ() - 10 && entity
									.getLocation().getZ() <= loc.getZ() + 10))
							&& MonsterTamer.friends.containsKey(e.getPlayer()
									.getName())
							&& MonsterTamer.friends
									.get(e.getPlayer().getName()).contains(
											"" + entity.getEntityId())) {
						Creature c = (Creature) entity;
						c.setTarget(target);
						count += 1;
					}
				}
			}
			if (count == 0)
				e.getPlayer()
						.sendMessage(
								ChatColor.GRAY
										+ "You didn't have any friendly monsters nearby.");
			else if (count == 1)
				e.getPlayer().sendMessage(
						ChatColor.GREEN + "You sent " + count + " " + name
								+ " after " + target.getName() + "!");
			else
				e.getPlayer().sendMessage(
						ChatColor.GREEN + "You sent " + count + " " + name
								+ "s after " + target.getName() + "!");
		} else if (split.length == 1
				&& (split[0].equalsIgnoreCase("/monsters") || split[0]
						.equalsIgnoreCase("/ms"))) {
			if (!(Permission.checkMonsters(e.getPlayer()))) {
				e.getPlayer()
						.sendMessage(
								ChatColor.RED
										+ "You don't have permission to use that command.");
				return;
			}
			// if we don't have any monsters
			if (MonsterTamer.playerMonsters.get(e.getPlayer().getName()) == null
					|| MonsterTamer.playerMonsters.get(
							(e.getPlayer().getName())).size() == 0
					|| MonsterTamer.playerMonsters
							.get((e.getPlayer().getName())).get(0).isEmpty()) {
				e.getPlayer().sendMessage(
						ChatColor.GRAY + "You don't have any monsters yet!");

				return;
			} else {
				ArrayList<String> array = MonsterTamer.playerMonsters.get(e
						.getPlayer().getName());
				e.getPlayer().sendMessage(
						ChatColor.GOLD + "A list of your current monsters.");
				e.getPlayer().sendMessage(
						ChatColor.AQUA + "------------------------------");
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
							e.getPlayer().sendMessage(
									ChatColor.GREEN + "A " + ChatColor.YELLOW
											+ monsterName + ChatColor.GREEN
											+ ", caught with a "
											+ ChatColor.RED + mat.name()
											+ ChatColor.GREEN + ".");
						}
					}
					if (i2 + 1 > 1) {
						i2 = 0;
						monsterName = "";
						name = "";
					} else
						i2 += 1;

				}
				e.getPlayer().sendMessage(
						ChatColor.AQUA + "------------------------------");
				e.setCancelled(true);
			}
		}
	}

	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (Permission.check(e.getPlayer())) {
			Item id = e.getItemDrop();
			if (checkMaps(id)) {
				t.schedule(new RemindTask(e, id), 1250);
			}
		}
	}

	public static void spawnFromItemDrop(PlayerDropItemEvent e, Item id) {
		if (MonsterTamer.playerMonsters.get(e.getPlayer().getName()) == null
				|| ((MonsterTamer.playerMonsters.get(e.getPlayer().getName())
						.size()) < 2))
			return;
		Location loc = id.getLocation();
		String name = getName(id, e.getPlayer());
		if (name.isEmpty()) {
			return;
		}
		String item = "" + id.getItemStack().getTypeId();
		if (!isInArray(name, item, e.getPlayer()))
			return;
		id.remove();
		try {
			CreatureType ct = CreatureType.fromName(name);
			Creature creature = id.getWorld().spawnCreature(loc, ct);
			removeNameFromArray(name, item, e.getPlayer());
			e.getPlayer().sendMessage(
					ChatColor.LIGHT_PURPLE
							+ "You released your "
							+ ChatColor.YELLOW
							+ name
							+ ChatColor.LIGHT_PURPLE
							+ ". You have "
							+ ChatColor.GREEN
							+ +(MonsterTamer.playerMonsters.get(
									e.getPlayer().getName()).size() / 2)
							+ ChatColor.LIGHT_PURPLE + " monsters remaining.");
			if (Permission.friendly(e.getPlayer())) {
				addFriends(e.getPlayer(), creature);
			}

		} catch (Exception e1) {
			MonsterTamer.log.info("[MonsterTamer]: Error spawning monster.");

		}
		MonsterTamer.writeUsers();

	}

	public String checkMonsters(LivingEntity le) {
		String name = "";
		if (le instanceof Chicken) {
			name = "chicken";
		} else if (le instanceof Cow) {
			name = "cow";
		} else if (le instanceof Creeper) {
			name = "creeper";
		} else if (le instanceof Ghast) {
			name = "ghast";
		} else if (le instanceof Giant) {
			name = "giant";
		} else if (le instanceof Pig) {
			name = "pig";
		} else if (le instanceof PigZombie) {
			name = "pigzombie";
		} else if (le instanceof Sheep) {
			name = "sheep";
		} else if (le instanceof Skeleton) {
			name = "skeleton";
		} else if (le instanceof Slime) {
			name = "slime";
		} else if (le instanceof Spider) {
			name = "spider";
		} else if (le instanceof Squid) {
			name = "squid";
		} else if (le instanceof Zombie) {
			name = "zombie";
		}
		return name;
	}

	public String checkMonsters(String name) {
		if (name.equals("chicken")) {
			name = "chicken";
		} else if (name.equals("cow")) {
			name = "cow";
		} else if (name.equals("creeper")) {
			name = "creeper";
		} else if (name.equals("ghast")) {
			name = "ghast";
		} else if (name.equals("giant")) {
			name = "giant";
		} else if (name.equals("pig")) {
			name = "pig";
		} else if (name.equals("pigzombie")) {
			name = "pigzombie";
		} else if (name.equals("sheep")) {
			name = "sheep";
		} else if (name.equals("skeleton")) {
			name = "skeleton";
		} else if (name.equals("slime")) {
			name = "slime";
		} else if (name.equals("spider")) {
			name = "spider";
		} else if (name.equals("squid")) {
			name = "squid";
		} else if (name.equals("zombie")) {
			name = "zombie";
		}
		return name;
	}

	public boolean checkMaps(Item id) {
		if (MonsterTamer.catchItems.containsKey(""
				+ id.getItemStack().getTypeId())) {
			return true;
		}
		return false;
	}

	public static String getName(Item id, Player p) {
		ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());
		if (array == null || array.size() == 0)
			return "";

		int i2 = 0;
		String monsterName = "";
		String itemID = "";
		for (int i = array.size() - 1; i >= 0; --i) {
			if (i2 == 0) {
				itemID = array.get(i);
			} else if (i2 == 1) {
				monsterName = array.get(i);
				if (id.getItemStack().getTypeId() == Integer.parseInt(itemID)) {
					break;
				}
			}
			if (i2 == 2) {
				i -= 1;
				i2 = 0;
				itemID = "";
				monsterName = "";
			} else
				i2 += 1;

		}
		return monsterName;
	}

	public static boolean isInArray(String name, String item, Player p) {
		ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());
		int i2 = 0;
		String monster = "";
		String itemID = "";
		for (int i = 0; i < array.size(); ++i) {
			if (i2 == 0) {
				monster = array.get(i);
			} else if (i2 == 1) {
				itemID = array.get(i);
				if (monster.equalsIgnoreCase(name)
						&& itemID.equalsIgnoreCase(item)) {
					return true;
				}
			}
			if (i2 == 2) {
				i -= 1;
				i2 = 0;
				itemID = "";
				monster = "";
			} else
				i2 += 1;
		}
		return false;
	}

	public static boolean removeNameFromArray(String name, String item, Player p) {
		ArrayList<String> array = MonsterTamer.playerMonsters.get(p.getName());
		int i2 = 0;
		int index = 0;
		String monster = "";
		String itemID = "";
		for (int i = 0; i < array.size(); ++i) {
			if (i2 == 0) {
				monster = array.get(i);
				index = i;
			} else if (i2 == 1) {
				itemID = array.get(i);
				if (monster.equalsIgnoreCase(name)
						&& itemID.equalsIgnoreCase(item)) {
					array.remove(index + 1);
					array.remove(index);

					MonsterTamer.playerMonsters.put(p.getName(), array);
					return true;
				}
			}
			if (i2 == 2) {
				i -= 1;
				i2 = 0;
				itemID = "";
				monster = "";
				index = 0;
			} else
				i2 += 1;

		}
		return false;
	}

	public static void addFriends(Player p, Creature c) {
		ArrayList<String> array = new ArrayList<String>();
		if (MonsterTamer.friends.containsKey(p.getName()))
			array = MonsterTamer.friends.get(p.getName());
		array.add("" + c.getEntityId());
		MonsterTamer.friends.put(p.getName(), array);
		return;
	}

	class RemindTask extends TimerTask {
		private PlayerDropItemEvent event;
		private Item itemdrop;

		public RemindTask(PlayerDropItemEvent e, Item id) {
			this.event = e;
			this.itemdrop = id;

		}

		public void run() {
			PlayerListen.spawnFromItemDrop(event, itemdrop);
		}
	}
}
