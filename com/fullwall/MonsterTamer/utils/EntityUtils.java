package com.fullwall.MonsterTamer.utils;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;

public class EntityUtils {
	public static String getMonsterName(LivingEntity le) {
		if (le instanceof Chicken) {
			return "Chicken";
		} else if (le instanceof Cow) {
			return "Cow";
		} else if (le instanceof Creeper) {
			return "Creeper";
		} else if (le instanceof Ghast) {
			return "Ghast";
		} else if (le instanceof Giant) {
			return "Giant";
		} else if (le instanceof Pig) {
			return "Pig";
		} else if (le instanceof PigZombie) {
			return "PigZombie";
		} else if (le instanceof Sheep) {
			return "Sheep";
		} else if (le instanceof Skeleton) {
			return "Skeleton";
		} else if (le instanceof Slime) {
			return "Slime";
		} else if (le instanceof Spider) {
			return "Spider";
		} else if (le instanceof Squid) {
			return "Squid";
		} else if (le instanceof Wolf) {
			return "Wolf";
		} else if (le instanceof Zombie) {
			return "Zombie";
		}
		return "";
	}

	public static CreatureType getType(Entity entity) {
		if (entity instanceof Chicken) {
			return CreatureType.CHICKEN;
		} else if (entity instanceof Cow) {
			return CreatureType.COW;
		} else if (entity instanceof Creeper) {
			return CreatureType.CREEPER;
		} else if (entity instanceof Ghast) {
			return CreatureType.GHAST;
		} else if (entity instanceof Giant) {
			return CreatureType.GIANT;
		} else if (entity instanceof Pig) {
			return CreatureType.PIG;
		} else if (entity instanceof PigZombie) {
			return CreatureType.PIG_ZOMBIE;
		} else if (entity instanceof Sheep) {
			return CreatureType.SHEEP;
		} else if (entity instanceof Skeleton) {
			return CreatureType.SKELETON;
		} else if (entity instanceof Slime) {
			return CreatureType.SLIME;
		} else if (entity instanceof Spider) {
			return CreatureType.SPIDER;
		} else if (entity instanceof Squid) {
			return CreatureType.SQUID;
		} else if (entity instanceof Wolf) {
			return CreatureType.WOLF;
		} else if (entity instanceof Zombie) {
			return CreatureType.ZOMBIE;
		}
		return null;
	}
}
