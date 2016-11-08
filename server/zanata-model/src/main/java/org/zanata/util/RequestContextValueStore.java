package org.zanata.util;

import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;

/**
 * To store value in request scope.
 *
 * @deprecated this is just to provide backward compatibility as Seam offers
 *             this functionality. We should produce values in request scope
 *             instead.
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Deprecated
@RequestScoped
public class RequestContextValueStore {
    private static final Logger log =
            LoggerFactory.getLogger(RequestContextValueStore.class);
    private Map<String, Object> store = Maps.newHashMap();

    public void put(String key, Object value) {
        store.put(key, value);
    }

    public <T> Optional<T> get(String key) {
        Object o = store.get(key);
        if (o != null) {
            try {
                T value = (T) o;
                return Optional.of(value);
            } catch (ClassCastException e) {
                log.error(
                        "value stored is type {} and can not be casted to the asking type",
                        o.getClass());
            }
      }
        return Optional.empty();
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }
}
