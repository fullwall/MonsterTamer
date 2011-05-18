package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class EntityListen extends EntityListener {
	@SuppressWarnings("unused")
	private final MonsterTamer plugin;
	private long delay;
	public static Random random = new Random();

	public EntityListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FIRE_TICK
				|| event.getCause() == DamageCause.FIRE) {
			if (MonsterTamer.friendlies.contains(event.getEntity()
					.getEntityId())) {
				event.setCancelled(true);
				event.getEntity().setFireTicks(0);
				return;
			}
		}
		if (!(event instanceof EntityDamageByEntityEvent)) {
			return;
		}
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (e.getDamager() instanceof LivingEntity
				&& e.getEntity() instanceof Player
				&& MonsterTamer.friends.get(((Player) e.getEntity()).getName()) != null) {
			ArrayList<Integer> array = MonsterTamer.friends.get(((Player) e
					.getEntity()).getName());
			List<LivingEntity> livingEntities = e.getEntity().getWorld()
					.getLivingEntities();
			for (LivingEntity i : livingEntities) {
				if (i instanceof Creature && array.contains(i.getEntityId())) {
					Creature c = (Creature) i;
					c.setTarget((LivingEntity) e.getDamager());
				}
			}
			return;
		}
		if (!(e.getDamager() instanceof Player)
				|| !(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		LivingEntity le = (LivingEntity) e.getEntity();
		Player player = (Player) e.getDamager();

		if ((MonsterTamer.catchItems.get(""
				+ player.getItemInHand().getTypeId()) == null)) {
			return;
		}
		if (MonsterTamer.monsterChances.get(checkMonsters(le)) == null) {
			return;
		}
		if (!Permission.check(player)) {
			return;
		}
		String name = checkMonsters(le);
		String isCatching;
		if (MonsterTamer.playerCatching.containsKey(player.getName()))
			isCatching = MonsterTamer.playerCatching.get(player.getName());
		else
			isCatching = null;
		if (isCatching != null) {
			if (MonsterTamer.playerCatching.get(player.getName())
					.equalsIgnoreCase(name)) {
				return;
			}
		}
		MonsterTamer.playerCatching.put(player.getName(), name);
		double chance = MonsterTamer.monsterChances.get(checkMonsters(le));
		if (chance < 3.0D)
			chance = 3.0D;
		double bonus = (MonsterTamer.catchItems.get(""
				+ player.getItemInHand().getTypeId()).doubleValue());
		double ran = random.nextDouble();
		ran *= 100;
		// magic pokemon chance to catch.
		chance = ((((3 * 20 - 2 * le.getHealth()) * chance * bonus) / (3 * 20) / 256) * 100);
		// friendlies get a 100% catch rate.
		ArrayList<Integer> temparray = MonsterTamer.friends.get(player
				.getName());
		if (temparray != null
				&& temparray.contains(e.getEntity().getEntityId()))
			chance = 9000;
		if (ran > chance) {
			if (ran - chance <= 10)
				player.sendMessage(ChatColor.LIGHT_PURPLE + "No! The " + name
						+ " escaped. So close!");
			else if (ran - chance <= 30)
				player.sendMessage(ChatColor.AQUA + "Nearly had it!");
			else if (ran - chance <= 60)
				player.sendMessage(ChatColor.BLUE
						+ "Oh no! Couldn't capture the " + name + "!");
			else if (ran - chance <= 80)
				player.sendMessage(ChatColor.GREEN
						+ "Nowhere near catching the " + name + ".");
			else
				player.sendMessage(ChatColor.GRAY + "Failed to catch the "
						+ name + ".");
			MonsterTamer.playerCatching.remove(player.getName());
			return;
		} else {
			if (System.currentTimeMillis() < 1000L + this.delay) {
				player.sendMessage(ChatColor.DARK_AQUA
						+ "You were moving too fast and stumbled.");
				MonsterTamer.playerCatching.remove(player.getName());
				return;
			}
			ArrayList<String> array = new ArrayList<String>();
			if (MonsterTamer.playerMonsters.get(player.getName()) != null) {
				array = MonsterTamer.playerMonsters.get(player.getName());
			}
			this.delay = System.currentTimeMillis();
			if ((array.size() / 2) > MonsterTamer.limit) {
				player.sendMessage(ChatColor.RED
						+ "You don't have enough room for more monsters! The limit is "
						+ MonsterTamer.limit + ".");
				MonsterTamer.playerCatching.remove(player.getName());
				return;
			}
			// get rid of the monster
			if (MonsterTamer.friends.containsKey(player.getName())) {
				ArrayList<Integer> friendsArray = MonsterTamer.friends
						.get(player.getName());
				if (friendsArray.contains(le.getEntityId())) {
					friendsArray.remove(friendsArray.indexOf(le.getEntityId()));
					MonsterTamer.friends.put(player.getName(), friendsArray);
				}
			}
			// stop other monsters overwriting the friendly ID
			if (MonsterTamer.friendlies.contains(le.getEntityId()))
				MonsterTamer.friendlies.remove(MonsterTamer.friendlies
						.indexOf(le.getEntityId()));
			le.remove();

			array.add(name);
			array.add("" + player.getItemInHand().getTypeId());
			MonsterTamer.playerMonsters.put(player.getName(), array);
			player.sendMessage(ChatColor.GOLD + "You caught a " + name + "!");

		}
		MonsterTamer.playerCatching.remove(player.getName());
		MonsterTamer.writeUsers();
	}

	@Override
	public void onEntityTarget(EntityTargetEvent e) {
		if ((e.getTarget() instanceof Player)) {
			int EID = e.getEntity().getEntityId();
			Player p = (Player) e.getTarget();
			if (MonsterTamer.friendlies.contains(EID)) {
				String name = MonsterTamer.targets.get(EID);
				if (name == null || name.isEmpty()) {
					e.setCancelled(true);
				}
				if (name != null && !(name.equals(p.getName()))) {
					e.setCancelled(true);
				}
			}
		}
		if (e.getTarget() instanceof LivingEntity
				&& e.getEntity() instanceof Monster) {
			int EID = e.getEntity().getEntityId();
			int TID = e.getTarget().getEntityId();
			for (Entry<String, ArrayList<Integer>> i : MonsterTamer.friends
					.entrySet()) {
				if (i.getValue().contains(EID) && i.getValue().contains(TID)) {
					e.setCancelled(true);
				}
			}
		}
	}

	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Monster
				|| event.getEntity() instanceof Animals) {
			ArrayList<Integer> ids = new ArrayList<Integer>();
			int id = event.getEntity().getEntityId();
			for (Entry<String, ArrayList<Integer>> i : MonsterTamer.selectedMonsters
					.entrySet()) {
				ids = i.getValue();
				if (ids.contains(id)) {
					ids.remove(ids.indexOf(id));
				}
			}
		}
	}

	public static String checkMonsters(LivingEntity le) {
		String name = "";
		if (le instanceof Chicken) {
			name = "Chicken";
		} else if (le instanceof Cow) {
			name = "Cow";
		} else if (le instanceof Creeper) {
			name = "Creeper";
		} else if (le instanceof Ghast) {
			name = "Ghast";
		} else if (le instanceof Giant) {
			name = "Giant";
		} else if (le instanceof Pig) {
			name = "Pig";
		} else if (le instanceof PigZombie) {
			name = "PigZombie";
		} else if (le instanceof Sheep) {
			name = "Sheep";
		} else if (le instanceof Skeleton) {
			name = "Skeleton";
		} else if (le instanceof Slime) {
			name = "Slime";
		} else if (le instanceof Spider) {
			name = "Spider";
		} else if (le instanceof Squid) {
			name = "Squid";
		} else if (le instanceof Wolf) {
			name = "Wolf";
		} else if (le instanceof Zombie) {
			name = "Zombie";
		}
		return name;
	}
}
