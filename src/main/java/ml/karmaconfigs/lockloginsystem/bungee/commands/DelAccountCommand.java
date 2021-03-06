package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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
public final class DelAccountCommand extends Command implements LockLoginBungee, BungeeFiles {

    /**
     * Initialize account removal command
     */
    public DelAccountCommand() {
        super("delaccount", "", "delacc", "delcc");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        final String forceDel = "locklogin.forcedel";
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (args.length == 0) {
                if (user.isLogged()) {
                    user.send(messages.prefix() + messages.delAccount());
                } else {
                    if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                        if (user.isRegistered()) {
                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                        } else {
                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                        }
                    } else {
                        user.send(messages.prefix() + messages.typeCaptcha());
                    }
                }
            } else {
                if (args.length == 1) {
                    if (player.hasPermission(forceDel)) {
                        String tar = args[0];

                        if (plugin.getProxy().getPlayer(tar) != null) {
                            ProxiedPlayer target = plugin.getProxy().getPlayer(tar);

                            if (!target.equals(player)) {
                                User targetUser = new User(target);

                                if (config.enableAuthLobby()) {
                                    if (lobbyCheck.authWorking()) {
                                        targetUser.sendTo(lobbyCheck.getAuth());
                                    }
                                }

                                targetUser.remove();
                                targetUser.setLogged(false);
                                targetUser.send(messages.prefix() + messages.forcedDelAccount(player));
                                user.send(messages.prefix() + messages.forcedDelAccAdmin(target));
                                new StartCheck(target, CheckType.REGISTER);
                                dataSender.sendAccountStatus(target);
                            } else {
                                user.send(messages.prefix() + messages.delAccount());
                            }
                        } else {
                            OfflineUser targetUser = new OfflineUser("", tar, true);

                            if (targetUser.exists()) {
                                targetUser.delete();
                                user.send(messages.prefix() + messages.forcedDelAccAdmin(tar));
                            } else {
                                user.send(messages.prefix() + messages.unknownPlayer(tar));
                            }
                        }
                    } else {
                        user.send(messages.prefix() + messages.permission(forceDel));
                    }
                } else {
                    if (args.length == 2) {
                        String password = args[0];
                        String confirmation = args[1];

                        if (password.equals(confirmation)) {
                            PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                            if (utils.validate()) {
                                user.remove();
                                user.setLogged(false);
                                user.send(messages.prefix() + messages.accountDeleted());
                                new StartCheck(player, CheckType.REGISTER);
                                dataSender.sendAccountStatus(player);
                            } else {
                                user.send(messages.prefix() + messages.delAccError());
                            }
                        } else {
                            user.send(messages.prefix() + messages.delAccMatch());
                        }
                    } else {
                        user.send(messages.prefix() + messages.delAccount());
                    }
                }
            }
        } else {
            if (args.length == 1) {
                String tar = args[0];

                if (plugin.getProxy().getPlayer(tar) != null) {
                    ProxiedPlayer target = plugin.getProxy().getPlayer(tar);
                    User targetUser = new User(target);

                    if (config.enableAuthLobby()) {
                        if (lobbyCheck.authWorking()) {
                            targetUser.sendTo(lobbyCheck.getAuth());
                        }
                    }

                    targetUser.remove();
                    targetUser.setLogged(false);
                    targetUser.send(messages.prefix() + messages.forcedDelAccount("SERVER"));
                    Console.send(messages.prefix() + messages.forcedDelAccAdmin(target));
                    new StartCheck(target, CheckType.REGISTER);
                    dataSender.sendAccountStatus(target);
                } else {
                    OfflineUser targetUser = new OfflineUser("", tar, true);

                    if (targetUser.exists()) {
                        targetUser.delete();
                        Console.send(messages.prefix() + messages.forcedDelAccAdmin(tar));
                    } else {
                        Console.send(messages.prefix() + messages.unknownPlayer(tar));
                    }
                }
            } else {
                Console.send(plugin, "Correct usage: delacc <player>", Level.WARNING);
            }
        }
    }
}
