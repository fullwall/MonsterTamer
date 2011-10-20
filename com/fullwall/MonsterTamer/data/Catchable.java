package com.fullwall.MonsterTamer.data;

import java.util.Map;

import org.bukkit.Material;

public class Catchable {
	private final Map<Integer, Double> chances;
	private final double monsterMultiplier;

	public Catchable(double monsterMultiplier, Map<Integer, Double> itemChances) {
		this.monsterMultiplier = monsterMultiplier;
		this.chances = itemChances;
	}

	public boolean canCatchWith(Material material) {
		return chances.get(material.getId()) != null;
	}

	public double getChance(Material material) {
		return chances.get(material.getId());
	}

	public double getMultiplier() {
		return monsterMultiplier;
	}
}
