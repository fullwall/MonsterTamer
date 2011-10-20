package com.fullwall.MonsterTamer.listeners;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerChatEvent;

import com.fullwall.MonsterTamer.Constants.Constant;
import com.fullwall.MonsterTamer.MonsterManager;
import com.fullwall.MonsterTamer.Permission;
import com.fullwall.MonsterTamer.data.Catchable;
import com.fullwall.MonsterTamer.data.DataHandler;
import com.fullwall.MonsterTamer.data.Monster;
import com.fullwall.MonsterTamer.data.Tamer;
import com.fullwall.MonsterTamer.utils.EntityUtils;
import com.fullwall.MonsterTamer.utils.StringUtils;

public class EntityListen extends EntityListener {
	private static Random random = new Random(System.currentTimeMillis());
	private static HashMap<String, Monster> naming = new HashMap<String, Monster>();

	public static boolean handlePlayerChat(PlayerChatEvent event) {
		String name = event.getPlayer().getName();
		if (naming.get(name) != null) {
			Monster monster = naming.get(name);
			if (monster.getTamer().getMonster(event.getMessage()) != null) {
				event.getPlayer().sendMessage(
						ChatColor.GRAY
								+ "You already have a monster by that name.");
				return true;
			}
			monster.register(event.getMessage());
			naming.remove(name);
			event.getPlayer().sendMessage(
					ChatColor.GREEN + StringUtils.wrap(event.getMessage())
							+ " has been successfully tamed.");
			return true;
		}
		return false;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FIRE_TICK
				|| event.getCause() == DamageCause.FIRE) {
			if (MonsterManager.monsters.containsKey(event.getEntity()
					.getEntityId())) {
				event.getEntity().setFireTicks(0);
				event.setCancelled(true);
				return;
			}
		}
		if (!(event instanceof EntityDamageByEntityEvent)) {
			return;
		}
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

		if (e.getDamager() instanceof LivingEntity
				&& e.getEntity() instanceof Player) {
			Tamer tamer = MonsterManager.getTamer((Player) e.getEntity());
			if (tamer.hasMonsters()) {
				for (Monster monster : tamer.getMonsters()) {
					monster.setTarget(e.getDamager());
				}
			}
			return;
		}
		if (!(e.getDamager() instanceof Player)
				|| !(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		Player player = (Player) e.getDamager();
		Tamer tamer = MonsterManager.getTamer(player);

		LivingEntity le = (LivingEntity) e.getEntity();
		String name = EntityUtils.getMonsterName(le);
		Catchable catchable = MonsterManager.catchables.get(CreatureType
				.fromName(name));
		Monster monster = MonsterManager.getMonster(le);
		boolean owned = monster != null
				&& monster.getTamer().getName().equals(tamer.getName());
		if (catchable == null
				|| !catchable.canCatchWith(player.getItemInHand().getType())) {
			if (owned) {
				e.setCancelled(true);
				return;
			}
			return;
		}
		if (!Permission.generic(player, "monstertamer.player.catch")) {
			return;
		}
		if (owned) {
			monster.getMonster().remove();
			monster.setMonster(null);
			player.sendMessage(ChatColor.GREEN + "Monster returned to storage.");
			return;
		}
		double ran = random.nextDouble();
		int chance = attemptCatch(tamer, ran, le, catchable);
		if (ran > chance) {
			sendFailedMessage(player, name, ran - chance);
			return;
		} else {
			if (tamer.getMonsters().size() + 1 > Constant.Limit.getInt()) {
				player.sendMessage(ChatColor.RED
						+ "You don't have enough room for more monsters. The limit is "
						+ StringUtils.wrap(Constant.Limit.getInt(),
								ChatColor.RED) + ".");
				return;
			}
			if (monster != null && owned) {
				Player previousOwner = monster.getTamer().getPlayer();
				if (previousOwner != null) {
					previousOwner
							.sendMessage(StringUtils.wrap(monster.getName(),
									ChatColor.GRAY)
									+ " was taken by "
									+ StringUtils.wrap(player.getName(),
											ChatColor.GRAY) + ".");
				}
				monster.getTamer().removeMonster(monster.getName());
			}
			player.sendMessage(ChatColor.GREEN + "You caught a "
					+ StringUtils.wrap(name) + "!");
			player.sendMessage(ChatColor.GREEN
					+ "Type in the name of your new " + StringUtils.wrap(name)
					+ ".");
			naming.put(player.getName(),
					new Monster(tamer, EntityUtils.getType(le), player
							.getItemInHand().getType()));
			le.remove();

		}
		DataHandler.saveUsers();
	}

	@Override
	public void onEntityTarget(EntityTargetEvent e) {
		if (e.getTarget() instanceof Player) {
			Tamer tamer = MonsterManager.getTamer(((Player) e.getTarget()));
			if (tamer.owns(e.getEntity().getEntityId()))
				e.setCancelled(true);
		}
		if (e.getTarget() instanceof LivingEntity
				&& e.getEntity() instanceof LivingEntity) {
			Monster monster = MonsterManager.getMonster(e.getTarget());
			Monster second = MonsterManager.getMonster(e.getEntity());
			if (monster != null && second != null) {
				if (monster.getTamer().getName()
						.equals(second.getTamer().getName())) {
					e.setCancelled(true);
				}
			}
		}
	}

	private void sendFailedMessage(Player player, String name, double difference) {
		if (difference <= 10)
			player.sendMessage(ChatColor.LIGHT_PURPLE + "No! The " + name
					+ " escaped. So close!");
		else if (difference <= 30)
			player.sendMessage(ChatColor.AQUA + "Nearly had it!");
		else if (difference <= 60)
			player.sendMessage(ChatColor.BLUE + "Oh no! Couldn't capture the "
					+ name + "!");
		else if (difference <= 80)
			player.sendMessage(ChatColor.GREEN + "Nowhere near catching the "
					+ name + ".");
		else
			player.sendMessage(ChatColor.GRAY + "Failed to catch the " + name
					+ ".");

	}

	private int attemptCatch(Tamer tamer, double ran, LivingEntity le,
			Catchable catchable) {
		double chance = catchable.getMultiplier();
		if (chance < 3.0D)
			chance = 3.0D;
		double bonus = catchable.getChance(tamer.getPlayer().getItemInHand()
				.getType());
		ran *= 100;
		// magic pokemon chance to catch.
		chance = ((((3 * 20 - 2 * le.getHealth()) * chance * bonus) / (3 * 20) / 256) * 100);
		// friendlies get a 100% catch rate.
		if (tamer.owns(le.getEntityId()))
			chance = 9000;
		return (int) chance;
	}
}
