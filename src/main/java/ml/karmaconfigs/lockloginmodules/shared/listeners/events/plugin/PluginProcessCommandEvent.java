package ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when a player or server
 * performs a LockLogin command ( $[argument] )
 */
public final class PluginProcessCommandEvent extends Event {

    private boolean handled = false;

    private final String arg;
    private final Object sender;
    private final String[] parameters;

    private final Object eventObj;

    /**
     * Initialize the event
     *
     * @param argument the command argument
     * @param sender the command sender
     * @param arguments the command arguments
     * @param event the event in where this event is fired
     */
    public PluginProcessCommandEvent(final String argument, final Object sender, final Object event, final String... arguments) {
        arg = argument;
        this.sender = sender;
        parameters = arguments;
        eventObj = event;
    }

    /**
     * Get the command argument
     *
     * @return the command argument
     */
    public final String getArgument() {
        return arg;
    }

    /**
     * Get the command parameters
     *
     * @return the command parameters
     */
    public final String[] getParameters() {
        return parameters;
    }

    /**
     * Get the command sender
     *
     * @return the command sender
     */
    public final Object getSender() {
        return sender;
    }

    /**
     * Set the event handle status
     *
     * @param status the handle status
     */
    @Override
    public void setHandled(boolean status) {
        handled = status;
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
     * Get the event instance
     *
     * @return the event instance
     */
    @Override
    public @Nullable Object getEvent() {
        return null;
    }
}
