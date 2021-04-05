package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class PropertyManager implements LockLoginSpigot {

    private Class<?> property;

    public PropertyManager() {
        try {
            property = Class.forName("org.spongepowered.api.profile.property.ProfileProperty");
        } catch (Exception exe) {
            try {
                property = Class.forName("com.mojang.authlib.properties.Property");
            } catch (Exception e) {
                try {
                    property = Class.forName("net.md_5.bungee.connection.LoginResult$Property");
                } catch (Exception ex) {
                    try {
                        property = Class.forName("net.minecraft.util.com.mojang.authlib.properties.Property");
                    } catch (Exception exc) {
                        try {
                            property = Class.forName("com.velocitypowered.api.util.GameProfile$Property");
                        } catch (Exception exce) {
                            Console.send(plugin, "Could not find any skin provider", Level.INFO);
                        }
                    }
                }
            }
        }
    }

    public Object createProperty(String name, String value, String signature) {
        try {
            return ReflectionUtil.invokeConstructor(property,
                    new Class<?>[]{String.class, String.class, String.class}, name, value, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
