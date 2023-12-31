package me.blueslime.minedis.extension.authenticator;

import me.blueslime.minedis.api.MinedisAPI;
import me.blueslime.minedis.api.extension.MinedisExtension;
import me.blueslime.minedis.extension.authenticator.cache.CodeCache;
import me.blueslime.minedis.extension.authenticator.cache.DiscordCache;
import me.blueslime.minedis.extension.authenticator.listeners.DiscordCommandListener;
import me.blueslime.minedis.extension.authenticator.listeners.PlayerChatListener;
import me.blueslime.minedis.extension.authenticator.listeners.PlayerJoinListener;
import me.blueslime.minedis.extension.authenticator.listeners.PlayerQuitListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class MStaffAuthenticator extends MinedisExtension {
    private final ArrayList<String> commandList = new ArrayList<>();
    private final DiscordCache discordCache = new DiscordCache();
    private final CodeCache codes = new CodeCache();

    @Override
    public String getIdentifier() {
        return "MStaffAuthenticator";
    }

    @Override
    public String getName() {
        return "Staff Authenticator";
    }

    private Configuration minecraftStorage;
    private Configuration discordStorage;

    @Override
    public void onEnabled() {
        getLogger().info("Loading Staff Authenticator extension v1.0.0");

        registerCache(codes);
        registerCache(discordCache);

        if (!getConfiguration().contains("settings.auth")) {
            getConfiguration().set("settings.auth.prevent-join-without-linked-account", true);
            getConfiguration().set("settings.auth.address-not-found", "Not yet");
            getConfiguration().set("settings.auth.not-registered", "&cYour discord account is not registered in database");
            getConfiguration().set("settings.auth.md-disabled", "&cThis current discord user linked to this account has the MD disabled.");
            getConfiguration().set("settings.auth.permission", "staffauth.need");
            getConfiguration().set("settings.auth.login-msg", "&6Staff > &eYou are not logged yet.");
            getConfiguration().set("settings.auth.welcome", "&aWelcome! &fYour IP Address has been registered to our database for auto-join");
            getConfiguration().set("settings.auth.command", "staffcode");
            getConfiguration().set("settings.auth.logged", "&aNow you are logged");
            getConfiguration().set("settings.auth.guild", "NOT_SET");


            if (!getConfiguration().contains("settings.auth.formats.without-embed.message")) {
                getConfiguration().set("settings.auth.formats.without-embed.message", "(Old Address: **%ip_last%** New Address: **%ip_current%**) %nick%, use this command in-game: **%command% %code%**");
            }
            String embedPath = "settings.auth.formats.with-embed.";
            if (!getConfiguration().contains(embedPath + "enabled")) {
                getConfiguration().set(embedPath + "enabled", true);
                getConfiguration().set(embedPath + "title", "Security System by Minedis with StaffAuthenticator Extension");
                getConfiguration().set(embedPath + "description", "**%nick%** are you trying to join to the server? if you are trying to join to the server, use this command in the chat for start playing: **%command% %code%**");
                getConfiguration().set(embedPath + "color", "YELLOW");
                getConfiguration().set(embedPath + "image", "https://i.imgur.com/OmeC6Xg.gif");
                getConfiguration().set(embedPath + "thumbnail", "https://cravatar.eu/helmhead/%nick%.png");
                getConfiguration().set(embedPath + "fields.address.inline", true);
                getConfiguration().set(embedPath + "fields.address.name", "Your Address");
                getConfiguration().set(embedPath + "fields.address.value", "Old: %ip_last%, Current: %ip_current%");
                getConfiguration().set(embedPath + "footer", "mc.spigotmc.org");
            }
        }

        if (!getConfiguration().contains("settings.auth.prevent-join-without-linked-account-timer")) {
            getConfiguration().set("settings.auth.prevent-join-without-linked-account-timer", 30);
        }

        String embedPath = "settings.logs.join-log.";
        if (!getConfiguration().contains(embedPath + "enabled")) {
            getConfiguration().set(embedPath + "enabled", true);
            getConfiguration().set(embedPath + "guild-id", "NOT_SET");
            getConfiguration().set(embedPath + "channel-id", "NOT_SET");
            getConfiguration().set(embedPath + "title", "Join Logs - StaffAuthenticator Extension");
            getConfiguration().set(embedPath + "description", "**%nick%** joined to the server");
            getConfiguration().set(embedPath + "color", "GREEN");
            getConfiguration().set(embedPath + "thumbnail", "https://cravatar.eu/helmhead/%nick%.png");
            getConfiguration().set(embedPath + "footer", "mc.spigotmc.org");
        }

        embedPath = "settings.logs.fail-login-attempt-log.";
        if (!getConfiguration().contains(embedPath + "enabled")) {
            getConfiguration().set(embedPath + "enabled", true);
            getConfiguration().set(embedPath + "guild-id", "NOT_SET");
            getConfiguration().set(embedPath + "channel-id", "NOT_SET");
            getConfiguration().set(embedPath + "title", "Command & Chat Logs - StaffAuthenticator Extension");
            getConfiguration().set(embedPath + "description", "**%nick%** is trying to use a command or the chat without being logged: **%command%**");
            getConfiguration().set(embedPath + "color", "BLUE");
            getConfiguration().set(embedPath + "footer", "mc.spigotmc.org");
        }

        embedPath = "settings.logs.quit-log.";
        if (!getConfiguration().contains(embedPath + "enabled")) {
            getConfiguration().set(embedPath + "enabled", true);
            getConfiguration().set(embedPath + "guild-id", "NOT_SET");
            getConfiguration().set(embedPath + "channel-id", "NOT_SET");
            getConfiguration().set(embedPath + "title", "Quit Log - StaffAuthenticator Extension");
            getConfiguration().set(embedPath + "description", "**%nick%** quit from the server");
            getConfiguration().set(embedPath + "color", "RED");
            getConfiguration().set(embedPath + "thumbnail", "https://cravatar.eu/helmhead/%nick%.png");
            getConfiguration().set(embedPath + "footer", "mc.spigotmc.org");
        }

        embedPath = "settings.commands.link.";
        if (!getConfiguration().contains(embedPath + "command")) {
            getConfiguration().set(embedPath + "guild-id", "NOT_SET");
            getConfiguration().set(embedPath + "command", "link");
            getConfiguration().set(embedPath + "command-description", "Link your MC account with your Discord account");
            getConfiguration().set(embedPath + "player-is-not-online", "This player is not online.");
            getConfiguration().set(embedPath + "check-md", "Check your MD");
            getConfiguration().set(embedPath + "account-linked", "&aNow your minecraft account has been linked to your discord account ;)");
            getConfiguration().set(embedPath + "player-message", "&aDiscord account %discord% is trying to link your username to his discord, please use the command on your discord account to confirm.");
            getConfiguration().set(embedPath + "already", "Your account is already linked to minecraft account.");
            getConfiguration().set(embedPath + "title", "Link Account - StaffAuthenticator Extension");
            getConfiguration().set(embedPath + "description", "You are trying to link your discord account with minecraft account: **%nick%**, please use the next command in-game: **%command% %code%**");
            getConfiguration().set(embedPath + "color", "BLUE");
            getConfiguration().set(embedPath + "thumbnail", "https://cravatar.eu/helmhead/%nick%.png");
            getConfiguration().set(embedPath + "footer", "mc.spigotmc.org");
        }

        reloadDatabase();

        registerMinecraftListeners(
                new PlayerJoinListener(this),
                new PlayerQuitListener(this),
                new PlayerChatListener(this)
        );

        registerEventListeners(
                new DiscordCommandListener(this)
        );

        saveConfiguration();

        String guildID = getConfiguration().getString(embedPath + "guild-id", "NOT_SET");

        if (guildID.isEmpty() || guildID.equalsIgnoreCase("NOT_SET")) {
            getLogger().info("Can't register link command because discord guild id was not set yet.");
            return;
        }

        Guild guild = getJDA().getGuildById(guildID);

        if (guild == null) {
            getLogger().info("Discord GUILD was not found for link command.");
            return;
        }

        guild.upsertCommand(
            Commands.slash(
                getConfiguration().getString(embedPath + "command", "link"),
                getConfiguration().getString(embedPath + "command-description", "Link your MC account with your discord account")
            ).addOption(
                OptionType.STRING,
                "username",
                "Nick of your user",
                true
            )
        ).queue(cmd -> commandList.add(cmd.getId()));
    }

    public void saveDatabase() {
        MinedisAPI api = MinedisAPI.get();
        if (api != null) {
            File extensions = api.getDirectoryFile("extensions");
            File folder = new File(extensions, getIdentifier());
            boolean load = folder.exists() || folder.mkdirs();
            if (load) {
                File file1 = new File(folder, "minecraft-storage.yml");
                File file2 = new File(folder, "discord-storage.yml");

                try {
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(
                            minecraftStorage, file1
                    );
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(
                            discordStorage, file2
                    );
                } catch (IOException ignored) {}
            }
        }
    }

    public void reloadDatabase() {
        MinedisAPI api = MinedisAPI.get();
        if (api == null) {
            minecraftStorage = new Configuration();
            discordStorage = new Configuration();
        } else {
            File extensions = api.getDirectoryFile("extensions");
            File folder = new File(extensions, getIdentifier());
            boolean load = folder.exists() || folder.mkdirs();
            if (load) {
                File file1 = new File(folder, "minecraft-storage.yml");
                File file2 = new File(folder, "discord-storage.yml");

                try {
                    if (!file1.exists()) {
                        if (!file1.createNewFile()) {
                            minecraftStorage = new Configuration();
                        }
                    }
                    if (!file2.exists()) {
                        if (!file2.createNewFile()) {
                            discordStorage = new Configuration();
                        }
                    }
                    if (file1.exists() && file2.exists()) {
                        minecraftStorage = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file1);
                        discordStorage = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file2);
                    }
                } catch (Exception ignored) {
                    minecraftStorage = new Configuration();
                    discordStorage = new Configuration();
                }
            } else {
                minecraftStorage = new Configuration();
                discordStorage = new Configuration();
            }
        }
    }

    public Configuration getMinecraftStorage() {
        return minecraftStorage;
    }

    public Configuration getDiscordStorage() {
        return discordStorage;
    }

    @Override
    public void onDisable() {
        getLogger().info("All listeners are unloaded from MStaffAuthenticator");

        String guildID = getConfiguration().getString("settings.commands.link.guild-id", "NOT_SET");

        if (guildID.isEmpty() || guildID.equalsIgnoreCase("NOT_SET")) {
            getLogger().info("Can't register link command because discord guild id was not set yet.");
            return;
        }

        Guild guild = getJDA().getGuildById(guildID);

        if (guild == null) {
            getLogger().info("Discord GUILD was not found for link command.");
            return;
        }

        commandList.forEach(command -> guild.deleteCommandById(command).queue());

        commandList.clear();
    }
}
