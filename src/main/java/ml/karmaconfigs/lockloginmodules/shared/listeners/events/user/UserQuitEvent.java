package ml.karmaconfigs.lockloginmodules.shared.listeners.events.user;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;

/**
 * This event is fired when a user quits
 * the server at the eyes of the plugin.
 *
 * This means, this event will be fire when
 * a player quits the server, or it's kicked...
 */
public final class UserQuitEvent extends Event {

    private boolean handled = false;

    private final Object player;
    private final Object eventObj;

    /**
     * Initialize the event
     *
     * @param playerObject the player object
     * @param event the event in where this event is fired
     */
    public UserQuitEvent(final Object playerObject, final Object event) {
        player = playerObject;
        eventObj = event;
    }

    /**
     * Set the event handle status
     *
     * @param status the handle status
     */
    @Override
    public void setHandled(boolean status) {
        handled = true;
    }

    /**
     * Check if the event has been handled
     *
     * @return if the event has been handled
     */
    @Override
    public boolean isHandled() {
        return handled;
    }

    /**
     * Get the player
     *
     * @return the player
     */
    public final Object getPlayer() {
        return player;
    }

    /**
     * Get the event instance
     *
     * @return the event instance
     */
    @Override
    public Object getEvent() {
        return eventObj;
    }
}
