package org.zanata.client.config;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

public class ConfigUtil {

    /**
     * Finds the key prefix, within an ini subnode (eg [servers]) with the
     * specified url.
     * <p>
     * If the ini contains:
     *
     * <pre>
     * [servers]
     * a.username=alice
     * a.url=http://a.example.com
     * b.username=bob
     * b.url=http://b.example.com
     * </pre>
     *
     * then the code
     *
     * <pre>
     * servers = config.getSection("servers");
     * String prefix = findPrefix(servers, new URL("http://b.example.com"))
     * String username = servers.getString(prefix+".username");
     * </pre>
     *
     * will return "bob";
     * <p>
     * Due to an anomaly/bug in the current version of Apache Commons
     * Configuration's iterator, the returned prefix is actually "b." (with a
     * dot), and the runtime key for "b.url" is actually "b..url". Fortunately
     * the config.get* commands are consistent with this, so if you reference
     * <code>servers.getString(prefix+".username")</code> the value "bob" will
     * be returned anyway.
     *
     * @param config
     * @param url
     * @return
     */
    public static String findPrefix(SubnodeConfiguration config, URL url) {
        String suffix = ".url";
        DataConfiguration dataConfig = new DataConfiguration(config);

        for (Iterator<String> iterator = dataConfig.getKeys(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (key.endsWith(suffix)) {
                URL configURL = dataConfig.getURL(key);
                if (equal(url, configURL)) {
                    return key.substring(0, key.length() - suffix.length());
                }
            }
        }
        return null;
    }

    /**
     * Avoids DNS dependency for URL.equals.
     * @see http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html
     * @param a
     * @param b
     * @return
     */
    private static boolean equal(@Nonnull URL a, @Nullable URL b) {
        try {
            return b == null ? false : a.toURI().equals(b.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
