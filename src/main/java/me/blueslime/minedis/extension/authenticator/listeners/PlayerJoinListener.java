package me.blueslime.minedis.extension.authenticator.listeners;

import me.blueslime.minedis.extension.authenticator.MStaffAuthenticator;
import me.blueslime.minedis.extension.authenticator.utils.CodeGenerator;
import me.blueslime.minedis.extension.authenticator.utils.EmbedSection;
import me.blueslime.minedis.utils.player.PlayerTools;
import me.blueslime.minedis.utils.text.TextReplacer;
import me.blueslime.minedis.utils.text.TextUtilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerJoinListener implements Listener {
    private final Map<UUID, ScheduledTask> taskMap = new HashMap<>();
    private final MStaffAuthenticator extension;

    public PlayerJoinListener(MStaffAuthenticator extension) {
        this.extension = extension;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();


        Configuration minecraft = extension.getMinecraftStorage();

        Configuration settings = extension.getConfiguration();

        if (player.hasPermission(extension.getConfiguration().getString("settings.auth.permission", "staffauth.need"))) {
            if (minecraft.contains("storage.id." + player.getName())) {
                try {
                    Object minecraftData = minecraft.get("storage.ip." + player.getName());

                    if (minecraftData == null || !minecraftData.toString().equalsIgnoreCase(PlayerTools.getIP(player))) {

                        if (settings.getString("settings.auth.guild", "NOT_SET").equalsIgnoreCase("NOT_SET")) {
                            extension.getLogger().info("Warning! GUILD is not set, the extension is not generating codes...");
                            return;
                        }

                        String code = CodeGenerator.generate(
                            10
                        );

                        extension.getCache("mstaff-mc-codes").set(
                            player.getUniqueId(),
                            code
                        );

                        Guild guild = extension.getJDA().getGuildById(
                            settings.getString("settings.auth.guild", "0")
                        );

                        if (guild == null) {
                            extension.getLogger().info("Warning! This guild id was not found: " + settings.getString("settings.auth.guild", "NOT_SET"));
                            return;
                        }

                        guild.retrieveMemberById(
                            minecraft.getString("storage.id." + player.getName(), "0")
                        ).queue(
                            member -> {
                                if (member != null) {
                                    member.getUser().openPrivateChannel().queue(
                                        channel -> {
                                            if (channel != null && channel.canTalk()) {
                                                TextReplacer replacer = TextReplacer.builder()
                                                    .replace("%ip_current%", PlayerTools.getIP(player))
                                                    .replace("%ip_last%", (minecraftData == null) ? settings.getString("settings.auth.address-not-found", "Not yet") : minecraftData.toString())
                                                    .replace("%command%", "/" + settings.getString("settings.auth.command", "staffcode"))
                                                    .replace("%code%", code)
                                                    .replace("%nick%", player.getName())
                                                    .replace("%name%", player.getName());

                                                if (settings.getBoolean("settings.auth.formats.with-embed.enabled")) {
                                                    channel.sendMessageEmbeds(
                                                        new EmbedSection(
                                                            settings.getSection("settings.auth.formats.with-embed")
                                                        ).build(
                                                            replacer
                                                        )
                                                    ).queue();
                                                } else {
                                                    channel.sendMessage(
                                                        replacer.apply(
                                                            settings.getString(
                                                                "settings.auth.formats.without-embed.message",
                                                                "(Old Address: **%ip_last%** New Address: **%ip_current%**) %nick%, use this command in-game: **%command% %code%**"
                                                            )
                                                        )
                                                    ).queue();
                                                }
                                            } else {
                                                player.disconnect(
                                                    TextUtilities.component(
                                                        settings.getString(
                                                            "settings.auth.md-disabled", "&cThis current discord user linked to this account has the MD disabled."
                                                        )
                                                    )
                                                );
                                            }
                                        }
                                    );
                                }
                            }
                        );
                    } else {
                        player.sendMessage(
                            ChatMessageType.ACTION_BAR,
                            TextUtilities.component(
                                settings.getString("settings.auth.logged", "&aNow you are logged")
                            )
                        );
                    }
                } catch (Exception ignored) {
                    player.disconnect(
                        TextUtilities.component(
                            settings.getString(
                                "settings.auth.md-disabled", "&cThis current discord user linked to this account has the MD disabled."
                            )
                        )
                    );
                }
            } else {
                if (settings.getBoolean("settings.auth.prevent-join-without-linked-account", true)) {
                    String code = CodeGenerator.generate(
                            10
                    );

                    extension.getCache("mstaff-mc-codes").set(
                            player.getUniqueId(),
                            code
                    );

                    taskMap.put(player.getUniqueId(), extension.getProxy().getScheduler().schedule(
                            extension.getPlugin(),
                            new Runnable() {

                                int seconds = settings.getInt("settings.auth.prevent-join-without-linked-account-timer", 30);

                                @Override
                                public void run() {
                                    if (!player.isConnected()) {
                                        cancel(player.getUniqueId());
                                        return;
                                    }
                                    if (extension.getCache("mstaff-mc-codes").contains(player.getUniqueId())) {
                                        String code = (String)extension.getCache("mstaff-mc-codes").get(player.getUniqueId());
                                        if (extension.getCache("mstaff-discord").contains(code)) {
                                            player.sendMessage(
                                                    ChatMessageType.ACTION_BAR,
                                                    TextUtilities.component(
                                                            "&aWaiting code"
                                                    )
                                            );
                                            cancel(player.getUniqueId());
                                        }
                                        return;
                                    }
                                    player.sendMessage(
                                            ChatMessageType.ACTION_BAR,
                                            TextUtilities.component(
                                                    "&6" + seconds
                                            )
                                    );
                                    seconds--;
                                    if (seconds <= 0) {
                                        player.disconnect(
                                            TextUtilities.component(
                                                settings.getString(
                                                    "settings.auth.not-registered", "&cYour discord account is not registered in database"
                                                )
                                            )
                                        );
                                        cancel(player.getUniqueId());
                                    }
                                }
                            },
                            0,
                            1,
                            TimeUnit.SECONDS
                    ));


                    return;
                }
            }
            if (player.isConnected() && settings.getBoolean("settings.logs.join-log.enabled", true)) {
                String channelID = settings.getString("settings.logs.join-log.channel-id", "NOT_SET");
                String guildID = settings.getString("settings.logs.join-log.guild-id", "NOT_SET");

                if (channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET")) {
                    return;
                }

                Guild announce = extension.getJDA().getGuildById(guildID);

                if (announce == null) {
                    extension.getLogger().info("Guild-ID was not found for join logs: " + guildID);
                    return;
                }

                TextChannel textChannel = announce.getTextChannelById(
                    channelID
                );

                if (textChannel == null) {
                    extension.getLogger().info("Channel for join logs was not found: " + channelID);
                    return;
                }

                if (player.isConnected()) {
                    textChannel.sendMessageEmbeds(
                        new EmbedSection(
                            settings.getSection("settings.logs.join-log")
                        ).build(
                            TextReplacer.builder()
                                .replace("%nick%", player.getName())
                                .replace("%name%", player.getName())
                                .replace("%uuid%", player.getUniqueId().toString())
                                .replace("%id%", player.getUniqueId().toString().replace("-", ""))
                        )
                    ).queue();
                }
            }
        }
    }

    public void cancel(UUID uuid) {
        ScheduledTask task = taskMap.get(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}
