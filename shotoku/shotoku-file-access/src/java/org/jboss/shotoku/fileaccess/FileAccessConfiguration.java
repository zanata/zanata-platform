package org.jboss.shotoku.fileaccess;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class FileAccessConfiguration {
    private static final Logger log = Logger.getLogger(FileAccessServlet.class);

    private List<FileAccessMonitor> monitors;
    private List<Pattern> allowedPatterns;

    private String[] getTokensFromProperty(Properties props, String propName) {
        String prop = props.getProperty(propName);
        if ((prop == null) || ("".equals(prop))) {
            return new String[0];
        }

        return prop.split("[,]");
    }

    public FileAccessConfiguration(Properties props) {
        monitors = new ArrayList<FileAccessMonitor>();

        // Reading monitor classes.
        String[] monitorsTokens = getTokensFromProperty(props, "monitor.stack");
        for (String monitorToken : monitorsTokens) {
            try {
                monitors.add((FileAccessMonitor) Thread.currentThread().getContextClassLoader().loadClass(
                        monitorToken).newInstance());
            } catch (Exception e) {
                log.error("Error loading a file access monitor.", e);
            }
        }

        // Reading allowed patterns.
        allowedPatterns = new ArrayList<Pattern>();

        // 1. Allowed paths which contain a given string.
        String[] containingTokens = getTokensFromProperty(props, "allowed.paths.containing");
        if (containingTokens.length == 0) {
            allowedPatterns.add(Pattern.compile(".*"));   
        } else {
            for (String containingToken : containingTokens) {
                allowedPatterns.add(Pattern.compile(".*" + Pattern.quote(containingToken) + ".*"));
            }
        }
    }

    public List<FileAccessMonitor> getMonitors() {
        return monitors;
    }

    public boolean checkPath(String path) {
        if (path.indexOf("..") != -1) {
            return false;
        }

        for (Pattern p : allowedPatterns) {
            if (p.matcher(path).matches()) {
                return true;
            }
        }

        return false;
    }
}
