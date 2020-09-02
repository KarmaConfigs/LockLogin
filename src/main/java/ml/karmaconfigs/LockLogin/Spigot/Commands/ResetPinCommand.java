package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ResetPinCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    /**
     * The pin reset command
     *
     * @param sender who executes the command
     * @param cmd    the command
     * @param arg    the command arg
     * @param args   the command args
     * @return a boolean
     */
    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (config.EnablePins()) {
                if (user.hasPin()) {
                    if (args.length == 1) {
                        try {
                            if (args[0].length() == 4) {
                                Integer.parseInt(args[0]);

                                String pin = args[0];
                                if (new PasswordUtils(pin, user.getPin()).PasswordIsOk()) {
                                    user.removePin();

                                    user.Message(messages.Prefix() + messages.PinSet("none"));
                                } else {
                                    user.Message(messages.Prefix() + messages.IncorrectPin());
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.PinLength());
                            }
                        } catch (NumberFormatException e) {
                            user.Message(messages.Prefix() + messages.ResetPin());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.ResetPin());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.NoPin());
                }
            } else {
                user.Message(messages.Prefix() + messages.PinDisabled());
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
        return false;
    }
}
