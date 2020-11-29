package ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles;

import ml.karmaconfigs.api.bungee.karmayaml.FileCopy;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */

public final class AllowedCommands implements LockLoginBungee {

    private final static List<String> allowed = new ArrayList<>();

    /**
     * Setup the allowed commands file
     * data
     */
    public AllowedCommands() {
        File allowed_file = new File(plugin.getDataFolder(), "allowed.yml");
        FileCopy creator = new FileCopy(plugin, "auto-generated/allowed.yml");

        creator.copy(allowed_file);

        FileManager manager = new FileManager("allowed.yml");
        manager.setInternal("auto-generated/allowed.yml");

        if (!manager.isSet("AllowedCommands")) {
            manager.set("AllowedCommands", allowed);
        }

        for (String str : manager.getList("AllowedCommands")) {
            if (!allowed.contains(str)) {
                allowed.add("/" + str.replace("/", ""));
            }
        }
    }

    /**
     * Add all the listed cmds to
     * the list of allowed cmds
     *
     * @param cmds the cmds
     */
    public final void addAll(List<String> cmds) {
        for (String str : cmds) {
            if (!allowed.contains(str)) {
                allowed.add("/" + str.replace("/", ""));
            }
        }
    }

    /**
     * <code>Will be implemented as
     * a command to add allowed
     * cmds</code>
     * Add the cmd to the list
     * of allowed cmds
     *
     * @param cmd the cmds
     * @return if the command has been added
     */
    public final boolean add(String cmd) {
        if (!allowed.contains(cmd)) {
            allowed.add("/" + cmd.replace("/", ""));
            return true;
        }
        return false;
    }

    /**
     * <code>Will be implemented as
     * a command to remove allowed
     * cmds</code>
     * Remove the cmd from the
     * list of allowed cmds
     *
     * @param cmd the cmd
     * @return is the command has been removed
     */
    public final boolean remove(String cmd) {
        if (allowed.contains(cmd)) {
            allowed.remove("/" + cmd.replace("/", ""));
            return true;
        }
        return false;
    }

    /**
     * Check if the cmd is allowed
     *
     * @param cmd the cmd
     * @return if the command is allowed
     */
    public final boolean isAllowed(String cmd) {
        return allowed.contains("/" + cmd.replace("/", ""));
    }
}
