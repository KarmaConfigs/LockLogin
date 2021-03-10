package ml.karmaconfigs.lockloginsystem.spigot.api;

import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class OfflineAPI implements SpigotFiles {

    private final Module module;
    private final String player;

    /**
     * Initialize LockLogin bungee's API
     *
     * @param loader the module that is calling
     *               the API method
     * @param player the player
     */
    public OfflineAPI(final Module loader, String player) {
        module = loader;
        if (ModuleLoader.manager.isLoaded(loader)) {
            this.player = player;
        } else {
            this.player = null;
        }
    }

    /**
     * Get the offline player UUID
     *
     * @return the player UUID
     */
    public final String getUUID() {
        if (config.isMySQL()) {
            Utils utils = new Utils();
            return utils.fetchUUID(player);
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.getUUID().toString();
        }
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2fa
     */
    public final boolean has2FA() {
        if (config.isMySQL()) {
            Utils utils = new Utils();
            String player_uuid = utils.fetchUUID(player);
            utils = new Utils(player_uuid, player);

            return utils.has2fa();
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.has2FA();
        }
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        if (config.isMySQL()) {
            Utils utils = new Utils();
            String player_uuid = utils.fetchUUID(player);
            utils = new Utils(player_uuid, player);

            return utils.userExists() && utils.getPassword() != null && !utils.getPassword().isEmpty();
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.has2FA();
        }
    }

    /**
     * Get the user token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        if (config.isMySQL()) {
            Utils utils = new Utils();
            String player_uuid = utils.fetchUUID(player);
            utils = new Utils(player_uuid, player);

            return utils.getToken();
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.getToken();
        }
    }

    /**
     * Get the online player API
     *
     * @return the online version of PlayerAPI
     */
    @Nullable
    public final PlayerAPI getAPI() {
        Player player_instance = LockLoginSpigot.plugin.getServer().getPlayer(getUUID());

        if (player_instance != null) {
            return new PlayerAPI(module, player_instance);
        }

        return null;
    }
}
