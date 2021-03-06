package ml.karmaconfigs.lockloginsystem.shared.version;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class GetLatestVersion {

    private final List<String> replaced = new ArrayList<>();
    private int latest;
    private String version = "";


    /**
     * Starts retrieving the info from the html file
     */
    public GetLatestVersion() {
        try {
            URL url = new URL("https://karmaconfigs.github.io/updates/LockLogin/latest.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String word;
            List<String> lines = new ArrayList<>();
            while ((word = reader.readLine()) != null)
                lines.add((word.replaceAll("\\s", "").isEmpty() ? "&f" : word));

            reader.close();
            for (String str : lines) {
                replaced.add(str
                        .replace("[", "{open}")
                        .replace("]", "{close}")
                        .replace(",", "{comma}")
                        .replace("_", "&"));
            }
            this.latest = Integer.parseInt(replaced.get(0).replaceAll("\\s", "").replaceAll("[aA-zZ]", "").replace(".", ""));
            this.version = replaced.get(0);
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while retrieving latest LockLogin version data", Level.INFO);
        }
    }

    /**
     * @return the latest version as integer
     */
    public final int getId() {
        return latest;
    }

    /**
     * Get the latest version
     *
     * @return the latest version string
     */
    public final String getVersion() {
        return version.replaceAll("\\s", "").replaceAll("[aA-zZ]", "").replaceAll("\\s", "");
    }

    /**
     * @return the latest version status (Snapshot - Release candidate - Release) and his version int
     */
    public final VersionChannel getChannel() {
        String version_text = version.replaceAll("\\s", "").replaceAll("[0-9]", "").replace(".", "").replace(" ", "");
        if (!version_text.replaceAll("\\s", "").isEmpty()) {
            version_text = version_text.toLowerCase();
        } else {
            version_text = "release";
        }

        switch (version_text.toLowerCase()) {
            case "beta":
            case "releasecandidate":
            case "rc":
                return VersionChannel.RC;
            case "snapshot":
                return VersionChannel.SNAPSHOT;
            case "release":
            default:
                return VersionChannel.RELEASE;
        }
    }

    /**
     * Gets the changelog
     *
     * @return the latest version changelog
     */
    public final String getChangeLog() {
        List<String> replace = new ArrayList<>();

        for (int i = 0; i < replaced.size(); i++) {
            if (i == 0) {
                replace.add("&b--------- &eChangeLog &6: &a{version} &b---------"
                        .replace("{version}", replaced.get(0)) + "&r");
            } else {
                replace.add(replaced.get(i).replace("-", "&8-&3"));
            }
        }

        return replace.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "\n")
                .replace("{open}", "[")
                .replace("{close}", "]")
                .replace("{comma}", ",");
    }
}
