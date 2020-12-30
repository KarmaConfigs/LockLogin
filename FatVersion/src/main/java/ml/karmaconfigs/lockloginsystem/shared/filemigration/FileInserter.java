package ml.karmaconfigs.lockloginsystem.shared.filemigration;

import ml.karmaconfigs.lockloginsystem.shared.InsertInfo;
import ml.karmaconfigs.lockloginsystem.shared.InsertReader;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class FileInserter {

    private final InsertReader reader;

    /**
     * Initialize the file inserter
     *
     * @param info the info
     */
    public FileInserter(InsertInfo info) {
        this.reader = new InsertReader(info.getData());
    }

    /**
     * Insert the info into files
     */
    public final void insert() {
        String name = reader.get("Name").toString();
        String uuid = reader.get("UUID").toString();
        String pass = reader.get("Password").toString();
        String token = reader.get("Token").toString();
        String pin = reader.get("Pin").toString();
        boolean faon = Boolean.parseBoolean(reader.get("gAuth").toString());
        boolean fly = Boolean.parseBoolean(reader.get("Fly").toString());

        ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager spigotManager = null;
        ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager bungeeManager = null;
        try {
            spigotManager = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager(uuid.replace("-", "") + ".yml", "playerdata");
        } catch (NoClassDefFoundError e) {
            bungeeManager = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager(uuid.replace("-", "") + ".yml", "playerdata");
        }
        if (spigotManager != null) {
            spigotManager.set("Player", name);
            spigotManager.set("UUID", uuid);
            spigotManager.set("Password", pass);
            spigotManager.set("Pin", pin);
            spigotManager.set("GAuth", token);
            spigotManager.set("2FA", faon);
            spigotManager.set("Fly", fly);
        } else {
            bungeeManager.set("Player", name);
            bungeeManager.set("UUID", uuid);
            bungeeManager.set("Password", pass);
            bungeeManager.set("Pin", pin);
            bungeeManager.set("GAuth", token);
            bungeeManager.set("2FA", faon);
            bungeeManager.set("Fly", fly);
        }
    }
}
