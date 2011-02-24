package com.fullwall.MonsterTamer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Name for Bukkit
 * 
 * @author fullwall
 */
public class MonsterTamer extends JavaPlugin {

	public final PlayerListen pl = new PlayerListen(this);
	public final EntityListen el = new EntityListen(this);

	private static final String codename = "Companions";
	public static Logger log = Logger.getLogger("Minecraft");

	// what monster the player is currently catching.
	public static HashMap<String, String> playerCatching = new HashMap<String, String>();
	// player name, arraylist of monsters, item caught with - grouped in twos
	public static HashMap<String, ArrayList<String>> playerMonsters = new HashMap<String, ArrayList<String>>();
	// name, catch rate
	public static HashMap<String, Double> monsterChances = new HashMap<String, Double>();
	// id, bonus
	public static HashMap<String, Double> catchItems = new HashMap<String, Double>();
	public static HashMap<String, ArrayList<String>> friends = new HashMap<String, ArrayList<String>>();
	public static Integer limit = 50;

	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGED, el, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, el, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pl, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND, pl, Priority.Normal, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		Permission.initialize(getServer());
		readSettings();
		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well.
		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") loaded");

	}

	public void onDisable() {
		writeUsers();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") disabled");
	}

	public void readSettings() {

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(
					"plugins/MonsterTamer/MonsterTamer.properties"));
		} catch (FileNotFoundException e) {
			log.info("[MonsterTamer]: Couldn't find properties file.");
		} catch (IOException e) {
			log.info("[MonsterTamer]: Couldn't load properties file.");
		}
		if (props.containsKey("items")) {
			String[] split = props.getProperty("items").split(";");
			for (String i : split) {
				String[] newSplit = i.split(":");
				catchItems.put(newSplit[0], Double.parseDouble(newSplit[1]));
			}
		}
		if (props.containsKey("limit")) {
			limit = Integer.parseInt(props.getProperty("limit"));
		}
		if (props.containsKey("monsters")) {
			String[] split = props.getProperty("monsters").split(";");
			for (String i : split) {
				String[] newSplit = i.split(":");
				monsterChances
						.put(newSplit[0], Double.parseDouble(newSplit[1]));
			}
		}
		props.clear();
		try {
			FileReader input = new FileReader(
					"plugins/MonsterTamer/MonsterTamer.users");
			BufferedReader bufRead = new BufferedReader(input);
			String line;
			line = bufRead.readLine();
			while (line != null) {
				if (line.length() == 0) {
					line = bufRead.readLine();
					continue;
				}
				if (line.isEmpty()) {
					line = bufRead.readLine();
					continue;
				}
				if (line.startsWith("#")) {
					line = bufRead.readLine();
					continue;
				}
				if (!line.contains("=")) {
					line = bufRead.readLine();
					continue;
				}

				int equals = line.indexOf("=");

				int commentIndex = line.length();
				String key = line.substring(0, equals).trim();

				if (key.equals("")) {
					line = bufRead.readLine();
					continue;
				}

				String value = line.substring(equals + 1, commentIndex).trim();
				String[] values = value.split(";");
				ArrayList<String> array = new ArrayList<String>();
				for (int i = 0; i < values.length; ++i) {

					String[] player = values[i].split(",");

					for (String i4 : player) {
						array.add(i4);
					}

				}
				MonsterTamer.playerMonsters.put(key, array);
				line = bufRead.readLine();
			}
			bufRead.close();

		} catch (IOException e) {
			log.info("[MonsterTamer]: Error reading MonsterTamer.users.");
		}

	}

	public static void writeUsers() {
		Properties props = new Properties();
		String str = "";
		int i3 = 0;

		// get rid of whitespace
		for (Map.Entry<String, ArrayList<String>> entry : playerMonsters
				.entrySet()) {
			Iterator<String> it = entry.getValue().iterator();
			while (it.hasNext()) {
				String itn = it.next();
				if (itn.contains(" "))
					itn.replace(" ", "");
				if (itn.isEmpty())
					it.remove();
			}
		}
		String first;
		String second;
		// switch around errored monsters
		for (Map.Entry<String, ArrayList<String>> entry : playerMonsters
				.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				first = entry.getValue().get(i);
				if (i != entry.getValue().size() - 1) {
					second = entry.getValue().get(i + 1);
				} else
					break;
				if (Character.isDigit(first.charAt(0))
						&& !Character.isDefined(second.charAt(0))) {
					entry.getValue().add(i, second);
					entry.getValue().add(i + 1, first);
				}

			}
		}

		try {
			for (Map.Entry<String, ArrayList<String>> entry : playerMonsters
					.entrySet()) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					i3 += 1;
					str += entry.getValue().get(i);
					if (i3 != 2)
						str += ",";
					else {
						i3 = 0;
						str += ";";
					}
				}
				props.setProperty(entry.getKey(), str);

			}
			props.store(new FileOutputStream(
					"plugins/MonsterTamer/MonsterTamer.users"), null);
		} catch (FileNotFoundException e) {
			log.info("[MonsterTamer]: Couldn't find MonsterTamer.users.");
		} catch (IOException e) {
			log.info("[MonsterTamer]: Couldn't write to MonsterTamer.users.");
		}
	}
}