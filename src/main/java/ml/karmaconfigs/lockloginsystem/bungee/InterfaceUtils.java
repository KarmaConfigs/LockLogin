package ml.karmaconfigs.lockloginsystem.bungee;

import ml.karmaconfigs.api.common.utils.StringUtils;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class InterfaceUtils {

    private static Main plugin;

    private static boolean ready_to_update = true;

    /**
     * Start the interface utils
     *
     * @param instance a Main instance
     */
    public final void setMain(Main instance) {
        plugin = instance;
    }

    /**
     * Get a Main instance
     *
     * @return the plugin BungeeCord main instance
     */
    public final Main getPlugin() {
        return plugin;
    }

    /**
     * Get the plugin name
     *
     * @return the plugin name
     */
    public final String getName() {
        return StringUtils.toColor("&c[ &4GSA &c] &eLockLogin");
    }

    /**
     * Get the plugin version
     *
     * @return the plugin version
     */
    public final String getVersion() {
        return StringUtils.toColor("&c" + plugin.getDescription().getVersion());
    }

    /**
     * Get the plugin version as
     * integer
     *
     * @return the plugin version ID
     */
    @Deprecated
    public final int getVersionID() {
        return Integer.parseInt(plugin.getDescription().getVersion()
                .replaceAll("[aA-zZ]", "")
                .replace(".", "")
                .replace(" ", ""));
    }

    /**
     * Check if the plugin is ready to update
     *
     * @return if the plugin is ready to update
     */
    public final boolean notReadyToUpdate() {
        return !ready_to_update;
    }

    /**
     * Set the plugin ready to update status
     *
     * @param status the ready to update status
     */
    public final void setReadyToUpdate(final boolean status) {
        ready_to_update = status;
    }
}
