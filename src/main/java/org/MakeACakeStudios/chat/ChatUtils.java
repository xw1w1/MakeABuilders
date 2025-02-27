package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChatUtils implements Listener {

    public static String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return messages.get(random.nextInt(messages.size()));
    }

    public static Component getFormattedPlayerComponent(Player player) {
        String badge = PlayerDataStorage.instance.getHighestBadge(player.getName());
        String prefix = MakeABuilders.instance.getPlayerPrefix(player);
        String suffix = MakeABuilders.instance.getPlayerSuffix(player);
        if (badge.isEmpty()) {
            return MiniMessage.miniMessage().deserialize(prefix + player.getName() + suffix);
        } else {
            return MiniMessage.miniMessage().deserialize(badge + " " + prefix + player.getName() + suffix);
        }
    }

    public static String getFormattedPlayerString(String playerName, Boolean value) {
        if (value) {
            String badge = PlayerDataStorage.instance.getHighestBadge(playerName);
            String prefix = PlayerDataStorage.instance.getPlayerPrefixByName(playerName);
            String suffix = PlayerDataStorage.instance.getPlayerSuffixByName(playerName);
            if (badge.isEmpty()) {
                return prefix + playerName + suffix;
            } else {
                return badge + " " + prefix + playerName + suffix;
            }
        } else {
            String prefix = PlayerDataStorage.instance.getPlayerPrefixByName(playerName);
            String suffix = PlayerDataStorage.instance.getPlayerSuffixByName(playerName);
            return prefix + playerName + suffix;
        }
    }

    public static void handleExternalCommandMessage(Player sender, String message) {
        String formattedMessage = TagFormatter.format(message);

        String playerName = getFormattedPlayerString(sender.getName(), false);
        String playerNameBadge = getFormattedPlayerString(sender.getName(), true);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String clickableName = "<click:suggest_command:'/msg " + sender.getName() + " '>" +
                        "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы отправить сообщение игроку " +
                        playerName + ".'>" +
                        playerNameBadge + "</hover></click>";

            String finalMessage = clickableName + " > " + formattedMessage;

            onlinePlayer.sendMessage(MiniMessage.miniMessage().deserialize(finalMessage));
        }
    }

    public static void broadcastMessage(String message) {
        String formattedMessage = TagFormatter.format(message);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(MiniMessage.miniMessage().deserialize(formattedMessage));
        }
    }

    public static String replaceLocationTag(OfflinePlayer sender, String message) {
        if (message.contains(":loc:")) {
            int x = sender.getLocation().getBlockX();
            int y = sender.getLocation().getBlockY();
            int z = sender.getLocation().getBlockZ();
            String worldName = sender.getLocation().getWorld().getName();

            String prefix = MakeABuilders.instance.getPlayerPrefix(sender);
            String suffix = MakeABuilders.instance.getPlayerSuffix(sender);
            String playerName = sender.getName();

            String formattedPlayerName = prefix + playerName + suffix;

            String color;
            switch (worldName) {
                case "world":
                    color = "<gradient:#00FF1A:#7EFF91>";
                    break;
                case "world_nether":
                    color = "<gradient:#FF0000:#FF7E7E>";
                    break;
                case "world_the_end":
                    color = "<gradient:#ED00FF:#DE7EFF>";
                    break;
                default:
                    color = "<gradient:#FFFFFF:#FFFFFF>";
            }

            String location = color + "<click:run_command:'/goto " + worldName + " " + x + " " + y + " " + z + "'>["
                    + x + "x/" + y + "y/" + z + "z, " + worldName + "]</click></gradient>";
            String locationHover = "<hover:show_text:'Координаты игрока " + formattedPlayerName + ".\nНажмите <green>ЛКМ</green> чтобы телепортироваться.'>" + location + "</hover>";

            return message.replace(":loc:", locationHover);
        }
        return message;
    }

    public static String replaceMentions(Player sender, String message) {
        if (message.contains("@")) {
            String[] words = message.split(" ");
            for (String word : words) {
                if (word.startsWith("@")) {
                    String mentionedPlayerName = word.substring(1);
                    Player mentionedPlayer = Bukkit.getPlayerExact(mentionedPlayerName);

                    if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                        mentionedPlayer.playSound(mentionedPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                        String playerFooter = ChatUtils.getFormattedPlayerString(mentionedPlayer.getName(), false);

                        String formattedMention = "<click:run_command:'/profile " + mentionedPlayer.getName() + "'>" + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть профиль игрока " + playerFooter + ".'>" + "<yellow>@" + mentionedPlayer.getName() + "</yellow></hover></click>";
                        message = message.replace(word, formattedMention);
                        return message;
                    } else {
                        String formattedMention = "<gray>@" + mentionedPlayerName + "</gray>";
                        message = message.replace(word, formattedMention);
                    }
                }
            }
        }
        return message;
    }

    public static String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(new Date(millis));
    }
}
