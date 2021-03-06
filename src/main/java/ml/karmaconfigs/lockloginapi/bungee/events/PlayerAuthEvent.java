package ml.karmaconfigs.lockloginapi.bungee.events;

import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

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
public class PlayerAuthEvent extends Event {

    private final AuthType auth_type;
    private final ProxiedPlayer player;
    private EventAuthResult auth_result;
    private String auth_message;

    /**
     * Initialize the player auth event
     *
     * @param _auth_type the auth type
     * @param _auth_result the auth result
     * @param _player the player
     * @param _auth_message the auth message
     */
    public PlayerAuthEvent(final AuthType _auth_type, final EventAuthResult _auth_result, final ProxiedPlayer _player, final String _auth_message) {
        auth_type = _auth_type;
        auth_result = _auth_result;
        player = _player;
        auth_message = _auth_message;
    }

    /**
     * Get the event player
     *
     * @return the event player
     */
    public final ProxiedPlayer getPlayer() {
        return player;
    }

    /**
     * Get the auth type
     *
     * @return the auth type
     */
    public final AuthType getAuthType() {
        return auth_type;
    }

    /**
     * Get the auth result
     *
     * @return the auth result
     */
    public final EventAuthResult getAuthResult() {
        return auth_result;
    }

    /**
     * Set the auth result
     *
     * @param result the auth result
     */
    public final void setAuthResult(final EventAuthResult result) {
        auth_result = result;
    }

    /**
     * Get the auth message
     *
     * @return the auth message
     */
    public final String getAuthMessage() {
        return auth_message;
    }

    /**
     * Set the auth result with
     * auth message
     *
     * @param result the new auth result
     * @param message the new auth message
     */
    public final void setAuthResult(final EventAuthResult result, final String message) {
        auth_result = result;
        auth_message = message;
    }
}
