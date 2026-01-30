package jodelle.powermining.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class responsible for checking GitHub for newer plugin releases.
 *
 * <p>
 * This class queries the GitHub REST API for the latest release of the
 * PowerMining plugin and compares the reported version against the currently
 * running plugin version.
 * </p>
 *
 * <p>
 * Network communication is executed asynchronously to avoid blocking the
 * Bukkit main thread. If a newer version is detected, a notification message
 * is sent to the server console on the main thread.
 * </p>
 *
 * <p>
 * Version comparison is performed using a best-effort semantic version
 * approach. Common Git tag prefixes (such as {@code v1.4.0}) are supported.
 * </p>
 */
public final class GitHubUpdater {

    /**
     * GitHub API endpoint that provides information about the latest release.
     */
    private static final String GITHUB_API_URL = "https://api.github.com/repos/dringewald/JodellePowerMining/releases/latest";

    /**
     * Regular expression used to extract the {@code tag_name} field from the
     * GitHub JSON response.
     */
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>
     * This class is intended to be used as a static utility only.
     * </p>
     */
    private GitHubUpdater() {
        // Utility class
    }

    /**
     * Asynchronously checks GitHub for a newer plugin version.
     *
     * <p>
     * If a newer version than the currently running one is detected, a message
     * is printed to the server console informing administrators about the
     * available update.
     * </p>
     *
     * @param plugin
     *               The plugin instance used for scheduling tasks and retrieving
     *               the
     *               current plugin version.
     *
     * @throws NullPointerException
     *                              If {@code plugin} is {@code null}.
     */
    public static void checkForUpdates(final JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String json = fetchLatestReleaseJson();
                String latestTag = extractTagName(json);

                if (latestTag == null || latestTag.isBlank()) {
                    plugin.getLogger().warning("Could not extract tag_name from GitHub response.");
                    return;
                }

                String currentVersion = plugin.getDescription().getVersion();
                if (currentVersion == null || currentVersion.isBlank()) {
                    plugin.getLogger().warning("Current plugin version is not set in plugin.yml.");
                    return;
                }

                if (isNewerVersion(latestTag, currentVersion)) {
                    String msg = ChatColor.YELLOW
                            + "[PowerMining] A new version (" + latestTag + ") is available! "
                            + "Download it at: https://github.com/dringewald/JodellePowerMining/releases";

                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getConsoleSender().sendMessage(msg));
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to check for updates (I/O): " + e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    /**
     * Fetches the raw JSON response of the latest GitHub release.
     *
     * @return
     *         The JSON response body as a string.
     *
     * @throws Exception
     *                   If an error occurs while connecting to GitHub or reading
     *                   the
     *                   response.
     */
    private static String fetchLatestReleaseJson() throws Exception {
        URI uri = new URI(GITHUB_API_URL);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "PowerMining-Updater");
            connection.setRequestProperty("Accept", "application/vnd.github+json");

            connection.setConnectTimeout(7_500);
            connection.setReadTimeout(7_500);

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String errorBody = readAll(connection.getErrorStream());
                throw new IOException(
                        "HTTP " + code + " while fetching update information. Body: " + errorBody);
            }

            return readAll(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Reads an entire {@link InputStream} into a string using UTF-8 encoding.
     *
     * @param in
     *           The input stream to read from. May be {@code null}.
     *
     * @return
     *         The stream contents as a string, or an empty string if the stream
     *         is {@code null}.
     *
     * @throws IOException
     *                     If an I/O error occurs while reading.
     */
    private static String readAll(final InputStream in) throws IOException {
        if (in == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder(1024);
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * Extracts the release tag name from a GitHub API JSON response.
     *
     * @param json
     *             The raw JSON response string.
     *
     * @return
     *         The extracted tag name, or {@code null} if it could not be found.
     */
    private static String extractTagName(final String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        Matcher m = TAG_NAME_PATTERN.matcher(json);
        if (!m.find()) {
            return null;
        }
        return m.group(1);
    }

    /**
     * Determines whether the given GitHub version is newer than the current
     * plugin version.
     *
     * <p>
     * The comparison is based on numeric version segments and ignores common
     * prefixes such as {@code v}.
     * </p>
     *
     * @param latestTag
     *                       The version tag retrieved from GitHub.
     * @param currentVersion
     *                       The currently running plugin version.
     *
     * @return
     *         {@code true} if the GitHub version is newer, {@code false}
     *         otherwise.
     */
    private static boolean isNewerVersion(final String latestTag, final String currentVersion) {
        String latest = normalizeVersion(latestTag);
        String current = normalizeVersion(currentVersion);

        int[] latestParts = parseVersionParts(latest);
        int[] currentParts = parseVersionParts(current);

        if (latestParts == null || currentParts == null) {
            return !latest.equalsIgnoreCase(current);
        }

        int max = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < max; i++) {
            int l = (i < latestParts.length) ? latestParts[i] : 0;
            int c = (i < currentParts.length) ? currentParts[i] : 0;
            if (l != c) {
                return l > c;
            }
        }
        return false;
    }

    /**
     * Normalizes a version string by trimming whitespace and removing a leading
     * {@code v} or {@code V} prefix if present.
     *
     * @param v
     *          The raw version string.
     *
     * @return
     *         The normalized version string.
     */
    private static String normalizeVersion(final String v) {
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * Parses a version string into numeric components.
     *
     * <p>
     * Suffixes such as {@code -SNAPSHOT} are ignored. Only the numeric
     * dot-separated
     * portion is considered.
     * </p>
     *
     * @param version
     *                The version string to parse.
     *
     * @return
     *         An array of integer version parts, or {@code null} if parsing fails.
     */
    private static int[] parseVersionParts(final String version) {
        if (version == null || version.isBlank()) {
            return null;
        }

        String base = version.split("-", 2)[0];
        String[] parts = base.split("\\.");
        int[] out = new int[parts.length];

        try {
            for (int i = 0; i < parts.length; i++) {
                out[i] = Integer.parseInt(parts[i]);
            }
            return out;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}