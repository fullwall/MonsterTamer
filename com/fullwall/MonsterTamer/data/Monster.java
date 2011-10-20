package com.fullwall.MonsterTamer.data;

import net.minecraft.server.EntityCreature;
import net.minecraft.server.PathEntity;
import net.minecraft.server.PathPoint;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.fullwall.MonsterTamer.MonsterManager;
import com.fullwall.MonsterTamer.utils.DirectionUtils;
import com.fullwall.MonsterTamer.utils.EntityUtils;
import com.fullwall.MonsterTamer.utils.LocationUtils;
import com.fullwall.MonsterTamer.utils.StringUtils;

public class Monster {
	private final CreatureType type;
	private final Material caughtWith;
	private final Tamer tamer;
	private String name;
	private boolean wait = false;
	private boolean follow = false;
	private boolean selected = false;
	private int target;
	private LivingEntity monster;
	private Location moveTo = null;
	private Location respawnPoint = null;

	public Monster(Tamer tamer, CreatureType type, Material caughtWith) {
		this.tamer = tamer;
		this.caughtWith = caughtWith;
		this.type = type;
	}

	public Monster(Tamer tamer, LivingEntity monster, Material caughtWith) {
		this.tamer = tamer;
		this.setMonster(monster);
		this.caughtWith = caughtWith;
		this.type = EntityUtils.getType(monster);
	}

	public Material getCaughtWith() {
		return caughtWith;
	}

	public void setTarget(Entity target) {
		this.target = target == null ? -1 : target.getEntityId();
	}

	public void setWait(boolean wait) {
		if (this.hasTarget())
			this.target = -1;
		this.wait = wait;
	}

	public boolean isWait() {
		return wait;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
	}

	public boolean isFollow() {
		return follow;
	}

	public int getID() {
		if (isDead())
			return -1;
		return this.getMonster().getEntityId();
	}

	public boolean isDead() {
		return this.getMonster() == null || this.getMonster().isDead();
	}

	public void setMoveTo(Location moveTo) {
		if (this.hasTarget())
			this.resetTarget();
		this.moveTo = moveTo;
	}

	private void resetTarget() {
		this.target = -1;
	}

	public Location getMoveTo() {
		return moveTo;
	}

	public void doTick() {
		if (follow || wait || moveTo != null) {
			Location move = null;
			if (follow) {
				Player player = tamer.getPlayer();
				move = DirectionUtils.getBlockBehind(player.getLocation(),
						player.getLocation().getYaw()).getLocation();
			}
			if (wait) {
				move = this.monster.getLocation();
			}
			if (moveTo != null) {
				move = moveTo;
				if (LocationUtils.withinRange(monster.getLocation(), moveTo, 1)) {
					moveTo = null;
				}
			}
			(getHandle()).pathEntity = new PathEntity(
					new PathPoint[] { new PathPoint(move.getBlockX(),
							move.getBlockY(), move.getBlockZ()) });
		}
		if (target > 0) {
			boolean found = false;
			for (LivingEntity entity : this.monster.getLocation().getBlock()
					.getWorld().getLivingEntities()) {
				if (entity.getEntityId() == target) {
					((Creature) monster).setTarget(entity);
					found = true;
				}
			}
			if (!found)
				target = -1;
		}
	}

	private EntityCreature getHandle() {
		return ((EntityCreature) ((CraftEntity) this.monster).getHandle());
	}

	public CreatureType getType() {
		return this.type;
	}

	public boolean matches(CreatureType type) {
		return type == this.type;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void setMonster(LivingEntity monster) {
		if (monster == null && this.monster != null)
			MonsterManager.monsters.remove(this.monster.getEntityId());
		else
			MonsterManager.monsters.put(monster.getEntityId(), this);
		this.monster = monster;
	}

	public LivingEntity getMonster() {
		return monster;
	}

	public String getName() {
		return this.name;
	}

	public void register(String name) {
		this.name = name;
		tamer.addMonster(this);
	}

	public Tamer getTamer() {
		return this.tamer;
	}

	public boolean withinRange(LivingEntity target) {
		return LocationUtils.withinRange(this.monster.getLocation(),
				target.getLocation(), 10);
	}

	public boolean hasTarget() {
		return target != -1;
	}

	public String formattedType() {
		return StringUtils.capitalise(this.type.getName().toLowerCase());
	}

	public String formattedCaughtWith() {
		return StringUtils.capitalise(this.caughtWith.name().toLowerCase()
				.replace("_", ""));
	}

	public void setRespawnPoint(Location respawnPoint) {
		this.respawnPoint = respawnPoint;
	}

	public Location getRespawnPoint() {
		return respawnPoint;
	}
}
