package ml.karmaconfigs.lockloginsystem.bungee.utils.user;

import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
public final class StartCheck implements LockLoginBungee, BungeeFiles {

    private final BungeeSender dataSender = new BungeeSender();

    private final Set<UUID> inCheck = new HashSet<>();

    private ScheduledTask task;
    private ScheduledTask msTask;
    private int back;

    /**
     * Start the checker
     *
     * @param p    the player
     * @param type the check type
     */
    public StartCheck(ProxiedPlayer p, CheckType type) {
        if (p != null && !inCheck.contains(p.getUniqueId())) {
            inCheck.add(p.getUniqueId());
            startSendingMessage(p, type);
            switch (type) {
                case REGISTER:
                    back = config.registerTimeOut();
                    task = plugin.getProxy().getScheduler().schedule(plugin, () -> {
                        if (p.isConnected()) {
                            dataSender.sendUUID(p.getUniqueId(), p.getServer());
                            User user = new User(p);
                            if (!user.isRegistered()) {
                                if (back != 0) {
                                    dataSender.sendUUID(p.getUniqueId(), p.getServer());
                                    dataSender.sendAccountStatus(p);

                                    if (!messages.registerTitle(back).isEmpty() || !messages.registerSubtitle(back).isEmpty())
                                        user.sendTitle(messages.registerTitle(back), messages.registerSubtitle(back));
                                } else {
                                    dataSender.blindEffect(p, false, config.nauseaRegister());
                                    user.kick("&eLockLogin\n\n" + messages.registerTimeOut());
                                    task.cancel();
                                    inCheck.remove(p.getUniqueId());
                                }
                            } else {
                                task.cancel();
                                dataSender.blindEffect(p, false, config.nauseaRegister());
                                user.sendTitle("&a", "&b");
                                inCheck.remove(p.getUniqueId());
                            }
                            back--;
                        } else {
                            task.cancel();
                        }
                    }, 0, 1, TimeUnit.SECONDS);
                    break;
                case LOGIN:
                    back = config.loginTimeOut();
                    task = plugin.getProxy().getScheduler().schedule(plugin, () -> {
                        if (p.isConnected()) {
                            dataSender.sendUUID(p.getUniqueId(), p.getServer());
                            User user = new User(p);
                            if (!user.isLogged()) {
                                if (back != 0) {
                                    dataSender.sendUUID(p.getUniqueId(), p.getServer());
                                    dataSender.sendAccountStatus(p);

                                    if (!messages.loginTitle(back).isEmpty() || !messages.loginSubtitle(back).isEmpty())
                                        user.sendTitle(messages.loginTitle(back), messages.loginSubtitle(back));
                                } else {
                                    dataSender.blindEffect(p, false, config.nauseaLogin());
                                    user.kick("&eLockLogin\n\n" + messages.loginTimeOut());
                                    task.cancel();
                                    inCheck.remove(p.getUniqueId());
                                }
                                back--;
                            } else {
                                task.cancel();
                                dataSender.blindEffect(p, false, config.nauseaLogin());
                                user.sendTitle("&a", "&b");
                                inCheck.remove(p.getUniqueId());
                            }
                        } else {
                            task.cancel();
                            dataSender.blindEffect(p, false, config.nauseaLogin());
                        }
                    }, 0, 1, TimeUnit.SECONDS);
                    break;
            }
        }
    }

    /**
     * Send the auth message
     *
     * @param player the player
     * @param type   the auth message type
     */
    private void sendAuthMessage(ProxiedPlayer player, CheckType type) {
        User user = new User(player);

        switch (type) {
            case LOGIN:
                user.send(messages.prefix() + messages.login(user.getCaptcha()));
                break;
            case REGISTER:
                user.send(messages.prefix() + messages.register(user.getCaptcha()));
                break;
        }
    }

    /**
     * Start sending the login message
     *
     * @param player the player
     * @param type   the message type
     */
    private void startSendingMessage(ProxiedPlayer player, CheckType type) {
        int interval = (new User(player).isRegistered() ? config.loginInterval() : config.registerInterval());

        msTask = plugin.getProxy().getScheduler().schedule(plugin, () -> {
            User user = new User(player);
            if (!user.isLogged()) {
                sendAuthMessage(player, type);
            } else {
                msTask.cancel();
            }
        }, 0, interval, TimeUnit.SECONDS);
    }
}
