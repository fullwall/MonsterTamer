package com.fullwall.MonsterTamer;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
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
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityListen extends EntityListener {
	@SuppressWarnings("unused")
	private final MonsterTamer plugin;
	public static Random random = new Random();

	public EntityListen(final MonsterTamer plugin) {
		this.plugin = plugin;
	}

	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event instanceof EntityDamageByEntityEvent)) {
			return;
		}
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (!(e.getDamager() instanceof Player))
			return;

		if (!(e.getEntity() instanceof LivingEntity)
				&& (!(e.getEntity() instanceof Animals) || !(e.getEntity() instanceof Monster)))
			return;
		LivingEntity le = (LivingEntity) e.getEntity();
		Player player = (Player) e.getDamager();

		if ((MonsterTamer.catchItems.get(""
				+ player.getItemInHand().getTypeId()) == null))
			return;
		if (MonsterTamer.monsterChances.get(checkMonsters(le)) == null)
			return;
		if (!Permission.check(player))
			return;
		String name = checkMonsters(le);
		String isCatching;
		if (MonsterTamer.playerCatching.containsKey(player.getName()))
			isCatching = MonsterTamer.playerCatching.get(player.getName());
		else
			isCatching = null;
		if (isCatching != null)
			if (MonsterTamer.playerCatching.get(player.getName()).equals(name))
				return;
		MonsterTamer.playerCatching.put(player.getName(), name);
		double chance = MonsterTamer.monsterChances.get(checkMonsters(le));
		if (chance < 3)
			chance = 3;
		double bonus = MonsterTamer.catchItems.get(""
				+ player.getItemInHand().getTypeId());
		double ran = random.nextDouble();
		ran *= 100;
		// magic pokemon chance to catch.
		chance = ((((3 * 20 - 2 * le.getHealth()) * chance * bonus) / (3 * 20) / 256) * 100);
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

			ArrayList<String> array = new ArrayList<String>();
			if (MonsterTamer.playerMonsters.get(player.getName()) == null) {
				@SuppressWarnings("unused")
				int i = 0;
			} else {
				array = MonsterTamer.playerMonsters.get(player.getName());
			}
			if (array.size() % 2 > MonsterTamer.limit) {
				player.sendMessage(ChatColor.RED
						+ "You don't have enough room for more monsters! The limit is "
						+ MonsterTamer.limit + ".");
				MonsterTamer.playerCatching.remove(player.getName());
				return;
			}
			// get rid of the monster
			if (MonsterTamer.friends.containsKey(player.getName())) {
				ArrayList<String> friendsArray = MonsterTamer.friends
						.get(player.getName());
				if (friendsArray.contains(""+le.getEntityId()))
					friendsArray.remove(""+le.getEntityId());
			}
			le.remove();

			array.add(name);
			array.add("" + player.getItemInHand().getTypeId());
			MonsterTamer.playerMonsters.put(player.getName(), array);
			player.sendMessage(ChatColor.GOLD + "You caught a " + name + "!");

		}
		MonsterTamer.playerCatching.remove(player.getName());
		MonsterTamer.writeUsers();
	}
	public void onEntityTarget(EntityTargetEvent e) {
		if (e.getTarget() instanceof Player) {
			Player p = (Player) e.getTarget();
			if (MonsterTamer.friends.containsKey(p.getName())) {
				ArrayList<String> array = MonsterTamer.friends.get(p.getName());
				if (array.contains(""+e.getEntity().getEntityId())) {
					e.setCancelled(true);
				}
			}
		}
	}

	public String checkMonsters(LivingEntity le) {
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
		} else if (le instanceof Zombie) {
			name = "Zombie";
		}
		return name;
	}
}
