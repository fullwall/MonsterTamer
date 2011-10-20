package com.fullwall.MonsterTamer.listeners;

import java.util.Collection;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

import com.fullwall.MonsterTamer.Constants.Constant;
import com.fullwall.MonsterTamer.MonsterManager;
import com.fullwall.MonsterTamer.MonsterTamer;
import com.fullwall.MonsterTamer.Permission;
import com.fullwall.MonsterTamer.data.DataHandler;
import com.fullwall.MonsterTamer.data.Monster;
import com.fullwall.MonsterTamer.data.Tamer;
import com.fullwall.MonsterTamer.utils.InventoryUtils;
import com.fullwall.MonsterTamer.utils.StringUtils;

public class PlayerListen extends PlayerListener {

	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if (EntityListen.handlePlayerChat(event))
			event.setCancelled(true);
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (MonsterManager.getTamer(event.getPlayer()) == null)
			MonsterManager.addTamer(event.getPlayer(), new Tamer(event
					.getPlayer().getName()));
	}

	@Override
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (Permission.generic(e.getPlayer(), "monstertamer.player.release")) {
			Tamer tamer = MonsterManager.getTamer(e.getPlayer());
			if (tamer.hasCaughtWith(e.getItemDrop().getItemStack().getType())) {
				Bukkit.getServer()
						.getScheduler()
						.scheduleSyncDelayedTask(MonsterTamer.plugin,
								new SpawnTask(tamer, e, e.getItemDrop()), 20);
			}
		}
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getPlayer().getItemInHand().getTypeId() == Constant.SelectTool
				.getInt()) {
			if (e.getAction() == Action.LEFT_CLICK_BLOCK
					|| e.getAction() == Action.LEFT_CLICK_AIR) {
				handleLeftClick(e);
			}
		}
	}

	@Override
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getPlayer().getItemInHand().getTypeId() == Constant.SelectTool
				.getInt()) {
			handleRightClick(event);
		}
	}

	public static void spawnFromItemDrop(Tamer tamer,
			PlayerDropItemEvent event, Item item) {
		if (!tamer.hasMonsters() || !tamer.hasStoredMonsters())
			return;
		boolean succeeded = spawnFromLocation(tamer, event.getPlayer(), item
				.getItemStack().getType(), item.getLocation());
		if (succeeded && Constant.ConsumeItems.getBoolean()) {
			event.getPlayer().sendMessage(
					ChatColor.GRAY
							+ StringUtils.pluralise("One of the ", "The ",
									"item", item.getItemStack().getAmount())
							+ " disappeared.");
			InventoryUtils.decrementItem(item);
		}
	}

	public static boolean spawnFromLocation(Tamer tamer, Player player,
			Material type, Location loc) {
		if (!tamer.hasMonsters() || !tamer.hasStoredMonsters())
			return false;
		Monster monster = tamer.spawn(loc, type);
		if (monster == null) {
			player.sendMessage(ChatColor.GRAY + "Unable to spawn a monster.");
			return false;
		}
		player.sendMessage(StringUtils.wrap("Released ") + "your "
				+ StringUtils.wrap(monster.getType().getName()) + ". You have "
				+ StringUtils.wrap(tamer.getStored().size()) + " "
				+ StringUtils.pluralise("monster", tamer.getStored().size())
				+ " remaining.");

		DataHandler.saveUsers();
		return true;
	}

	private void handleLeftClick(PlayerInteractEvent event) {
		Tamer tamer = MonsterManager.getTamer(event.getPlayer());
		if (!tamer.hasSelected()) {
			event.getPlayer().sendMessage(
					ChatColor.GRAY + "No monsters selected.");
			return;
		}
		Collection<Monster> selected = tamer.getSelected();
		for (Monster monster : selected) {
			if (event.getClickedBlock() != null) {
				monster.setMoveTo(event.getClickedBlock().getLocation());
			} else {
				monster.setMoveTo(event.getPlayer().getTargetBlock(null, 50)
						.getLocation());
			}
		}
		event.getPlayer().sendMessage(
				StringUtils.wrap(selected.size()) + " "
						+ StringUtils.pluralise("monster", selected.size())
						+ " moving to that block.");
	}

	private void handleRightClick(PlayerInteractEntityEvent e) {
		Tamer tamer = MonsterManager.getTamer(e.getPlayer());
		if (tamer.getMonster(MonsterManager.getMonster(e.getRightClicked())) != null) {
			Monster monster = MonsterManager.getMonster(e.getRightClicked());
			String prefix = "";
			if (monster.isSelected()) {
				prefix = "de";
				tamer.removeSelected(monster.getName());
			} else {
				tamer.addSelected(monster);
			}
			e.getPlayer().sendMessage(
					ChatColor.GREEN + "You "
							+ StringUtils.wrap(prefix + "selected") + " your "
							+ StringUtils.wrap(monster.getType().getName())
							+ ".");
		} else if (tamer.hasSelected()) {
			Collection<Monster> selected = tamer.getSelected();
			if (selected.size() == 0) {
				e.getPlayer().sendMessage(
						ChatColor.GRAY + "No monsters selected.");
				return;
			}
			for (Monster monster : selected) {
				monster.setTarget(e.getRightClicked());
			}
			e.getPlayer().sendMessage(
					ChatColor.GREEN + "Setting "
							+ StringUtils.wrap(selected.size()) + " "
							+ StringUtils.pluralise("monster", selected.size())
							+ " on that creature.");
		}
	}

	public static class SpawnTask extends TimerTask {
		private final PlayerDropItemEvent event;
		private final Item itemdrop;
		private final Tamer tamer;

		public SpawnTask(Tamer tamer, PlayerDropItemEvent event, Item item) {
			this.tamer = tamer;
			this.event = event;
			this.itemdrop = item;

		}

		@Override
		public void run() {
			if (itemdrop.getLocation().getBlock().getRelative(BlockFace.DOWN)
					.getType() == Material.AIR)
				Bukkit.getServer().getScheduler()
						.scheduleSyncDelayedTask(MonsterTamer.plugin, this, 4);
			spawnFromItemDrop(tamer, event, itemdrop);
		}
	}
}
