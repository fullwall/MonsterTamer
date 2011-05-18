package com.fullwall.MonsterTamer;

import java.util.ArrayList;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.server.AxisAlignedBB;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class PlayerListen extends PlayerListener {
	private static MonsterTamer plugin;
	private long delay = 0;
	public static Timer t = new Timer();

	@SuppressWarnings("static-access")
	public PlayerListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (Permission.check(e.getPlayer())) {
			Item id = e.getItemDrop();
			if (checkMaps(id)) {
				if (System.currentTimeMillis() <= (delay + 1000)) {
					e.getPlayer()
							.sendMessage(
									ChatColor.RED
											+ "You have to wait for at least a second before releasing another monster.");
					return;
				}
				delay = System.currentTimeMillis();
				t.schedule(new RemindTask(e, id), 1000);
			}
		}
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getPlayer().getItemInHand().getTypeId() == MonsterTamer.selectTool) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				handleSearchEntity(e);
			} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				handleSetBlockTarget(e);
			}
		}
	}

	private void handleSetBlockTarget(PlayerInteractEvent e) {
		if (MonsterTamer.selectedMonsters.get(e.getPlayer().getName()) != null) {
			for (Integer ID : MonsterTamer.selectedMonsters.get(e.getPlayer()
					.getName())) {
				MonsterTamer.locationMovers.put(ID, e.getPlayer()
						.getTargetBlock(null, 50).getLocation());
				MonsterTamer.addFollower(e.getPlayer(), ID, true);
			}
			e.getPlayer().sendMessage(
					ChatColor.YELLOW
							+ ""
							+ MonsterTamer.selectedMonsters.get(
									e.getPlayer().getName()).size()
							+ ChatColor.GREEN
							+ " monster(s) moving to that block.");
		} else
			e.getPlayer().sendMessage(ChatColor.RED + "No selected monsters!");
	}

	@SuppressWarnings({ "rawtypes" })
	private void handleSearchEntity(PlayerInteractEvent event) {
		List entities = getEntityList(event);
		if (entities.size() == 1) {
			Entity entity = CraftEntity.getEntity(
					(CraftServer) plugin.getServer(),
					(net.minecraft.server.Entity) entities.get(0));
			if (!(entity instanceof LivingEntity))
				return;
			if (MonsterTamer.friends.containsKey(event.getPlayer().getName())
					&& MonsterTamer.friends.get(event.getPlayer().getName())
							.contains(entity.getEntityId())) {
				if (!MonsterTamer.selectedMonsters.containsKey(event
						.getPlayer().getName())
						|| !MonsterTamer.selectedMonsters.get(
								event.getPlayer().getName()).contains(
								entity.getEntityId())) {
					ArrayList<Integer> selected = new ArrayList<Integer>();
					if (MonsterTamer.selectedMonsters.get(event.getPlayer()
							.getName()) != null)
						selected = MonsterTamer.selectedMonsters.get(event
								.getPlayer().getName());
					if (selected.indexOf(entity.getEntityId()) == -1) {
						selected.add(entity.getEntityId());
						MonsterTamer.selectedMonsters.put(event.getPlayer()
								.getName(), selected);
						event.getPlayer().sendMessage(
								ChatColor.GREEN
										+ "You selected your "
										+ ChatColor.YELLOW
										+ EntityListen.checkMonsters(
												(LivingEntity) entity)
												.toUpperCase()
										+ ChatColor.GREEN + ".");
						MonsterTamer.addFollower(event.getPlayer(),
								(LivingEntity) entity, false);
					}
				} else {
					ArrayList<Integer> selected = new ArrayList<Integer>();
					if (MonsterTamer.selectedMonsters.get(event.getPlayer()
							.getName()) != null)
						selected = MonsterTamer.selectedMonsters.get(event
								.getPlayer().getName());
					if (selected.indexOf(entity.getEntityId()) != -1) {
						selected.remove(selected.indexOf(entity.getEntityId()));
						MonsterTamer.selectedMonsters.put(event.getPlayer()
								.getName(), selected);
						event.getPlayer().sendMessage(
								ChatColor.GREEN
										+ "You deselected your "
										+ ChatColor.YELLOW
										+ EntityListen.checkMonsters(
												(LivingEntity) entity)
												.toUpperCase()
										+ ChatColor.GREEN + ".");
						MonsterTamer.addFollower(event.getPlayer(),
								(LivingEntity) entity, true);
					}
				}
			} else if (MonsterTamer.selectedMonsters.containsKey(event
					.getPlayer().getName())
					&& !(MonsterTamer.selectedMonsters.get(event.getPlayer()
							.getName()).contains(entity.getEntityId()))) {
				int count = 0;
				for (LivingEntity livingEntity : entity.getWorld()
						.getLivingEntities()) {
					if (MonsterTamer.selectedMonsters.get(
							event.getPlayer().getName()).contains(
							livingEntity.getEntityId())) {
						((Creature) livingEntity)
								.setTarget((LivingEntity) entity);
						count += 1;
					}
				}
				if (count != 0)
					event.getPlayer().sendMessage(
							ChatColor.GREEN + "Setting " + ChatColor.YELLOW
									+ count + ChatColor.GREEN
									+ " monster(s) on your target.");
				else
					event.getPlayer().sendMessage(
							ChatColor.RED
									+ "You don't have any monsters selected!");
			}

		}
	}

	@SuppressWarnings({ "rawtypes" })
	private List getEntityList(PlayerInteractEvent e) {
		Location loc = e.getPlayer().getTargetBlock(null, 4).getLocation();
		Location loc2 = e.getPlayer().getLocation();
		double x1 = loc.getBlockX() + 0.4D;
		double y1 = loc.getBlockY() + 0.4D;
		double z1 = loc.getBlockZ() + 0.4D;
		double x2 = loc2.getBlockX();
		double y2 = loc.getBlockY() + 0.6D;
		double z2 = loc2.getBlockZ();
		return ((CraftWorld) e.getPlayer().getWorld()).getHandle().b(
				((CraftPlayer) e.getPlayer()).getHandle(),
				AxisAlignedBB.a(Math.min(x1, x2), y1, Math.min(z1, z2),
						Math.max(x1, x2), y2, Math.max(z1, z2)));
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
		if (MonsterTamer.consumeItems) {
			int amount = id.getItemStack().getAmount();
			amount -= 1;
			if (amount == 0)
				id.remove();
			else {
				ItemStack is = id.getItemStack();
				is.setAmount(amount);
				id.setItemStack(is);
			}
		}
		try {
			CreatureType ct = CreatureType.fromName(name);
			Creature creature = (Creature) id.getWorld().spawnCreature(loc, ct);
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

	public static void spawnFromLocation(Player p, int ID) {
		if (MonsterTamer.playerMonsters.get(p.getName()) == null
				|| ((MonsterTamer.playerMonsters.get(p.getName()).size()) < 2))
			return;
		Location loc = p.getLocation();
		String name = getName(p, ID);
		if (name.isEmpty()) {
			return;
		}
		String item = "" + ID;
		if (!isInArray(name, item, p))
			return;
		try {
			CreatureType ct = CreatureType.fromName(name);
			Creature creature = (Creature) p.getWorld().spawnCreature(loc, ct);
			removeNameFromArray(name, item, p);
			p.sendMessage(ChatColor.LIGHT_PURPLE + "You released your "
					+ ChatColor.YELLOW + name + ChatColor.LIGHT_PURPLE
					+ ". You have " + ChatColor.GREEN
					+ (MonsterTamer.playerMonsters.get(p.getName()).size() / 2)
					+ ChatColor.LIGHT_PURPLE + " monsters remaining.");
			if (Permission.friendly(p)) {
				addFriends(p, creature);
			}
		} catch (Exception e1) {
			MonsterTamer.log.info("[MonsterTamer]: Error spawning monster.");

		}
		MonsterTamer.writeUsers();
	}

	public static String checkMonsters(String name) {
		name = name.toLowerCase();
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
		} else if (name.equals("wolf")) {
			name = "wolf";
		} else if (name.equals("zombie")) {
			name = "zombie";
		} else if (name.equals("all")) {
			name = "all";
		} else
			name = "";
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

	public static String getName(Player p, int ID) {
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
				if (ID == Integer.parseInt(itemID)) {
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
		ArrayList<Integer> array = new ArrayList<Integer>();
		if (MonsterTamer.friends.containsKey(p.getName()))
			array = MonsterTamer.friends.get(p.getName());
		array.add(c.getEntityId());
		MonsterTamer.friends.put(p.getName(), array);
		MonsterTamer.friendlies.add(c.getEntityId());
		return;
	}

	class RemindTask extends TimerTask {
		private PlayerDropItemEvent event;
		private Item itemdrop;

		public RemindTask(PlayerDropItemEvent e, Item id) {
			this.event = e;
			this.itemdrop = id;

		}

		@Override
		public void run() {
			PlayerListen.spawnFromItemDrop(event, itemdrop);
		}
	}
}
