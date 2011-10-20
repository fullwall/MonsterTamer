package com.fullwall.MonsterTamer.utils;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

	/**
	 * Remove items from a player's current held slot
	 * 
	 * @param player
	 * @param material
	 */
	public static void decrementItem(Player player, int slot) {
		ItemStack item = player.getInventory().getItem(slot);
		if (item == null)
			return;
		if (item.getAmount() == 1)
			item = null;
		else
			item.setAmount(item.getAmount() - 1);
		player.getInventory().setItem(slot, item);
	}

	public static void decrementItem(Item item) {
		if (item == null)
			return;
		if (item.getItemStack().getAmount() == 1)
			item.remove();
		else {
			ItemStack stack = item.getItemStack();
			stack.setAmount(stack.getAmount() - 1);
			item.setItemStack(stack);
		}
	}
}