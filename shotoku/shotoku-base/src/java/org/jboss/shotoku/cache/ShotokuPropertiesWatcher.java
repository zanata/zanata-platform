package org.jboss.shotoku.cache;

import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

import java.util.Properties;
import java.util.Map;
import java.io.IOException;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class ShotokuPropertiesWatcher<K> extends ShotokuResourceWatcher<K, Properties>  {
    protected abstract String getConfigFileName();

    private Properties getProperties(K key) {
        try {
            Properties ret = new Properties();
            ret.load(getContentManager(key).getNode(
                    getConfigFileName()).getContentInputStream());
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResourceDoesNotExist e) {
            // Well ...
            e.printStackTrace();
        }

        return null;
    }

    public ValueInit<? extends Properties> init(K key) {
        addWatchedPath(key, getConfigFileName());
        return ValueInit.realValue(getProperties(key));
    }

    protected ValueChange<Properties> update(K key, Properties currentObject,
                          Map<String, ChangeType> changes) {
        return ValueChange.changeTo(getProperties(key));
    }
}
