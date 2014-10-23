package org.zanata.rest.dto.stats.contribution;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map that holds user contribution statistics
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ContributionStatistics implements Map<String, LocaleStatistics>,
        Serializable {

    private Map<String, LocaleStatistics> map =
            new HashMap<String, LocaleStatistics>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public LocaleStatistics get(Object key) {
        return map.get(key);
    }

    @Override
    public LocaleStatistics put(String key, LocaleStatistics value) {
        return map.put(key, value);
    }

    @Override
    public LocaleStatistics remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends LocaleStatistics> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<LocaleStatistics> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, LocaleStatistics>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContributionStatistics)) return false;

        ContributionStatistics that = (ContributionStatistics) o;

        if (map != null ? !map.equals(that.map) : that.map != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }
}
