package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class CheckPlayerCommand extends Command implements LockLoginBungee, BungeeFiles {

    /**
     * Initialize player info command
     */
    public CheckPlayerCommand() {
        super("playerinf", "", "playerinfo, playerinformation");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        final String checkPlayerInfo = "locklogin.playerinfo";
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (player.hasPermission(checkPlayerInfo)) {
                if (args.length == 1) {
                    String tar = args[0];

                    sendData(player, tar);
                } else {
                    user.send(messages.prefix() + messages.playerInfoUsage());
                }
            } else {
                user.send(messages.prefix() + messages.permission(checkPlayerInfo));
            }
        } else {
            if (args.length == 1) {
                String tar = args[0];

                sendData(tar);
            } else {
                Console.send(messages.prefix() + messages.playerInfoUsage());
            }
        }
    }

    /**
     * Fill the player data
     *
     * @param issuer the command issuer
     * @param tName  the player
     */
    private void sendData(ProxiedPlayer issuer, String tName) {
        User user = new User(issuer);

        user.send("&6&l&m------&r &eLockLogin &6&l&m------");
        user.send(" ");
        user.send("&ePlayer&7: &f" + tName);
        user.send("&eUUID&7: &f" + getUUId(tName).toString());
        user.send("&eTrimmed&7: &f" + getUUId(tName).toString().replace("-", ""));
        if (isOnline(tName)) {
            ProxiedPlayer target = plugin.getProxy().getPlayer(tName);
            User targetUser = new User(target);

            user.send("&eIP&7: &f" + Objects.requireNonNull(User.external.getIp(target.getSocketAddress())).getHostName());
            if (!targetUser.isLogged()) {
                if (targetUser.isRegistered()) {
                    user.send("&eStatus&7: &cNot logged");
                } else {
                    user.send("&eStatus&7: &cNot registered");
                }
            } else {
                if (targetUser.has2FA()) {
                    if (!targetUser.isTempLog()) {
                        user.send("&eStatus&7: &aVerified with 2FA");
                    } else {
                        user.send("&eStatus&7: &cNeeds 2FA");
                    }
                } else {
                    user.send("&eStatus&7: &aVerified");
                }
            }
            user.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                    .replace("false", "&cNo"));
            if (targetUser.has2FA()) {
                if (targetUser.getToken(false).isEmpty()) {
                    user.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                } else {
                    user.send("&eToken&7: &aTarget google token found");
                }
            }

            user.send("&eFly&7: &f:BUNGEECORD");
            user.send("&eGamemode&7: &fBUNGEECORD");
            user.send("&eServer&7: &f" + target.getServer().getInfo().getName());
        } else {
            OfflineUser targetUser = new OfflineUser("", tName, true);

            if (targetUser.exists()) {
                user.send("&eIP&7: &cDISCONNECTED");
                user.send("&eStatus&7: &cNot logged");
                user.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                        .replace("false", "&cNo"));
                if (targetUser.has2FA()) {
                    if (targetUser.getToken().isEmpty()) {
                        user.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                    } else {
                        user.send("&eToken&7: &aTarget google token found");
                    }
                }
                user.send("&eFly&7: &fBUNGEECORD");
                user.send("&eGamemode&7: &cDISCONNECTED");
                user.send("&eServer&7: &cDISCONNECTED");
            } else {
                user.send(messages.prefix() + messages.unknownPlayer(tName));
            }
        }
    }

    /**
     * Fill the player data
     *
     * @param tName the player
     */
    private void sendData(String tName) {
        Console.send("&6&l&m------&r &eLockLogin &6&l&m------");
        Console.send(" ");
        Console.send("&ePlayer&7: &f" + tName);
        Console.send("&eUUID&7: &f" + getUUId(tName).toString());
        Console.send("&eTrimmed&7: &f" + getUUId(tName).toString().replace("-", ""));
        if (isOnline(tName)) {
            ProxiedPlayer target = plugin.getProxy().getPlayer(tName);
            User targetUser = new User(target);

            Console.send("&eIP&7: &f" + Objects.requireNonNull(User.external.getIp(target.getSocketAddress())).getHostName());
            if (!targetUser.isLogged()) {
                if (targetUser.isRegistered()) {
                    Console.send("&eStatus&7: &cNot logged");
                } else {
                    Console.send("&eStatus&7: &cNot registered");
                }
            } else {
                if (targetUser.has2FA()) {
                    if (!targetUser.isTempLog()) {
                        Console.send("&eStatus&7: &aVerified with 2FA");
                    } else {
                        Console.send("&eStatus&7: &cNeeds 2FA");
                    }
                } else {
                    Console.send("&eStatus&7: &aVerified");
                }
            }
            Console.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                    .replace("false", "&cNo"));
            if (targetUser.has2FA()) {
                if (targetUser.getToken(false).isEmpty()) {
                    Console.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                } else {
                    Console.send("&eToken&7: &aTarget google token found");
                }
            }
            Console.send("&eFly&7: &7: &fBUNGEECORD");
            Console.send("&eGamemode&7: &fBUNGEECORD");
            Console.send("&eServer&7: &f" + target.getServer().getInfo().getName());
        } else {
            OfflineUser targetUser = new OfflineUser("", tName, true);

            if (targetUser.exists()) {
                Console.send("&eIP&7: &cDISCONNECTED");
                Console.send("&eStatus&7: &cNot logged");
                Console.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                        .replace("false", "&cNo"));
                if (targetUser.has2FA()) {
                    if (targetUser.getToken().isEmpty()) {
                        Console.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                    } else {
                        Console.send("&eToken&7: &aTarget google token found");
                    }
                }
                Console.send("&eFly&7: &fBUNGEECORD");
                Console.send("&eGamemode&7: &fBUNGEECORD");
                Console.send("&eServer&7: &cDISCONNECTED");
            } else {
                Console.send(messages.prefix() + messages.unknownPlayer(tName));
            }
        }
    }

    /**
     * Check if the target is online
     *
     * @param name the player name
     * @return if the player is online
     */
    private boolean isOnline(String name) {
        return plugin.getProxy().getPlayer(name) != null && plugin.getProxy().getPlayer(name).isConnected();
    }

    /**
     * Get an uuid from the name
     *
     * @param name the name
     * @return the player UUID
     */
    private UUID getUUId(String name) {
        if (plugin.getProxy().getPlayer(name) != null) {
            return plugin.getProxy().getPlayer(name).getUniqueId();
        } else {
            if (!plugin.getProxy().getConfig().isOnlineMode()) {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            } else {
                return mojangUUID(name);
            }
        }
    }

    /**
     * Get the mojang player uuid
     *
     * @param name the player name
     * @return the player mojang UUID
     */
    private UUID mojangUUID(String name) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

            String UUIDJson = IOUtils.toString(new URL(url));

            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);

            return UUID.fromString(UUIDObject.get("id").toString());
        } catch (Throwable e) {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }
    }
}
