package ml.karmaconfigs.lockloginsystem.bungee.utils.user;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginapi.bungee.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserAuthEvent;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.pluginmanager.LockLoginBungeeManager;
import ml.karmaconfigs.lockloginsystem.shared.*;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountID;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountManager;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
public final class User implements LockLoginBungee, BungeeFiles {

    private final AccountManager manager;

    private final static HashSet<UUID> logged = new HashSet<>();
    private final static HashSet<UUID> tempLog = new HashSet<>();

    private final static HashSet<UUID> captchaLogged = new HashSet<>();

    private final static HashMap<UUID, Integer> playerTries = new HashMap<>();
    private final static HashMap<UUID, String> playerCaptcha = new HashMap<>();

    private final ProxiedPlayer player;

    /**
     * Setup the user
     *
     * @param player the player
     */
    public User(ProxiedPlayer player) throws IllegalStateException {
        this.player = player;

        if (PlatformUtils.accountManagerValid()) {
            manager = PlatformUtils.getManager(new Class<?>[]{ProxiedPlayer.class}, player);

            if (manager == null) {
                LockLoginBungeeManager.unload();
                throw new IllegalStateException("Could not initialize player manager ( null ), plugin must stop working now!");
            }
        } else {
            LockLoginBungeeManager.unload();
            throw new IllegalStateException("Current account manager is null, plugin must stop working now!");
        }
    }

    /**
     * Setup the player file
     */
    public final void setupFile() {
        if (!manager.exists())
            manager.create();

        if (manager.getName().replaceAll("\\s", "").isEmpty())
            manager.setName(player.getName());
        if (manager.getUUID().getId().replaceAll("\\s", "").isEmpty())
            manager.saveUUID(AccountID.fromUUID(player.getUniqueId()));
    }

    /**
     * Generate a captcha for the player
     */
    public final void genCaptcha() {
        if (!config.getCaptchaType().equals(CaptchaType.DISABLED))
            if (!captchaLogged.contains(player.getUniqueId())) {
                String captcha = StringUtils.randomString(config.getCaptchaLength(), (config.letters() ? StringUtils.StringGen.NUMBERS_AND_LETTERS : StringUtils.StringGen.ONLY_NUMBERS), StringUtils.StringType.RANDOM_SIZE);

                playerCaptcha.put(player.getUniqueId(), captcha);

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!player.isConnected() || captchaLogged.contains(player.getUniqueId()))
                            cancel();
                        else
                            player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + messages.captcha(captcha))));
                    }
                }, 0, 1000);
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

                        player.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(StringUtils.getLastColor(last) + msg)));
                    } catch (Throwable ex) {
                        player.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(msg)));
                    }
                }
            } catch (Throwable ex) {
                player.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(text)));
            }
        }
    }

    /**
     * Send a list messages to the player
     *
     * @param texts the messages
     */
    public final void send(List<String> texts) {
        if (!texts.isEmpty())
            for (String str : texts)
                send(str);
    }

    /**
     * Send a json message to the player
     *
     * @param JSonMessage the json message
     */
    public final void send(TextComponent JSonMessage) {
        if (!JSonMessage.getText().replace(messages.prefix(), "").replaceAll("\\s", "").isEmpty())
            player.sendMessage(JSonMessage);
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
     * Send a title to the player
     *
     * @param Title    the title
     * @param Subtitle the subtitle
     */
    public final void sendTitle(String Title, String Subtitle) {
        Title title = plugin.getProxy().createTitle();
        title.fadeIn(0);
        title.stay(20 * 5);
        title.fadeOut(0);
        title.title(TextComponent.fromLegacyText(StringUtils.toColor(Title)));
        title.subTitle(TextComponent.fromLegacyText(StringUtils.toColor(Subtitle)));

        title.send(player);
    }

    public final void authPlayer(final String password) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            PasswordUtils utils = new PasswordUtils(password, getPassword());

            BFSystem bf_prevention = new BFSystem(player.getPendingConnection().getVirtualHost().getAddress());

            PlayerAuthEvent event = new PlayerAuthEvent(AuthType.PASSWORD, EventAuthResult.WAITING, player, "");

            boolean valid_password = false;
            if (utils.validate()) {
                valid_password = true;
                if (hasPin()) {
                    event.setAuthResult(EventAuthResult.SUCCESS_TEMP);
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

            plugin.getProxy().getPluginManager().callEvent(event);
            UserAuthEvent authEvent = new UserAuthEvent(event.getAuthType(), event.getAuthResult(), player, event.getAuthMessage(), event);

            switch (event.getAuthResult()) {
                case SUCCESS:
                    if (valid_password) {
                        send(event.getAuthMessage());
                        bf_prevention.success();
                        setLogged(true);
                        checkServer();

                        dataSender.sendAccountStatus(player);

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
                            plugin.getProxy().getScheduler().schedule(plugin, () -> send(motd.onLogin(player.getName(), config.serverName())), motd.getDelay(), TimeUnit.SECONDS);

                        LockLoginListener.callEvent(authEvent);
                    }
                    break;
                case SUCCESS_TEMP:
                    if (valid_password) {
                        bf_prevention.success();
                        setLogged(true);

                        if (Passwords.isLegacySalt(getPassword())) {
                            setPassword(password);
                            send(messages.prefix() + "&cYour account password was using legacy encryption and has been updated");
                        } else {
                            if (utils.needsRehash(config.passwordEncryption())) {
                                setPassword(password);
                            }
                        }

                        setTempLog(true);
                        if (!hasPin()) {
                            send(event.getAuthMessage());
                        } else {
                            dataSender.openPinGUI(player);
                        }

                        LockLoginListener.callEvent(authEvent);
                    } else {
                        logger.scheduleLog(Level.WARNING, "Someone tried to force temp log " + player.getName() + " using event API");
                        send(event.getAuthMessage());
                    }
                    break;
                case FAILED:
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
                    }
                    if (hasTries()) {
                        restTries();
                        send(event.getAuthMessage());
                    } else {
                        bf_prevention.fail();
                        delTries();
                        kick("&eLockLogin\n\n" + messages.logError());
                    }

                    LockLoginListener.callEvent(authEvent);
                    break;
                case ERROR:
                case WAITING:
                    send(event.getAuthMessage());
                    LockLoginListener.callEvent(authEvent);
                    break;
            }
        });
    }

    /**
     * Send the player to the specified
     * server name
     *
     * @param server the server name
     */
    public final void sendTo(String server) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> player.connect(lobbyCheck.generateServerInfo(server)), 0, TimeUnit.SECONDS);
    }

    /**
     * Kick the player
     *
     * @param reason the kick reason
     */
    public final void kick(String reason) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> player.disconnect(TextComponent.fromLegacyText(StringUtils.toColor(reason))), 0, TimeUnit.SECONDS);
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
        manager.setGAuth(token);
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
     * Check the user server
     */
    public final void checkServer() {
        Server current_server = player.getServer();

        if (current_server != null) {
            if (current_server.isConnected() && player.isConnected()) {
                ServerInfo current_info = current_server.getInfo();
                boolean sent = false;
                if (!isLogged() || isTempLog())
                    if (config.enableAuthLobby())
                        if (lobbyCheck.authOk() && lobbyCheck.authWorking())
                            if (!current_info.getName().equals(lobbyCheck.getAuth())) {
                                sendTo(lobbyCheck.getAuth());
                                sent = true;
                            } else
                                sent = current_info.getName().equals(lobbyCheck.getMain());

                if (!sent)
                    if (config.enableMainLobby())
                        if (lobbyCheck.mainOk() && lobbyCheck.mainWorking())
                            if (!current_info.getName().equals(lobbyCheck.getMain()))
                                sendTo(lobbyCheck.getMain());
            }
        }
    }

    /**
     * Tries to get the player IP
     * using socket connection
     *
     * @return the player InetAddress
     */
    @SuppressWarnings("deprecation")
    public final @Nullable InetAddress getIp() {
        try {
            InetSocketAddress address = (InetSocketAddress) player.getSocketAddress();
            return address.getAddress();
        } catch (Throwable ex) {
            logger.scheduleLog(Level.GRAVE, ex);
            logger.scheduleLog(Level.INFO, "Failed to retrieve player IP");
            return player.getAddress().getAddress();
        }
    }

    /**
     * Check if the player captcha is correct
     *
     * @param code the captcha code
     * @return if the player captcha is correct
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
        if (getPassword() != null)
            return !getPassword().replaceAll("\\s", "").isEmpty();

        return false;
    }

    /**
     * Check if the player has a pin set
     *
     * @return if the player has pin
     */
    public final boolean hasPin() {
        if (config.pinEnabled()) {
            return !manager.getPin().isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Check if the player has 2fa enabled
     *
     * @return if the player has 2FA in his account
     */
    public final boolean has2FA() {
        if (config.enable2FA()) {
            return manager.has2FA();
        } else {
            return false;
        }
    }

    /**
     * Get the player password
     *
     * @return the player password
     */
    public final String getPassword() {
        return manager.getPassword();
    }

    /**
     * Set the player password
     *
     * @param password the password
     */
    public final void setPassword(String password) {
        manager.setPassword(password);
    }

    /**
     * Get the player pin
     *
     * @return the player pin
     */
    public final String getPin() {
        return manager.getPin();
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(String pin) {
        manager.setPin(pin);
    }

    /**
     * Remove the user pin
     */
    public final void removePin() {
        manager.setPin(null);
    }

    /**
     * Get the player token
     *
     * @param unHashed hash or not the token
     * @return the player google auth token
     */
    public final String getToken(boolean unHashed) {
        if (unHashed)
            return new PasswordUtils(manager.getGAuth()).unHash();
        else
            return manager.getGAuth();
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return player google auth token if set, if not, a new google auth token
     */
    public final String genToken() {
        if (getToken(true).isEmpty()) {
            GoogleAuthenticator gauth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gauth.createCredentials();

            return key.getKey();
        } else {
            return getToken(true);
        }
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return a new google auth token
     */
    public final String genNewToken() {
        GoogleAuthenticator gauth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gauth.createCredentials();

        return key.getKey();
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
        if (playerTries.containsKey(player.getUniqueId())) {
            return playerTries.get(player.getUniqueId()) != 0;
        } else {
            playerTries.put(player.getUniqueId(), config.loginMaxTries());
            return true;
        }
    }

    /**
     * Check if the player is in temp
     * login status
     *
     * @return if the player is in temp-log status
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
        if (value) {
            tempLog.add(player.getUniqueId());
        } else {
            tempLog.remove(player.getUniqueId());
        }
    }

    /**
     * Check if the code is ok
     *
     * @param code the code
     * @return if the specified code is valid
     */
    public final boolean validateCode(int code) {
        GoogleAuthenticator gauth = new GoogleAuthenticator();

        return gauth.authorize(getToken(true), code) || gauth.authorize(getToken(false), code);
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

    /**
     * External user utilities
     */
    public interface external {

        /**
         * Get the user ip
         *
         * @param connection the user connection
         * @return the user ip
         */
        @Nullable
        static InetAddress getIp(final SocketAddress connection) {
            try {
                InetSocketAddress address = (InetSocketAddress) connection;
                return address.getAddress();
            } catch (Throwable ex) {
                return null;
            }
        }
    }
}
