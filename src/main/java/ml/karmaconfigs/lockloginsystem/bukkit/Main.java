package ml.karmaconfigs.lockloginsystem.bukkit;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.JarInjector;
import ml.karmaconfigs.api.common.KarmaPlugin;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginmodules.Module;
import ml.karmaconfigs.lockloginmodules.shared.NoJarException;
import ml.karmaconfigs.lockloginmodules.shared.NoModuleException;
import ml.karmaconfigs.lockloginmodules.bukkit.AdvancedModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.dependencies.Dependency;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.PluginManagerBukkit;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.PlayerFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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
@KarmaPlugin(plugin_update_url = "https://karmaconfigs.github.io/updates/LockLogin/latest.txt")
public final class Main extends JavaPlugin {

    @Override
    public final void onEnable() {
        File tmp_jar = new File(Main.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());

        String version = FileInfo.getKarmaVersion(tmp_jar);
        File dest_file = new File(getDataFolder() + File.separator + "api" + File.separator + version, "KarmaAPI-Bundle.jar");

        InputStream is = null;
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try {
            URL download_url = new URL("https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/KarmaAPI/" + version + "/KarmaAPI-Bundle.jar");
            URLConnection connection = download_url.openConnection();

            System.out.println("Checking API version, please wait...");

            if (!dest_file.exists() || connection.getContentLengthLong() != dest_file.length()) {
                System.out.println("API file not downloaded, please wait until LockLogin downloads it...");

                if (!dest_file.getParentFile().exists())
                    Files.createDirectories(dest_file.getParentFile().toPath());

                if (!dest_file.exists())
                    Files.createFile(dest_file.toPath());

                TrustManager[] trustManagers = new TrustManager[]{new NvbTrustManager()};
                final SSLContext context = SSLContext.getInstance("SSL");
                context.init(null, trustManagers, null);

                // Set connections to use lenient TrustManager and HostnameVerifier
                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new NvbHostnameVerifier());

                is = download_url.openStream();
                rbc = Channels.newChannel(is);
                fos = new FileOutputStream(dest_file);

                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        } finally {
            try {
                if (rbc != null) {
                    rbc.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (Throwable ignored) {}

            try {
                URLClassLoader cl = (URLClassLoader) Main.class.getClassLoader();
                Class<?> clazz = URLClassLoader.class;

                // Get the protected addURL method from the parent URLClassLoader class
                Method method = clazz.getDeclaredMethod("addURL", URL.class);

                // Run projected addURL method to add JAR to classpath
                method.setAccessible(true);
                method.invoke(cl, dest_file.toURI().toURL());
            } catch (Throwable ex) {
                ex.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
            }
        }

        File modulesFolder = new File(getDataFolder(), "modules");
        if (!modulesFolder.exists()) {
            try {
                Files.createDirectories(modulesFolder.toPath());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        
        CurrentPlatform current = new CurrentPlatform();
        current.setRunning(Platform.BUKKIT);

        LockLoginSpigot.logger.setMaxSize(FileInfo.logFileSize((LockLoginSpigot.getJar())));

        boolean injected = true;
        Set<Dependency> success = new HashSet<>();
        Set<Dependency> error = new HashSet<>(Arrays.asList(Dependency.values()));
        Set<AdvancedModuleLoader> loaders = new LinkedHashSet<>();
        try {
            Console.setOkPrefix(this, "&8[ &eLockLogin &8] &aOK &f>> &7");
            Console.setInfoPrefix(this, "&8[ &eLockLogin &8] &7INFO &f>> &b");
            Console.setWarningPrefix(this, "&8[ &eLockLogin &8] &6WARNING &f>> &e");
            Console.setGravePrefix(this, "&8[ &eLockLogin &8] &4GRAVE &f>> &c");

            File[] modules = modulesFolder.listFiles();
            if (modules != null) {
                Injector injector = new Injector(LockLoginSpigot.getJar());

                for (File module : modules) {
                    if (module.isFile() && module.getName().endsWith(".jar")) {
                        AdvancedModuleLoader loader = new AdvancedModuleLoader(module);
                        injector.inject(loader.getMainClass());
                        loaders.add(loader);

                        JarInjector subInjector = new JarInjector(module);
                        subInjector.inject(this);
                    }
                }
            }
            
            for (Dependency dependency : Dependency.values()) {
                File target = new File(getDataFolder() + File.separator + "libraries", dependency.fileName());

                JarInjector injector = new JarInjector(target);
                if (injector.isDownloaded() && injector.inject(this)) {
                    success.add(dependency);
                    error.remove(dependency);
                } else {
                    injected = false;
                }
            }
        } catch (Throwable ex) {
            LockLoginSpigot.logger.scheduleLog(Level.GRAVE, ex);
            LockLoginSpigot.logger.scheduleLog(Level.INFO, "An internal error occurred while trying to load LockLogin");

            Console.send(this, "An error occurred while trying to enable LockLogin but it may work anyway, check /LockLogin/logs for more info", Level.GRAVE);
        } finally {
            if (!injected) {
                try {
                    Set<Dependency> copyError = new HashSet<>(error);

                    for (Dependency dependency : copyError) {
                        File target = new File(getDataFolder() + File.separator + "libraries", dependency.fileName());

                        JarInjector injector = new JarInjector(target);
                        injector.download(dependency.downloadURL());

                        if (injector.inject(this)) {
                            success.add(dependency);
                            error.remove(dependency);
                        }
                    }
                } catch (Throwable ex) {
                    LockLoginSpigot.logger.scheduleLog(Level.GRAVE, ex);
                    LockLoginSpigot.logger.scheduleLog(Level.INFO, "An internal error occurred while trying to load LockLogin");

                    Console.send(this, "An error occurred while trying to enable LockLogin and it may not work, check /LockLogin/logs for more info", Level.GRAVE);
                }
            }
        }

        if (!success.isEmpty())
            LockLoginSpigot.logger.scheduleLog(Level.INFO, "Successfully injected dependencies: " + success.toString().replace("[", "").replaceAll("]", ""));
        if (!error.isEmpty())
            LockLoginSpigot.logger.scheduleLog(Level.INFO, "Failed dependency injections: " + error.toString().replace("[", "").replace("]", ""));

        for (AdvancedModuleLoader loader : loaders) {
            Module module = loader.getAsModule();
            if (module != null) {
                try {
                    loader.inject();
                } catch (NoJarException | NoModuleException ex) {
                    ex.printStackTrace();
                    Console.send(this, "Failed to inject module " + module.name() + " ( " + ex.fillInStackTrace() + " )", Level.GRAVE);
                    ex.printStackTrace();
                } catch (IOException ex) {
                    Console.send(this, "Failed to inject module " + module.name() + " ( " + ex.fillInStackTrace() + " )", Level.WARNING);
                    ex.printStackTrace();
                }
            }
        }

        PluginManagerBukkit ms = new PluginManagerBukkit();
        ms.enable();
        LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin initialized");

        PlayerFile.migrateV1();
        PlayerFile.migrateV2();

        IPStorager.migrateFromV2();
        IPStorager.migrateFromV3();
        IPStorager.migrateFromV4();
    }

    @Override
    public final void onDisable() {
        PluginManagerBukkit ms = new PluginManagerBukkit();
        ms.disable();
        LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin disabled");
    }

    /**
     * Simple <code>TrustManager</code> that allows unsigned certificates.
     */
    private static final class NvbTrustManager implements TrustManager, X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) { }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) { }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    /**
     * Simple <code>HostnameVerifier</code> that allows any hostname and session.
     */
    private static final class NvbHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}


/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.github.io/page/license"> here </a>
 */
class Injector {

    private final File jarFile;

    /**
     * Initialize the injector class with
     * the specified file
     *
     * @param file the file
     */
    public Injector(@NotNull final File file) {
        jarFile = file;
    }

    /**
     * Inject the jar into the plugin
     */
    public final void inject(final Class<?> module) {
        URLClassLoader cl;
        try {
            // Get the ClassLoader class
            cl = (URLClassLoader) module.getClassLoader();
            Class<?> clazz = URLClassLoader.class;

            // Get the protected addURL method from the parent URLClassLoader class
            Method method = clazz.getDeclaredMethod("addURL", URL.class);

            // Run projected addURL method to add JAR to classpath
            method.setAccessible(true);
            method.invoke(cl, jarFile.toURI().toURL());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }/* finally {
            if (cl != null)
                try {
                    cl.close();
                } catch (Throwable ignored) {}
        }*/
    }
}