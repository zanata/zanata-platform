package org.jboss.shotoku.fileaccess;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.cache.ShotokuResourceWatcher;
import org.jboss.shotoku.cache.ChangeType;
import org.jboss.shotoku.cache.ValueChange;
import org.jboss.shotoku.cache.ValueInit;
import org.jboss.shotoku.tools.Pair;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Properties;
import java.io.IOException;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class FileAccessConfigurationWatcher extends ShotokuResourceWatcher<Pair<String, String>,
        FileAccessConfiguration> {
    private static final Logger log = Logger.getLogger(FileAccessServlet.class);

    private static final String CONFIG_FILE_NAME = "file-access.properties";

    private FileAccessConfiguration getConfiguration(Pair<String, String> key) {
        Properties props = new Properties();

        try {
            props.load(getContentManager(key).getNode(
                    CONFIG_FILE_NAME).getContentInputStream());
        } catch (IOException e) {
            log.error(e);
        } catch (ResourceDoesNotExist e) {
            // Well ...
            log.error(e);
        }

        return new FileAccessConfiguration(props);
    }

    public ValueInit<FileAccessConfiguration> init(Pair<String, String> key) {
        addWatchedPath(key, CONFIG_FILE_NAME);
        return ValueInit.realValue(getConfiguration(key));
    }

    protected ContentManager initContentManager(Pair<String, String> key) {
        return ContentManager.getContentManager(key.getFirst(), key.getSecond());
    }

    protected ValueChange<FileAccessConfiguration> update(Pair<String, String> key, FileAccessConfiguration currentObject,
                          Map<String, ChangeType> changes) {
        return ValueChange.changeTo(getConfiguration(key));
    }
}
