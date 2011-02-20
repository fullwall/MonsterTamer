package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftItemDrop;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.ItemDrop;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;

public class PlayerListen extends PlayerListener {
	@SuppressWarnings("unused")
	private static MonsterTamer plugin;
	public static Timer t = new Timer();

	@SuppressWarnings("static-access")
	public PlayerListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	public void onPlayerCommand(PlayerChatEvent e) {
		String[] split = e.getMessage().split(" ");

		if (split.length == 1 && split[0].equalsIgnoreCase("/monsters")
				|| split[0].equalsIgnoreCase("/ms")) {
			if (!(Permission.checkMonsters(e.getPlayer()))) {
				e.getPlayer()
						.sendMessage(
								ChatColor.RED
										+ "You don't have permission to use that command.");
				return;
			}
			//if we don't have any monsters
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
		ItemDrop id = e.getItemDrop();
		if (checkMaps(id)) {
			t.schedule(new RemindTask(e, id), 1250);
		}
	}

	public static void spawnFromItemDrop(PlayerDropItemEvent e, ItemDrop id) {
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
		CraftItemDrop ci = (CraftItemDrop) id;
		ci.getHandle().q();
		try {
			CreatureType ct = CreatureType.fromName(name);
			id.getWorld().spawnCreature(loc, ct);
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

		} catch (Exception e1) {
			MonsterTamer.log.info("[MonsterTamer]: Error spawning monster.");

		}
		MonsterTamer.writeUsers();

	}

	public boolean checkMaps(ItemDrop id) {
		if (MonsterTamer.catchItems.containsKey(""
				+ id.getItemStack().getTypeId())) {
			return true;
		}
		return false;
	}

	public static String getName(ItemDrop id, Player p) {
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

	class RemindTask extends TimerTask {
		private PlayerDropItemEvent event;
		private ItemDrop itemdrop;

		public RemindTask(PlayerDropItemEvent e, ItemDrop id) {
			this.event = e;
			this.itemdrop = id;

		}

		public void run() {
			PlayerListen.spawnFromItemDrop(event, itemdrop);
		}
	}
}
