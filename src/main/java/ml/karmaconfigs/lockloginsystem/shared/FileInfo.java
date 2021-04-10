package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
public interface FileInfo {

    static String getJarVersion(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                yml.close();
                return values.getOrDefault("project_version", "1.0.0").toString();
            }
            jar.close();

            return "-1";
        } catch (Throwable ex) {
            return "-1";
        }
    }

    static boolean apiDebug(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                yml.close();
                return Boolean.parseBoolean(values.getOrDefault("project_debug", false).toString());
            }
            jar.close();

            return false;
        } catch (Throwable ex) {
            return false;
        }
    }

    static boolean unsafeUpdates(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                yml.close();
                return Boolean.parseBoolean(values.getOrDefault("project_unsafeUpdate", false).toString());
            }
            jar.close();

            return false;
        } catch (Throwable ex) {
            return false;
        }
    }

    static int logFileSize(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                yml.close();
                return Integer.parseInt(values.getOrDefault("project_logSizeLimit", 100).toString());
            }
            jar.close();

            return 100;
        } catch (Throwable ex) {
            return 100;
        }
    }

    static VersionChannel getChannel(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                yml.close();

                String value = values.getOrDefault("project_build", "RELEASE").toString();
                return VersionChannel.valueOf(value.toUpperCase());
            }
            jar.close();

            return VersionChannel.RELEASE;
        } catch (Throwable ex) {
            return VersionChannel.RELEASE;
        }
    }
}
