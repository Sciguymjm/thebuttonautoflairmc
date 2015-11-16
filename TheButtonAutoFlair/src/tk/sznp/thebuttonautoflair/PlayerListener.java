package tk.sznp.thebuttonautoflair;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import au.com.mineauz.minigames.Minigames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PlayerListener implements Listener { // 2015.07.16.
	public static HashMap<String, UUID> nicknames = new HashMap<>();

	public static boolean Enable = false; // 2015.08.29.

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
				.getUniqueId());
		mp.PlayerName = p.getName(); // 2015.10.17. 0:58
		if (mp.CommentedOnReddit)
			// if (false)
			PluginMain.AppendPlayerDisplayFlair(mp, p); // 2015.08.09.
		else { // 2015.07.20.
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					if (!mp.IgnoredFlair) {
						String json = "[\"\",{\"text\":\"If you'd like your /r/TheButton flair displayed ingame, write your Minecraft name to \",\"color\":\"aqua\"},{\"text\":\"[this thread].\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]";
						PluginMain.Instance.getServer().dispatchCommand(
								PluginMain.Console,
								"tellraw " + mp.PlayerName + " " + json);
						json = "[\"\",{\"text\":\"If you don't want the flair, type /u ignore to prevent this message after next login.\",\"color\":\"aqua\"}]";
						PluginMain.Instance.getServer().dispatchCommand(
								PluginMain.Console,
								"tellraw " + mp.PlayerName + " " + json);
					}
				}
			};
			tt.mp = mp;
			timer.schedule(tt, 15 * 1000);
		}

		/* NICKNAME LOGIC */

		UUID id = p.getUniqueId();

		File f = new File("plugins/Essentials/userdata/" + id + ".yml");
		if (f.exists()) {
			YamlConfiguration yc = new YamlConfiguration();
			try {
				yc.load(f);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			String nickname = yc.getString("nickname");
			if (nickname != null) {
				nicknames.put(nickname, id);

				if (Enable) {
					if (!p.getName().equals("NorbiPeti")) {
						for (Player player : PluginMain.GetPlayers()) {
							if (player.getName().equals("NorbiPeti")) {
								player.chat("Hey, " + nickname + "!");
								break;
							}
						}
					}
				}
			}
		}

		mp.RPMode = true; // 2015.08.25.

		if (p.getName().equals("FigyTuna") && Commands.PluginUpdated)
			p.sendMessage("�bThe The Button MC plugin got updated. Please restart the server. :P�r");
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		String deletenick = null;
		for (String nickname : nicknames.keySet()) {
			UUID uuid = nicknames.get(nickname);
			if (event.getPlayer().getUniqueId().equals(uuid)) {
				deletenick = nickname;
				break;
			}
		}
		if (deletenick != null)
			nicknames.remove(deletenick);
	}

	public static String NotificationSound; // 2015.08.14.
	public static double NotificationPitch; // 2015.08.14.

	public static boolean ShowRPTag = false; // 2015.08.31.

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		MaybeOfflinePlayer player = MaybeOfflinePlayer.AllPlayers.get(event
				.getPlayer().getUniqueId());
		String flair = player.Flair; // 2015.08.08.
		if (player.IgnoredFlair)
			flair = "";
		String message = event.getMessage(); // 2015.08.08.
		for (Player p : PluginMain.GetPlayers()) { // 2015.08.12.
			String color = ""; // 2015.08.17.
			if (message.contains(p.getName())) {
				if (NotificationSound == null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); // 2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f,
							(float) NotificationPitch); // 2015.08.14.
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
						.getUniqueId()); // 2015.08.17.
				if (mp.Flair.length() > 1)
					color = mp.Flair.substring(0, 2);
			}

			message = message.replaceAll(p.getName(), color + p.getName()
					+ (event.getMessage().startsWith("�2>") ? "�2" : "�r"));
		}
		for (String n : nicknames.keySet()) {
			Player p = null;
			String nwithoutformatting = new String(n);
			int index;
			while ((index = nwithoutformatting.indexOf("�k")) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("�k"
						+ nwithoutformatting.charAt(index + 2), ""); // Support
																		// for
																		// one
																		// random
																		// char
			while ((index = nwithoutformatting.indexOf('�')) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("�"
						+ nwithoutformatting.charAt(index + 1), "");
			if (message.contains(nwithoutformatting)) {
				p = Bukkit.getPlayer(nicknames.get(n));
				if (NotificationSound == null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); // 2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f,
							(float) NotificationPitch); // 2015.08.14.
				MaybeOfflinePlayer.AddPlayerIfNeeded(p.getUniqueId()); // 2015.08.17.
			}
			if (p != null) {
				message = message.replaceAll(nwithoutformatting, n
						+ (event.getMessage().startsWith("�2>") ? "�2" : "�r"));
			}
		}

		event.setMessage(message); // 2015.09.05.

		event.setFormat(event
				.getFormat()
				.replace(
						"{rptag}",
						(player.RPMode ? (ShowRPTag ? "�2[RP]�r" : "")
								: "�8[OOC]�r"))
				.replace("{buttonflair}", flair)
				.replace(
						"{isitwilds}",
						(event.getPlayer().getWorld().getName()
								.equalsIgnoreCase("wilds") ? "[PVP]" : ""))); // 2015.09.04.
	}

	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent e) {
		String name = e.getLastToken();
		for (String nickname : nicknames.keySet()) {
			String nwithoutformatting = nickname;
			int index;
			while ((index = nwithoutformatting.indexOf("�k")) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("�k"
						+ nwithoutformatting.charAt(index + 2), ""); // Support
																		// for
																		// one
																		// random
																		// char
			while ((index = nwithoutformatting.indexOf('�')) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("�"
						+ nwithoutformatting.charAt(index + 1), "");
			if (nwithoutformatting.startsWith(name)
					&& !nwithoutformatting.equals(Bukkit.getPlayer(
							nicknames.get(nickname)).getName()))
				e.getTabCompletions().add(nwithoutformatting);
		}
	}

	public static boolean DebugMode = false;

	public void SendForDebug(String message) {
		if (DebugMode) {
			for (Player player : PluginMain.GetPlayers()) {
				if (player.getName().equals("NorbiPeti")) {
					player.sendMessage("[DEBUG] " + message);
					break;
				}
			}
		}
	}

	private boolean ActiveF = false;
	private int FCount = 0;

	@EventHandler
	public void onPlayerMessage(AsyncPlayerChatEvent e) {
		if (e.getMessage().equalsIgnoreCase("F")) {
			MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(e
					.getPlayer().getUniqueId());
			if (!mp.PressedF && ActiveF) {
				FCount++;
				mp.PressedF = true;
			}
		}

		if (e.getMessage().startsWith(">"))
			e.setMessage("�2" + e.getMessage());

		/*
		 * Channel c = ((Chat) Bukkit.getPluginManager().getPlugin("TownyChat"))
		 * .getChannelsHandler().getActiveChannel(e.getPlayer(),
		 * channelTypes.GLOBAL);
		 */
		/*
		 * List<String> modes = ((Chat) Bukkit.getPluginManager().getPlugin(
		 * "TownyChat")).getTowny().getPlayerMode(e.getPlayer()); if
		 * (modes.size() == 0 || modes.contains("g")) {
		 */
		if (e.getFormat().contains("[g]")) {
			StringBuilder sb = new StringBuilder();
			sb.append("tellraw @a [\"\"");
			sb.append(",{\"text\":\"Hashtags:\"}");
			int index = -1;
			ArrayList<String> list = new ArrayList<String>();
			while ((index = e.getMessage().indexOf("#", index + 1)) != -1) {
				int index2 = e.getMessage().indexOf(" ", index + 1);
				if (index2 == -1)
					index2 = e.getMessage().length();
				int index3 = e.getMessage().indexOf("#", index + 1);
				if (index3 != -1 && index3 < index2) // A # occurs before a
														// space
					index2 = index3;
				String original = e.getMessage().substring(index, index2);
				list.add(original);
				sb.append(",{\"text\":\" \"}");
				sb.append(",{\"text\":\"");
				sb.append(original);
				sb.append("\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://twitter.com/hashtag/");
				sb.append(original.substring(1));
				sb.append("\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open on Twitter\",\"color\":\"blue\"}]}}}");
			}
			for (String original : list)
				e.setMessage(e.getMessage().replace(
						original,
						"�9"
								+ original
								+ (e.getMessage().startsWith("�2>") ? "�2"
										: "�r")));
			/*
			 * for (String original : list)
			 * System.out.println(e.getMessage().replace( original, "�9" +
			 * original + (e.getMessage().startsWith("�2>") ? "�2" : "�r")));
			 */

			sb.append("]");

			if (list.size() > 0)
				PluginMain.Instance.getServer().dispatchCommand(
						PluginMain.Instance.getServer().getConsoleSender(),
						sb.toString());
		}
	}

	private Timer Ftimer;

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (!Minigames.plugin.pdata.getMinigamePlayer(e.getEntity())
				.isInMinigame() && new Random().nextBoolean()) {
			if (Ftimer != null)
				Ftimer.cancel();
			ActiveF = true;
			FCount = 0;
			for (Player p : PluginMain.GetPlayers()) {
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(p
						.getUniqueId());
				mp.PressedF = false;
				p.sendMessage("�bPress F to pay respects.�r");
			}
			Ftimer = new Timer();
			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					if (ActiveF) {
						ActiveF = false;
						for (Player p : PluginMain.GetPlayers()) {
							p.sendMessage("�b" + FCount + " "
									+ (FCount == 1 ? "person" : "people")
									+ " paid their respects.�r");
						}
					}
				}
			};
			Ftimer.schedule(tt, 15 * 1000);
		}
	}
}
