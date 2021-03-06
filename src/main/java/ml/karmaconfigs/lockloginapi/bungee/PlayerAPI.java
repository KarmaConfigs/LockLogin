package ml.karmaconfigs.lockloginapi.bungee;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginapi.bungee.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginapi.bungee.events.PlayerRegisterEvent;
import ml.karmaconfigs.lockloginmodules.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleUtil;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungee.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.servers.LobbyChecker;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
public class PlayerAPI implements LockLoginBungee, BungeeFiles {

    private final Module module;
    private final ProxiedPlayer player;
    private final BungeeSender dataSender = new BungeeSender();
    private AuthResult result = AuthResult.IDLE;

    /**
     * Initialize LockLogin bungee's API
     *
     * @param loader the module that is calling
     *               the API method
     * @param player the player
     */
    public PlayerAPI(final Module loader, ProxiedPlayer player) {
        module = loader;
        if (ModuleUtil.isLoaded(loader)) {
            logger.scheduleLog(Level.INFO, "Module " + loader.name() + " by " + loader.author() + " initialized PlayerAPI for " + player.getName());
            this.player = player;
        } else {
            this.player = null;
        }
    }

    /**
     * Mark the player as logged/un-logged with a message
     *
     * @param Value   true/false
     * @param Message the message
     */
    public final void setLogged(boolean Value, String Message) {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                if (Value) {
                    if (!utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " logged in " + player.getName());

                        if (utils.has2FA()) {
                            utils.setLogged(true);
                            utils.setTempLog(true);
                            utils.send(Message);
                            utils.send(messages.prefix() + messages.gAuthenticate());
                            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                                dataSender.sendAccountStatus(player);
                            }, (long) 1.5, TimeUnit.SECONDS);
                        } else {
                            utils.setLogged(true);
                            utils.setTempLog(false);
                            utils.send(Message);
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.mainOk() && checker.mainWorking()) {
                                utils.sendTo(checker.getMain());
                            }
                            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                                dataSender.sendAccountStatus(player);
                            }, (long) 1.5, TimeUnit.SECONDS);
                        }
                    }
                } else {
                    if (utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " un-logged in " + player.getName());

                        utils.setLogged(false);
                        utils.setTempLog(false);
                        utils.send(Message);
                        LobbyChecker checker = new LobbyChecker();
                        if (checker.authOk() && checker.authWorking()) {
                            utils.sendTo(checker.getAuth());
                        }
                        plugin.getProxy().getScheduler().schedule(plugin, () -> {
                            dataSender.sendAccountStatus(player);
                        }, (long) 1.5, TimeUnit.SECONDS);
                    }
                }
            }, (long) 1.5, TimeUnit.SECONDS);
        }
    }

    /**
     * Try to log the player
     *
     * @param value   the value
     * @param message the login message
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value, String message) {
        if (player != null) {
            PlayerAuthEvent event = new PlayerAuthEvent(AuthType.API, EventAuthResult.WAITING, player, "");

            if (player.isConnected()) {
                User utils = new User(player);
                if (value) {
                    if (!utils.isLogged()) {
                        plugin.getProxy().getPluginManager().callEvent(event);
                        if (utils.has2FA()) {
                            utils.setLogged(true);
                            utils.setTempLog(true);
                            utils.send(message);
                            utils.send(messages.prefix() + messages.gAuthenticate());
                            dataSender.sendAccountStatus(player);
                            result = AuthResult.SUCCESS_TEMP;
                            event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.prefix() + messages.gAuthenticate());
                        } else {
                            utils.setLogged(true);
                            utils.setTempLog(false);
                            utils.send(message);
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.mainOk() && checker.mainWorking()) {
                                utils.sendTo(checker.getMain());
                            }
                            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                                dataSender.sendAccountStatus(player);
                            }, (long) 1.5, TimeUnit.SECONDS);
                            result = AuthResult.SUCCESS;
                            event.setAuthResult(EventAuthResult.SUCCESS, message);
                        }
                    } else {
                        result = AuthResult.SUCCESS;
                        event.setAuthResult(EventAuthResult.ERROR);
                    }
                } else {
                    if (utils.isLogged()) {
                        utils.setLogged(false);
                        utils.setTempLog(false);
                        utils.send(message);
                        LobbyChecker checker = new LobbyChecker();
                        if (checker.authOk() && checker.authWorking()) {
                            utils.sendTo(checker.getAuth());
                        }
                        plugin.getProxy().getScheduler().schedule(plugin, () -> {
                            dataSender.sendAccountStatus(player);
                        }, (long) 1.5, TimeUnit.SECONDS);
                    }
                    result = AuthResult.SUCCESS;
                }
            } else {
                result = AuthResult.OFFLINE;
                event.setAuthResult(EventAuthResult.ERROR);
            }

            if (value)
                plugin.getProxy().getPluginManager().callEvent(event);

            logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + (value ? " tried to login " : " tried to un-login ") + player.getName() + " with result " + result.name());
        }

        return result;
    }

    /**
     * Try to log the player
     *
     * @param value the value
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value) {
        if (player != null) {
            PlayerAuthEvent event = new PlayerAuthEvent(AuthType.API, EventAuthResult.WAITING, player, "");

            if (player.isConnected()) {
                User utils = new User(player);
                if (value) {
                    if (!utils.isLogged()) {
                        if (utils.has2FA()) {
                            utils.setLogged(true);
                            utils.setTempLog(true);
                            utils.send(messages.prefix() + messages.gAuthenticate());
                            dataSender.sendAccountStatus(player);
                            result = AuthResult.SUCCESS_TEMP;
                            event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.prefix() + messages.gAuthenticate());
                        } else {
                            utils.setLogged(true);
                            utils.setTempLog(false);
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.mainOk() && checker.mainWorking()) {
                                utils.sendTo(checker.getMain());
                            }

                            dataSender.sendAccountStatus(player);
                            result = AuthResult.SUCCESS;
                            event.setAuthResult(EventAuthResult.SUCCESS);
                        }
                    } else {
                        result = AuthResult.SUCCESS;
                        event.setAuthResult(EventAuthResult.ERROR);
                    }
                } else {
                    if (utils.isLogged()) {
                        utils.setLogged(false);
                        utils.setTempLog(false);
                        LobbyChecker checker = new LobbyChecker();
                        if (checker.authOk() && checker.authWorking()) {
                            utils.sendTo(checker.getAuth());
                        }
                        dataSender.sendAccountStatus(player);
                        result = AuthResult.SUCCESS;
                    }
                }
            } else {
                result = AuthResult.OFFLINE;
                event.setAuthResult(EventAuthResult.ERROR);
            }

            if (value)
                plugin.getProxy().getPluginManager().callEvent(event);

            logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + (value ? " tried to login " : " tried to un-login ") + player.getName() + " with result " + result.name());
        }

        return result;
    }

    /**
     * Registers a player with the specified password
     *
     * @param password the password
     */
    public final void register(String password) {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);

                if (!utils.isRegistered()) {
                    logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " registered to " + player.getName());

                    utils.setLogged(true);
                    utils.setTempLog(utils.has2FA());
                    utils.setPassword(password);
                    utils.send(messages.prefix() + messages.registered());
                    utils.send("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");
                    plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
                }
            }, (long) 1.5, TimeUnit.SECONDS);
        }
    }

    /**
     * Tries to register the player
     *
     * @param password the password
     * @return the auth result of the request
     */
    public final AuthResult tryRegister(String password) {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                if (player.isConnected()) {
                    User utils = new User(player);

                    if (!utils.isRegistered()) {
                        utils.setLogged(true);
                        utils.setTempLog(utils.has2FA());
                        utils.setPassword(password);
                        utils.send(messages.prefix() + messages.registered());
                        utils.send("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");
                        plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);

                        PlayerRegisterEvent event = new PlayerRegisterEvent(player);
                        plugin.getProxy().getPluginManager().callEvent(event);
                    }
                    result = AuthResult.SUCCESS;
                } else {
                    result = AuthResult.OFFLINE;
                }

                logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " tried to register " + player.getName() + " with result " + result.name());
            }, (long) 1.5, TimeUnit.SECONDS);
        }

        return result;
    }

    /**
     * Mark the player as registered/not registered
     */
    public final void unRegister() {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                if (utils.isRegistered()) {
                    logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " unregistered to " + player.getName());

                    utils.remove();
                }
            }, (long) 1.5, TimeUnit.SECONDS);
        }
    }

    /**
     * Rest a trie left for the player
     */
    public final void restTrie() {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                utils.restTries();
            }, (long) 1.5, TimeUnit.SECONDS);
        }
    }

    /**
     * Checks if the player is logged or not
     *
     * @return if the player is logged
     */
    public final boolean isLogged() {
        if (player != null) {
            User utils = new User(player);
            if (utils.has2FA()) {
                return utils.isLogged() && !utils.isTempLog();
            }
            return utils.isLogged();
        }

        return false;
    }

    /**
     * Mark the player as logged/un-logged
     *
     * @param Value true = log the player; false = unlog him
     */
    public final void setLogged(boolean Value) {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                if (Value) {
                    if (!utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " logged in " + player.getName());

                        if (utils.has2FA()) {
                            utils.setLogged(true);
                            utils.setTempLog(true);
                            utils.send(messages.prefix() + messages.gAuthenticate());
                            dataSender.sendAccountStatus(player);
                        } else {
                            utils.setLogged(true);
                            utils.setLogged(false);
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.mainOk() && checker.mainWorking()) {
                                utils.sendTo(checker.getMain());
                            }
                            dataSender.sendAccountStatus(player);
                        }
                    }
                } else {
                    if (utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " un-logged in " + player.getName());

                        utils.setLogged(false);
                        utils.setTempLog(false);
                        LobbyChecker checker = new LobbyChecker();
                        if (checker.authOk() && checker.authWorking()) {
                            utils.sendTo(checker.getAuth());
                        }
                        dataSender.sendAccountStatus(player);
                    }
                }
            }, (long) 1.5, TimeUnit.SECONDS);
        }
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        if (player != null) {
            User utils = new User(player);
            return utils.isRegistered();
        }

        return false;
    }

    /**
     * Check if the player has tries left
     *
     * @return if the player has login tries left
     */
    public final boolean hasTries() {
        if (player != null) {
            User utils = new User(player);
            return utils.hasTries();
        }

        return true;
    }

    /**
     * Gets the player country name
     *
     * @return player country
     * <code>This won't return any
     * util information</code>
     */
    @Deprecated
    public final String[] getCountry() {
        return new String[]{"REMOVED", "VERSION 3.0.2"};
    }

    /**
     * Get the player registered
     * accounts
     *
     * @return a list of names that can be associated to that player
     */
    public final Set<OfflineUser> getAccounts() {
        if (player != null) {
            return IPStorager.manager.getAlts(module, player.getUniqueId().toString());
        }

        return new HashSet<>();
    }

    /**
     * Get the player connections
     *
     * @return the amount of connections from the player IP
     */
    public final int getConnections() {
        if (player != null) {
            IpData data = new IpData(module, User.external.getIp(player.getSocketAddress()));
            data.fetch(Platform.BUNGEE);

            return data.getConnections();
        }

        return 0;
    }
}
