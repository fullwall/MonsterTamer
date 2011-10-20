package com.fullwall.MonsterTamer;

import com.fullwall.MonsterTamer.data.Monster;

public class TickTask implements Runnable {

	@Override
	public void run() {
		for (Monster monster : MonsterManager.monsters.values()) {
			monster.doTick();
		}
	}
}
