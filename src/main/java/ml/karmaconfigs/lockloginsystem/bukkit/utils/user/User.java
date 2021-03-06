package ml.karmaconfigs.lockloginsystem.bukkit.utils.user;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import ml.karmaconfigs.api.bukkit.reflections.BarMessage;
import ml.karmaconfigs.api.bukkit.reflections.TitleMessage;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginapi.bukkit.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserAuthEvent;
import ml.karmaconfigs.lockloginsystem.shared.*;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountID;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountManager;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.inventory.PinInventory;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
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
public final class User implements LockLoginSpigot, SpigotFiles {

    private final AccountManager manager;

    private final static HashSet<UUID> logged = new HashSet<>();
    private final static HashSet<UUID> tempLog = new HashSet<>();

    private final static HashSet<UUID> captchaLogged = new HashSet<>();

    private final static HashMap<UUID, Integer> playerTries = new HashMap<>();
    private final static HashMap<UUID, String> playerCaptcha = new HashMap<>();

    private final static HashMap<UUID, Collection<PotionEffect>> playerEffects = new HashMap<>();

    private final Player player;

    /**
     * Setup the user
     *
     * @param player the player
     */
    public User(Player player) throws IllegalStateException {
        this.player = player;

        if (PlatformUtils.accountManagerValid()) {
            manager = PlatformUtils.getManager(new Class<?>[]{OfflinePlayer.class}, plugin.getServer().getOfflinePlayer(player.getUniqueId()));

            if (manager == null) {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                throw new IllegalStateException("Could not initialize player manager ( null ), plugin must stop working now!");
            }
        } else {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new IllegalStateException("Current account manager is null, plugin must stop working now!");
        }
    }

    /**
     * Setup the player file
     */
    public final void setupFile() {
        if (!config.isBungeeCord()) {
            if (!manager.exists())
                manager.create();

            if (manager.getName().replaceAll("\\s", "").isEmpty())
                manager.setName(player.getName());
            if (manager.getUUID().getId().replaceAll("\\s", "").isEmpty())
                manager.saveUUID(AccountID.fromUUID(player.getUniqueId()));
        }
    }

    /**
     * Generate a captcha for the player
     */
    public final void genCaptcha() {
        if (!config.getCaptchaType().equals(CaptchaType.DISABLED))
            if (!captchaLogged.contains(player.getUniqueId())) {
                String captcha = StringUtils.randomString(config.getCaptchaLength(), (config.letters() ? StringUtils.StringGen.NUMBERS_AND_LETTERS : StringUtils.StringGen.ONLY_NUMBERS), StringUtils.StringType.RANDOM_SIZE);
                playerCaptcha.put(player.getUniqueId(), captcha);

                BarMessage bar = new BarMessage(player, messages.prefix() + messages.captcha(captcha));
                bar.send(true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            if (!player.isOnline() || captchaLogged.contains(player.getUniqueId())) {
                                bar.setMessage("");
                                bar.stop();
                                cancel();
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }.runTaskTimerAsynchronously(plugin, 0, 20);
            }
    }

    /**
     * Send a message to the player
     *
     * @param text the message
     */
    public final void send(String text) {
        if (!text.replace(messages.prefix(), "").replaceAll("\\s", "").isEmpty()) {
            try {
                String[] data = text.split("\\{newline}");
                for (int i = 0; i < data.length; i++) {
                    String msg = data[i];
                    try {
                        String last = data[0];
                        if (i > 0)
                            last = data[i - 1];

                        player.sendMessage(StringUtils.toColor(StringUtils.getLastColor(last) + msg));
                    } catch (Throwable ex) {
                        player.sendMessage(StringUtils.toColor(msg));
                    }
                }
            } catch (Throwable ex) {
                player.sendMessage(StringUtils.toColor(text));
            }
        }
    }

    /**
     * Send a list of messages to the player
     *
     * @param messages the messages
     */
    public final void send(List<String> messages) {
        if (!messages.isEmpty())
            for (String str : messages)
                player.sendMessage(StringUtils.toColor(str));
    }

    /**
     * Send a json message to the player
     *
     * @param JSonMessage the json message
     */
    public final void send(TextComponent JSonMessage) {
        if (!JSonMessage.getText().replace(messages.prefix(), "").replaceAll("\\s", "").isEmpty())
            player.spigot().sendMessage(JSonMessage);
    }

    /**
     * Send a list of messages to the player
     *
     * @param messages the messages
     */
    public final void send(HashSet<String> messages) {
        if (!messages.isEmpty())
            for (String str : messages)
                send(str);
    }

    /**
     * Send a title to the players
     *
     * @param title    the title
     * @param subtitle the subtitle
     * @param fadeIn   time to show title to screen
     * @param fadeOut  time to hide title from screen
     * @param show     time to show title in screen
     */
    public final void sendTitle(String title, String subtitle, int fadeIn, int show, int fadeOut) {
        if (!title.isEmpty() || !subtitle.isEmpty()) {
            TitleMessage title_message = new TitleMessage(player, title, subtitle);
            try {
                title_message.send(fadeIn, show, fadeOut);
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while sending title to " + player.getName());
            }
        }
    }

    /**
     * Teleport the player to the location
     *
     * @param location the location
     */
    public final void teleport(Location location) {
        plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(location));
    }

    /**
     * Kick the player
     *
     * @param reason the kick reason
     */
    public final void kick(String reason) {
        plugin.getServer().getScheduler().runTask(plugin, () -> player.kickPlayer(StringUtils.toColor(reason)));
    }

    /**
     * Add the potion effect to the player
     *
     * @param type         the potion effect type
     * @param duration     the potion effect duration
     * @param amp          the potion effect amplification
     * @param isSource     if the potion has some source
     * @param hasParticles if the potion should spawn particles
     */
    public final void sendEffect(PotionEffectType type, int duration, int amp, boolean isSource, boolean hasParticles) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            PotionEffect effect = new PotionEffect(type, 20 * duration, amp, isSource, hasParticles);

            player.addPotionEffect(effect);
        });
    }

    /**
     * Add the potion effects to the player
     *
     * @param effects the potion effects
     */
    public final void sendEffects(Collection<PotionEffect> effects) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }
        });
    }

    /**
     * Remove the player potion effect type
     *
     * @param type the potion effect type
     */
    public final void removeEffect(PotionEffectType type) {
        try {
            plugin.getServer().getScheduler().runTask(plugin, () -> player.removePotionEffect(type));
        } catch (Throwable ex) {
            player.removePotionEffect(type);
        }
    }

    /**
     * Auth the player and perform his
     * login
     *
     * @param password the used password
     */
    public final void authPlayer(String password) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerAuthEvent event = new PlayerAuthEvent(AuthType.PASSWORD, EventAuthResult.WAITING, player, "");

            PasswordUtils utils = new PasswordUtils(password, getPassword());

            if (utils.validate()) {
                if (hasPin()) {
                    event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.prefix() + messages.logged(player));
                } else {
                    if (has2FA()) {
                        event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.gAuthInstructions());
                    } else {
                        event.setAuthResult(EventAuthResult.SUCCESS, messages.prefix() + messages.logged(player));
                    }
                }
            } else {
                event.setAuthResult(EventAuthResult.FAILED, messages.prefix() + messages.logError());
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().getPluginManager().callEvent(event);
                UserAuthEvent authEvent = new UserAuthEvent(event.getAuthType(), event.getAuthResult(), player, event.getAuthMessage(), event);

                switch (event.getAuthResult()) {
                    case SUCCESS:
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                            if (utils.validate()) {
                                InetSocketAddress ip = player.getAddress();

                                if (ip != null) {
                                    BFSystem bf_prevention = new BFSystem(ip.getAddress());
                                    bf_prevention.success();
                                }

                                sendTitle("", "", 1, 2, 1);
                                setLogged(true);

                                send(event.getAuthMessage());

                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    if (config.takeBack()) {
                                        LastLocation lastLoc = new LastLocation(player);
                                        if (lastLoc.hasLastLocation())
                                            teleport(lastLoc.getLastLocation());
                                    }
                                });
                                removeBlindEffect();

                                if (Passwords.isLegacySalt(getPassword())) {
                                    setPassword(password);
                                    send(messages.prefix() + "&cYour account password was using legacy encryption and has been updated");
                                } else {
                                    if (utils.needsRehash(config.passwordEncryption())) {
                                        setPassword(password);
                                    }
                                }

                                File motd_file = new File(plugin.getDataFolder(), "motd.locklogin");
                                Motd motd = new Motd(motd_file);

                                if (motd.isEnabled())
                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> send(motd.onLogin(player.getName(), config.serverName())), 20L * motd.getDelay());

                                LockLoginListener.callEvent(authEvent);
                            } else {
                                logger.scheduleLog(Level.WARNING, "Someone tried to force log " + player.getName() + " using event API");
                            }
                        });
                        break;
                    case SUCCESS_TEMP:
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                            if (utils.validate()) {
                                setLogged(true);
                                if (Passwords.isLegacySalt(getPassword())) {
                                    setPassword(password);
                                    send(messages.prefix() + "&cYour account password was using legacy encryption and has been updated");
                                } else {
                                    if (utils.needsRehash(config.passwordEncryption())) {
                                        setPassword(password);
                                    }
                                }

                                if (hasPin()) {
                                    PinInventory inventory = new PinInventory(player);
                                    inventory.open();

                                    setTempLog(true);
                                } else {
                                    if (has2FA()) {
                                        send(event.getAuthMessage());

                                        setTempLog(true);
                                    } else {
                                        logger.scheduleLog(Level.WARNING, "Someone tried to force temp log " + player.getName() + " using event API");

                                        send(event.getAuthMessage());
                                        InetSocketAddress ip = player.getAddress();

                                        if (ip != null) {
                                            BFSystem bf_prevention = new BFSystem(ip.getAddress());
                                            if (bf_prevention.getTries() >= config.bfMaxTries() && config.bfMaxTries() > 0) {
                                                bf_prevention.block();
                                                bf_prevention.updateTime(config.bfBlockTime());

                                                Timer unban = new Timer();
                                                unban.schedule(new TimerTask() {
                                                    final BFSystem saved_system = bf_prevention;
                                                    int back = config.bfBlockTime();

                                                    @Override
                                                    public void run() {
                                                        if (back == 0) {
                                                            saved_system.unlock();
                                                            cancel();
                                                        }
                                                        saved_system.updateTime(back);
                                                        back--;
                                                    }
                                                }, 0, TimeUnit.SECONDS.toMillis(1));

                                                kick("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft()));
                                            } else {
                                                if (!hasTries()) {
                                                    delTries();
                                                    bf_prevention.fail();
                                                    plugin.getServer().getScheduler().runTask(plugin, () -> kick("&eLockLogin\n\n" + messages.logError()));
                                                    return;
                                                }
                                                restTries();
                                                send(event.getAuthMessage());
                                            }
                                        }
                                    }
                                }

                                LockLoginListener.callEvent(authEvent);
                            } else {
                                logger.scheduleLog(Level.WARNING, "Someone tried to force temp log " + player.getName() + " using event API");
                                send(event.getAuthMessage());
                            }
                        });
                        break;
                    case FAILED:
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                            InetSocketAddress ip = player.getAddress();

                            if (ip != null) {
                                BFSystem bf_prevention = new BFSystem(ip.getAddress());
                                if (bf_prevention.getTries() >= config.bfMaxTries() && config.bfMaxTries() > 0) {
                                    bf_prevention.block();
                                    bf_prevention.updateTime(config.bfBlockTime());

                                    Timer unban = new Timer();
                                    unban.schedule(new TimerTask() {
                                        final BFSystem saved_system = bf_prevention;
                                        int back = config.bfBlockTime();

                                        @Override
                                        public void run() {
                                            if (back == 0) {
                                                saved_system.unlock();
                                                cancel();
                                            }
                                            saved_system.updateTime(back);
                                            back--;
                                        }
                                    }, 0, TimeUnit.SECONDS.toMillis(1));

                                    kick("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft()));
                                } else {
                                    if (!hasTries()) {
                                        delTries();
                                        bf_prevention.fail();
                                        plugin.getServer().getScheduler().runTask(plugin, () -> kick("&eLockLogin\n\n" + messages.logError()));
                                        return;
                                    }
                                    restTries();
                                    send(event.getAuthMessage());
                                }
                            }
                        });

                        LockLoginListener.callEvent(authEvent);
                        break;
                    case ERROR:
                    case WAITING:
                        send(event.getAuthMessage());
                        LockLoginListener.callEvent(authEvent);
                        break;
                }
            });
        });
    }

    /**
     * Rest a trie for the player
     */
    public final void restTries() {
        playerTries.put(player.getUniqueId(), getTriesLeft() - 1);
    }

    /**
     * Remove the player from tries
     * left
     */
    public final void delTries() {
        playerTries.remove(player.getUniqueId());
    }

    /**
     * Set the player 2fa status
     *
     * @param value true/false
     */
    public final void set2FA(boolean value) {
        manager.set2FA(value);
    }

    /**
     * Set the player 2fa token
     *
     * @param token the token
     */
    public final void setToken(String token) {
        if (token != null)
            manager.setGAuth(new PasswordUtils(token).hash());
        else
            manager.setGAuth("");
    }

    /**
     * Remove the player file
     * or if using mysql, player info
     */
    public final void remove() {
        if (manager.exists())
            manager.remove();
    }

    /**
     * Save the player current effects
     */
    public final void saveCurrentEffects() {
        playerEffects.putIfAbsent(player.getUniqueId(), player.getActivePotionEffects());
    }

    /**
     * Apply the blind effect to the player
     *
     * @param nausea apply nausea effect?
     */
    public final void applyBlindEffect(boolean nausea) {
        int time = config.registerTimeOut() * 20;
        if (isRegistered())
            time = config.loginTimeOut() * 20;

        removeEffect(PotionEffectType.BLINDNESS);
        removeEffect(PotionEffectType.NIGHT_VISION);
        if (nausea) {
            removeEffect(PotionEffectType.CONFUSION);
            sendEffect(PotionEffectType.CONFUSION, time, 100, true, false);
        }
        sendEffect(PotionEffectType.BLINDNESS, time, 100, true, false);
        sendEffect(PotionEffectType.NIGHT_VISION, time, 100, true, false);
    }

    /**
     * Remove the user blind effects
     */
    public final void removeBlindEffect() {
        removeEffect(PotionEffectType.BLINDNESS);
        removeEffect(PotionEffectType.NIGHT_VISION);
        removeEffect(PotionEffectType.CONFUSION);

        if (playerEffects.containsKey(player.getUniqueId()))
            sendEffects(playerEffects.remove(player.getUniqueId()));
    }

    /**
     * Check the player status
     */
    public final void checkStatus() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CheckType checkType;
            if (!isRegistered()) {
                checkType = CheckType.REGISTER;
            } else {
                checkType = CheckType.LOGIN;
            }

            new StartCheck(player, checkType);
            switch (checkType) {
                case REGISTER:
                    send(messages.prefix() + messages.register(getCaptcha()));
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (config.blindRegister()) {
                            saveCurrentEffects();
                            applyBlindEffect(config.nauseaRegister());
                        }
                    }, 5);
                    break;
                case LOGIN:
                    send(messages.prefix() + messages.login(getCaptcha()));
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (config.blindLogin()) {
                            saveCurrentEffects();
                            applyBlindEffect(config.nauseaLogin());
                        }
                    }, 5);
                    break;
            }
        });
    }

    /**
     * Remove the user pin
     */
    public final void removePin() {
        manager.setPin(null);
    }

    /**
     * Get the player UUID
     * <code>No longer used</code>
     *
     * @return the player UUID
     */
    @Deprecated
    public final String getUUID() {
        if (!config.isBungeeCord())
            return manager.getUUID().getId();

        return plugin.getServer().getOfflinePlayer(player.getUniqueId()).getUniqueId().toString();
    }

    /**
     * Check the captcha
     *
     * @param code the captcha code the player
     *             has typed
     * @return if the code is valid
     */
    public final boolean checkCaptcha(final String code) {
        if (playerCaptcha.containsKey(player.getUniqueId()))
            if (code.equals(playerCaptcha.get(player.getUniqueId()))) {
                playerCaptcha.remove(player.getUniqueId());
                captchaLogged.add(player.getUniqueId());

                return true;
            }

        return false;
    }

    /**
     * Check if the player has a pending
     * captcha
     *
     * @return if the player has pending captcha
     */
    public final boolean hasCaptcha() {
        if (config.getCaptchaType().equals(CaptchaType.DISABLED))
            return false;
        else
            return !captchaLogged.contains(player.getUniqueId());
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        if (!config.isBungeeCord()) {
            if (getPassword() != null)
                return !getPassword().replaceAll("\\s", "").isEmpty();
        }

        return false;
    }

    /**
     * Check if the player has a pin set
     *
     * @return if the player has a pin
     */
    public final boolean hasPin() {
        if (!config.isBungeeCord()) {
            if (config.enablePin())
                return !manager.getPin().replaceAll("\\s", "").isEmpty();
        }
        return false;
    }

    /**
     * Check if the player has 2fa enabled
     *
     * @return if the player has 2fa in his account
     */
    public final boolean has2FA() {
        if (!config.isBungeeCord()) {
            if (config.enable2FA()) {
                return manager.has2FA();
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if the player is logged
     *
     * @return if the player is logged
     */
    public final boolean isLogged() {
        return logged.contains(player.getUniqueId());
    }

    /**
     * Set the player session status
     *
     * @param value true/false
     */
    public final void setLogged(boolean value) {
        if (value)
            logged.add(player.getUniqueId());
        else
            logged.remove(player.getUniqueId());
    }

    /**
     * Check if the player has tries left
     *
     * @return if the player has login tries left
     */
    public final boolean hasTries() {
        if (!config.isBungeeCord()) {
            if (playerTries.containsKey(player.getUniqueId())) {
                return playerTries.get(player.getUniqueId()) != 0;
            } else {
                playerTries.put(player.getUniqueId(), config.loginMaxTries());
                return true;
            }
        }
        return true;
    }

    /**
     * Check if the player is in temp
     * login status
     *
     * @return if the player is in temp log status
     * For example, if he has 2Fa or pin
     */
    public final boolean isTempLog() {
        return tempLog.contains(player.getUniqueId());
    }

    /**
     * Set if the player is in temp-login
     * status
     *
     * @param value true/false
     */
    public final void setTempLog(boolean value) {
        if (value)
            tempLog.add(player.getUniqueId());
        else
            tempLog.remove(player.getUniqueId());
    }

    /**
     * Check if the code is ok
     *
     * @param code the code
     * @return if the specified code is valid
     */
    public final boolean validateCode(int code) {
        GoogleAuthenticator gauth = new GoogleAuthenticator();

        return gauth.authorize(getToken(false), code) || gauth.authorize(getToken(true), code);
    }

    /**
     * Get the player password
     *
     * @return the player password
     */
    public final String getPassword() {
        if (!config.isBungeeCord()) {
            /*
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.getPassword();
            } else {
                Utils sql = new Utils(player);

                return sql.getPassword();
            }*/
            return manager.getPassword();
        }
        return "";
    }

    /**
     * Set the player password
     *
     * @param password the password
     */
    public final void setPassword(String password) {
        if (!config.isBungeeCord()) {
            manager.setPassword(password);
        }
    }

    /**
     * Get the player pin
     *
     * @return the player pin
     */
    public final String getPin() {
        if (!config.isBungeeCord())
            return manager.getPin();

        return "";
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(String pin) {
        if (!config.isBungeeCord())
            manager.setPin(pin);
    }

    /**
     * Get the player token
     *
     * @param unHashed get the token with hash?
     * @return the player google auth token
     */
    public final String getToken(boolean unHashed) {
        if (!config.isBungeeCord())
            if (unHashed)
                return manager.getGAuth();
            else
                return new PasswordUtils(manager.getGAuth()).unHash();

        return "";
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return the player google auth token if set, if not, a new google auth token
     */
    public final String genToken() {
        if (!config.isBungeeCord()) {
            if (getToken(true).isEmpty()) {
                GoogleAuthenticator gauth = new GoogleAuthenticator();
                GoogleAuthenticatorKey key = gauth.createCredentials();

                return key.getKey();
            } else {
                return getToken(true);
            }
        }
        return "";
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return a new google auth token
     */
    public final String genNewToken() {
        if (!config.isBungeeCord()) {
            GoogleAuthenticator gauth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gauth.createCredentials();

            return key.getKey();
        }
        return "";
    }

    /**
     * Get the player captcha
     *
     * @return the player captcha
     */
    public final String getCaptcha() {
        return playerCaptcha.getOrDefault(player.getUniqueId(), "");
    }

    /**
     * Get the player tries left
     *
     * @return the amount of login tries left
     * of the player
     */
    public final int getTriesLeft() {
        return playerTries.getOrDefault(player.getUniqueId(), config.loginMaxTries());
    }
}