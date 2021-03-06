package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.lockloginmodules.bungee.PluginModule;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

class TempPluginModule extends PluginModule {

    @Override
    public @NotNull Plugin owner() {
        return LockLoginBungee.plugin;
    }

    @Override
    public @NotNull String name() {
        return "Command temp module";
    }

    @Override
    public @NotNull String version() {
        return "1.0.0";
    }

    @Override
    public @NotNull String author() {
        return "KarmaDev";
    }

    @Override
    public @NotNull String description() {
        return "This module is used to access an API feature when using /lookup command";
    }

    @Override
    public @NotNull String update_url() {
        return "https://karmaconfigs.ml/";
    }
}
