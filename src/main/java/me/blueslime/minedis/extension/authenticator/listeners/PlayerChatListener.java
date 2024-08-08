package me.blueslime.minedis.extension.authenticator.listeners;

import me.blueslime.minedis.extension.authenticator.MStaffAuthenticator;
import me.blueslime.minedis.extension.authenticator.utils.EmbedSection;
import me.blueslime.minedis.extension.authenticator.utils.StringUtil;
import me.blueslime.minedis.utils.player.PlayerTools;
import me.blueslime.minedis.utils.text.TextReplacer;
import me.blueslime.minedis.utils.text.TextUtilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

public class PlayerChatListener implements Listener {
    private final MStaffAuthenticator main;

    public PlayerChatListener(MStaffAuthenticator main) {
        this.main = main;
    }

    @EventHandler
    public void on(ChatEvent event) {

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        String command = main.getConfiguration().getString("settings.auth.command", "staffcode");

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (main.getCache("mstaff-mc-codes").contains(player.getUniqueId()) || main.getCodeCache().contains(player.getUniqueId())) {

            if (event.getMessage().contains("login ") || event.getMessage().contains("register ")) {
                if (!event.getMessage().contains(command + " ")) {
                    return;
                }
            }

            event.setCancelled(true);

            if (event.isCommand()) {
                if (event.getMessage().contains(command + " ")) {

                    String code = (String)main.getCache("mstaff-mc-codes").get(player.getUniqueId());

                    if (code == null) {
                        code = main.getCodeCache().get(player.getUniqueId());
                    }

                    if (event.getMessage().contains(code)) {

                        main.getCache("mstaff-mc-codes").remove(player.getUniqueId());
                        main.getCodeCache().remove(player.getUniqueId());

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        try (DataOutputStream out = new DataOutputStream(stream)) {
                            out.writeUTF(player.getUniqueId().toString() + ":completed");
                            player.getServer().sendData(MStaffAuthenticator.MESSAGE_CHANNEL, stream.toByteArray());
                        } catch (Exception ignored) { }

                        Configuration settings = main.getConfiguration();

                        String channelID = settings.getString("settings.logs.fail-login-attempt-log.channel-id", "NOT_SET");
                        String guildID = settings.getString("settings.logs.fail-login-attempt-log.guild-id", "NOT_SET");

                        Guild announce = channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET") ? null : main.getJDA().getGuildById(guildID);

                        if (announce == null) {
                            main.getLogger().info("Guild-ID was not found for chat logs: " + guildID);
                        }

                        TextChannel textChannel = announce != null ? announce.getTextChannelById(
                            channelID
                        ) : null;

                        if (textChannel == null) {
                            main.getLogger().info("Channel for chat logs was not found: " + channelID);
                        }

                        if (textChannel != null) {
                            textChannel.sendMessageEmbeds(
                                new EmbedSection(
                                    settings.getSection("settings.logs.login-event-log")
                                ).build(
                                    TextReplacer.builder()
                                        .replace("%nick%", player.getName())
                                        .replace("%command%", event.getMessage())
                                        .replace("%name%", player.getName())
                                        .replace("%uuid%", player.getUniqueId().toString())
                                        .replace("%ip%", PlayerTools.getIP(player))
                                        .replace("%id%", player.getUniqueId().toString().replace("-", ""))
                                )
                            ).queue();
                        }


                        if (main.getCache("mstaff-discord").contains(code)) {
                            String user = (String)main.getCache("mstaff-discord").get(code);

                            main.getMinecraftStorage().set(
                                    "storage.id." + player.getName(),
                                    user
                            );
                            main.getDiscordStorage().set(
                                    "storage.id." + user, player.getName()
                            );

                            main.getCache("mstaff-discord").remove(code);

                            player.sendMessage(
                                TextUtilities.component(
                                    main.getConfiguration().getString(
                                        "settings.commands.link.account-linked",
                                        "&aNow your minecraft account has been linked to your discord account ;)"
                                    )
                                )
                            );
                        }

                        player.sendMessage(
                            TextUtilities.component(
                                main.getConfiguration().getString(
                                    "settings.auth.welcome",
                                    "&aWelcome!&f Your IP Address has been registered."
                                )
                            )
                        );

                        String IP = PlayerTools.getIP(player);

                        main.getMinecraftStorage().set("storage.ip." + player.getName(), IP);

                        List<String> ipList = StringUtil.toStringList(main.getStaffStoredData().getList(player.getName() + ".success"));

                        if (!ipList.contains(IP)) {
                            ipList.add(IP);
                            main.getStaffStoredData().set(player.getName() + ".success", ipList);
                        }

                        main.saveDatabase();

                        main.reloadDatabase();
                        return;
                    } else {
                        Configuration settings = main.getConfiguration();

                        String channelID = settings.getString("settings.logs.fail-login-attempt-log.channel-id", "NOT_SET");
                        String guildID = settings.getString("settings.logs.fail-login-attempt-log.guild-id", "NOT_SET");

                        Guild announce = channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET") ? null : main.getJDA().getGuildById(guildID);

                        if (announce == null) {
                            main.getLogger().info("Guild-ID was not found for chat logs: " + guildID);
                        }

                        TextChannel textChannel = announce != null ? announce.getTextChannelById(
                            channelID
                        ) : null;

                        if (textChannel == null) {
                            main.getLogger().info("Channel for chat logs was not found: " + channelID);
                        }

                        if (textChannel != null) {
                            textChannel.sendMessageEmbeds(
                                new EmbedSection(
                                    settings.getSection("settings.logs.fail-login-attempt-log")
                                ).build(
                                    TextReplacer.builder()
                                        .replace("%nick%", player.getName())
                                        .replace("%command%", "/" + event.getMessage())
                                        .replace("%name%", player.getName())
                                        .replace("%uuid%", player.getUniqueId().toString())
                                        .replace("%id%", player.getUniqueId().toString().replace("-", ""))
                                )
                            ).queue();
                        }
                    }
                }
                player.sendMessage(
                    TextUtilities.component(
                        main.getConfiguration().getString(
                            "settings.auth.login-msg", "&6Staff > &eYou are not logged yet."
                        )
                    )
                );
                if (main.getConfiguration().getBoolean("settings.logs.fail-login-attempt-log.enabled", true)) {
                    Configuration settings = main.getConfiguration();

                    String channelID = settings.getString("settings.logs.fail-login-attempt-log.channel-id", "NOT_SET");
                    String guildID = settings.getString("settings.logs.fail-login-attempt-log.guild-id", "NOT_SET");

                    Guild announce = channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET") ? null : main.getJDA().getGuildById(guildID);

                    if (announce == null) {
                        main.getLogger().info("Guild-ID was not found for chat logs: " + guildID);
                    }

                    TextChannel textChannel = announce != null ? announce.getTextChannelById(
                        channelID
                    ) : null;

                    if (textChannel == null) {
                        main.getLogger().info("Channel for chat logs was not found: " + channelID);
                    }

                    if (player.isConnected()) {

                        String IP = PlayerTools.getIP(player);

                        List<String> ipList = StringUtil.toStringList(main.getStaffStoredData().getList(player.getName() + ".failed"));

                        if (!ipList.contains(IP)) {
                            ipList.add(IP);
                            main.getStaffStoredData().set(player.getName() + ".failed", ipList);
                        }

                        main.saveDatabase();
                        main.reloadDatabase();

                        if (textChannel != null) {
                            textChannel.sendMessageEmbeds(
                                new EmbedSection(
                                    settings.getSection("settings.logs.fail-login-attempt-log")
                                ).build(
                                    TextReplacer.builder()
                                        .replace("%nick%", player.getName())
                                        .replace("%command%", "/" + event.getMessage())
                                        .replace("%name%", player.getName())
                                        .replace("%uuid%", player.getUniqueId().toString())
                                        .replace("%id%", player.getUniqueId().toString().replace("-", ""))
                                )
                            ).queue();
                        }
                    }
                }
            }
            return;
        }

        if (player.hasPermission(main.getConfiguration().getString("settings.auth.permission", "staffauth.need"))) {
            Configuration settings = main.getConfiguration();

            String channelID = settings.getString("settings.logs.fail-login-attempt-log.channel-id", "NOT_SET");
            String guildID = settings.getString("settings.logs.fail-login-attempt-log.guild-id", "NOT_SET");

            if (channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET")) {
                return;
            }

            Guild announce = channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET") ? null : main.getJDA().getGuildById(guildID);

            if (announce == null) {
                main.getLogger().info("Guild-ID was not found for chat logs: " + guildID);
            }

            TextChannel textChannel = announce != null ? announce.getTextChannelById(
                channelID
            ) : null;

            if (textChannel == null) {
                main.getLogger().info("Channel for chat logs was not found: " + channelID);
            }

            if (textChannel != null) {
                textChannel.sendMessageEmbeds(
                    new EmbedSection(
                        settings.getSection("settings.logs.chat-use-log")
                    ).build(
                        TextReplacer.builder()
                            .replace("%nick%", player.getName())
                            .replace("%command%", event.getMessage())
                            .replace("%name%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString())
                            .replace("%ip%", PlayerTools.getIP(player))
                            .replace("%id%", player.getUniqueId().toString().replace("-", ""))
                    )
                ).queue();
            }
        }
        if (event.getMessage().contains(command + " ")) {
            event.setCancelled(true);
        }
    }
}
