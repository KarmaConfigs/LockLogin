package ml.karmaconfigs.lockloginsystem.bukkit.utils.files;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.karmayaml.FileCopy;
import ml.karmaconfigs.api.bukkit.karmayaml.YamlReloader;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.Lang;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
public final class ConfigGetter implements LockLoginSpigot {

    private final static File spigot = new File(plugin.getServer().getWorldContainer().getPath(), "spigot.yml");

    private final static File config = new File(plugin.getDataFolder(), "config.yml");
    private final static YamlConfiguration configuration = YamlConfiguration.loadConfiguration(config);

    private static boolean loaded = false;

    /**
     * Initialize plugin configuration
     */
    public ConfigGetter() {
        if (!config.exists()) {
            FileCopy creator = new FileCopy(plugin, "configs/config_spigot.yml");

            try {
                creator.copy(config);
                YamlReloader reloader = new YamlReloader(plugin, config, "configs/config_spigot.yml");
                if (reloader.reloadAndCopy())
                    configuration.loadFromString(reloader.getYamlString());
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while reloading config file");
            }
        }

        String svName = configuration.getString("ServerName", "");
        assert svName != null;

        if (svName.replaceAll("\\s", "").isEmpty()) {
            svName = StringUtils.randomString(8, StringUtils.StringGen.ONLY_LETTERS, StringUtils.StringType.ALL_LOWER);
            configuration.set("ServerName", svName);

            try {
                configuration.save(config);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            manager.reload();
        }

        if (!loaded) {
            manager.reload();
            loaded = true;
        }
    }

    public final boolean isBungeeCord() {
        if (spigot.exists()) {
            YamlConfiguration sp = YamlConfiguration.loadConfiguration(spigot);
            return sp.getBoolean("settings.bungeecord");
        } else {
            try {
                if (spigot.createNewFile()) {
                    Console.send(plugin, "LockLogin detected you are not running in spigot, so spigot.yml doesn't seems to exists, if your server.jar supports BungeeCord" +
                            " and you are using it, enable it in spigot.yml (Generated by LockLogin if it didn't exists)", Level.INFO);
                } else {
                    Console.send(plugin, "An error occurred while trying to generate spigot.yml, (You won't be able to enable bungee mode)", Level.INFO);
                }
                YamlConfiguration sp = YamlConfiguration.loadConfiguration(spigot);
                if (!sp.isSet("settings.bungeecord")) {
                    sp.set("settings.bungeecord", false);
                    try {
                        sp.save(spigot);
                    } catch (IOException e) {
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while creating artificial spigot.yml for bungee support");
                    }
                }

                return sp.getBoolean("settings.bungeecord");
            } catch (IOException e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while creating artificial spigot.yml for bungee support");
                return false;
            }
        }
    }

    public final String serverName() {
        String name = configuration.getString("ServerName", "");
        assert name != null;

        if (name.isEmpty()) {
            name = StringUtils.randomString(8, StringUtils.StringGen.ONLY_LETTERS, StringUtils.StringType.ALL_LOWER);
            configuration.set("ServerName", name);

            try {
                configuration.save(config);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            manager.reload();
        }

        return name;
    }

    public final boolean advancedFiltering() {
        return configuration.getBoolean("AdvancedFiltering");
    }

    public final Lang getLang() {
        String val = Objects.requireNonNull(configuration.getString("Lang", "en_EN")).toLowerCase();

        switch (val) {
            case "en_en":
            case "english":
                return Lang.ENGLISH;
            case "es_es":
            case "spanish":
                return Lang.SPANISH;
            case "zh_cn":
            case "simplified_chinese":
                return Lang.SIMPLIFIED_CHINESE;
            case "it_it":
            case "italian":
                return Lang.ITALIAN;
            case "pl_pl":
            case "polish":
                return Lang.POLISH;
            case "fr_fr":
            case "french":
                return Lang.FRENCH;
            case "cz_cs":
            case "czech":
                return Lang.CZECH;
            case "ru_ru":
            case "russian":
                return Lang.RUSSIAN;
            default:
                return Lang.UNKNOWN;
        }
    }

    public final String accountSys() {
        return configuration.getString("AccountSys", "file");
    }

    public final boolean isYaml() {
        return accountSys().equalsIgnoreCase("File") || accountSys().equalsIgnoreCase("file");
    }

    public final boolean isMySQL() {
        if (!isBungeeCord()) {
            return accountSys().equalsIgnoreCase("MySQL") || accountSys().equalsIgnoreCase("mysql");
        } else {
            return false;
        }
    }

    public final boolean accountSysValid() {
        return isYaml() || isMySQL();
    }

    public final boolean registerRestricted() {
        return configuration.getBoolean("Azuriom.Restrict", false);
    }

    public final boolean semiPremium() {
        return configuration.getBoolean("Azuriom.SemiPremium", false);
    }

    public final boolean blindRegister() {
        return configuration.getBoolean("Register.Blind", false);
    }

    public final boolean nauseaRegister() {
        return configuration.getBoolean("Register.Nausea", false);
    }

    public final int registerTimeOut() {
        return configuration.getInt("Register.TimeOut", 60);
    }

    public final int maxRegister() {
        return configuration.getInt("Register.Max", 2);
    }

    public final CryptType passwordEncryption() {
        String value = configuration.getString("Encryption.Passwords", "SHA512");
        assert value != null;

        switch (value.toLowerCase()) {
            case "256":
            case "sha256":
                return CryptType.SHA256;
            case "bcrypt":
                return CryptType.BCrypt;
            case "argon2i":
                return CryptType.ARGON2I;
            case "argon2id":
                return CryptType.ARGON2ID;
            case "512":
            case "sha512":
            default:
                return CryptType.SHA512;
        }
    }

    public final CryptType pinEncryption() {
        String value = configuration.getString("Encryption.Pins", "SHA512");
        assert value != null;

        switch (value.toLowerCase()) {
            case "256":
            case "sha256":
                return CryptType.SHA256;
            case "bcrypt":
                return CryptType.BCrypt;
            case "argon2i":
                return CryptType.ARGON2I;
            case "argon2id":
                return CryptType.ARGON2ID;
            case "512":
            case "sha512":
            default:
                return CryptType.SHA512;
        }
    }

    public final boolean blindLogin() {
        return configuration.getBoolean("Login.Blind", true);
    }

    public final boolean nauseaLogin() {
        return configuration.getBoolean("Login.Nausea", true);
    }

    public final int loginTimeOut() {
        return configuration.getInt("Login.TimeOut", 30);
    }

    public final int loginMaxTries() {
        return configuration.getInt("Login.MaxTries", 5);
    }

    public final int registerInterval() {
        int value = configuration.getInt("MessagesInterval.Register", 5);

        if (value < 5 || value > registerTimeOut()) {
            value = 5;
            configuration.set("MessagesInterval.Registration", value);

            try {
                configuration.save(config);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            manager.reload();
        }

        return value;
    }

    public final int loginInterval() {
        int value = configuration.getInt("MessagesInterval.Logging", 5);

        if (value < 5 || value > loginTimeOut()) {
            value = 5;
            configuration.set("MessagesInterval.Login", value);

            try {
                configuration.save(config);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            manager.reload();
        }

        return value;
    }

    public final CaptchaType getCaptchaType() {
        String val = configuration.getString("Captcha.Mode", "SIMPLE");
        assert val != null;

        switch (val.toLowerCase()) {
            case "simple":
                return CaptchaType.SIMPLE;
            case "disabled":
                return CaptchaType.DISABLED;
            case "complex":
            default:
                return CaptchaType.COMPLEX;
        }
    }

    public final int getCaptchaTimeOut() {
        return configuration.getInt("Captcha.TimeOut");
    }

    public final int getCaptchaLength() {
        int val = configuration.getInt("Captcha.Difficulty.Length");

        if (val >= 4 && val <= 8)
            return val;
        else
            return 6;
    }

    public final boolean letters() {
        return configuration.getBoolean("Captcha.Difficulty.Letters", true);
    }

    public final boolean strikethrough() {
        return configuration.getBoolean("Captcha.Strikethrough.Enabled", true);
    }

    public final boolean randomStrikethrough() {
        return configuration.getBoolean("Captcha.Strikethrough.Random", true);
    }

    public final int bfMaxTries() {
        return configuration.getInt("BruteForce.Tries");
    }

    public final int bfBlockTime() {
        int val = configuration.getInt("BruteForce.BlockTime");

        if (val <= 0)
            val = 30;

        return (int) TimeUnit.MINUTES.toSeconds(val);
    }

    public final boolean antiBot() {
        return configuration.getBoolean("AntiBot", false);
    }

    public final boolean allowSameIP() {
        return configuration.getBoolean("AllowSameIp", true);
    }

    public final boolean enablePin() {
        return configuration.getBoolean("Pin", true);
    }

    public final VersionChannel getUpdateChannel() {
        String value = configuration.getString("Updater.Channel", "RELEASE");
        assert value != null;

        switch (value.toLowerCase()) {
            case "rc":
            case "releasecandidate":
            case "release_candidate":
                return VersionChannel.RC;
            case "snapshot":
                return VersionChannel.SNAPSHOT;
            case "release":
            default:
                return VersionChannel.RELEASE;
        }
    }

    public final boolean checkUpdates() {
        return configuration.getBoolean("Updater.Check", true);
    }

    public final int checkInterval() {
        if (configuration.getInt("Updater.CheckTime", 10) >= 5 && configuration.getInt("Updater.CheckTime", 10) <= 86400) {
            return (int) TimeUnit.MINUTES.toSeconds(configuration.getInt("Updater.CheckTime"));
        } else {
            configuration.set("Updater.CheckTime", 5);
            return (int) TimeUnit.MINUTES.toSeconds(5);
        }
    }

    public final boolean enable2FA() {
        return configuration.getBoolean("2FA", true);
    }

    public final boolean enableSpawn() {
        return configuration.getBoolean("Spawn.Manage", false);
    }

    public final boolean takeBack() {
        return configuration.getBoolean("Spawn.TakeBack", false);
    }

    public final boolean clearChat() {
        return configuration.getBoolean("ClearChat", false);
    }

    public final int accountsPerIp() {
        return configuration.getInt("AccountsPerIp", 2);
    }

    public final boolean checkNames() {
        return configuration.getBoolean("CheckNames", false);
    }

    /**
     * Configuration manager utilities
     */
    public interface manager {

        /**
         * Reload configuration file
         *
         * @return if the file could be reloaded
         */
        static boolean reload() {
            try {
                InputStream stream = plugin.getResource("configs/config_spigot.yml");
                if (stream != null) {
                    YamlReloader reloader = new YamlReloader(plugin, config, "configs/config_spigot.yml");
                    if (reloader.reloadAndCopy()) {
                        configuration.loadFromString(reloader.getYamlString());
                        return true;
                    }
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while reloading config file");
            }

            return false;
        }
    }
}