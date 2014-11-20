package org.zanata.rest.dto.stats.contribution;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.zanata.common.LocaleId;

/**
 * Map that hold user contribution data
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleStatistics implements Serializable,
        Map<LocaleId, BaseContributionStatistic> {

    private Map<LocaleId, BaseContributionStatistic> localeStatsMap =
            new HashMap<LocaleId, BaseContributionStatistic>();

    @Override
    public int size() {
        return localeStatsMap.size();
    }

    @Override
    public boolean isEmpty() {
        return localeStatsMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return localeStatsMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return localeStatsMap.containsValue(value);
    }

    @Override
    public BaseContributionStatistic get(Object key) {
        return localeStatsMap.get(key);
    }

    @Override
    public BaseContributionStatistic put(LocaleId key,
        BaseContributionStatistic value) {
        return localeStatsMap.put(key, value);
    }

    @Override
    public BaseContributionStatistic remove(Object key) {
        return localeStatsMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends LocaleId, ? extends BaseContributionStatistic> m) {
        localeStatsMap.putAll(m);
    }

    @Override
    public void clear() {
        localeStatsMap.clear();
    }

    @Override
    public Set<LocaleId> keySet() {
        return localeStatsMap.keySet();
    }

    @Override
    public Collection<BaseContributionStatistic> values() {
        return localeStatsMap.values();
    }

    @Override
    public Set<Entry<LocaleId, BaseContributionStatistic>> entrySet() {
        return localeStatsMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocaleStatistics)) return false;

        LocaleStatistics that = (LocaleStatistics) o;

        if (localeStatsMap != null ?
            !localeStatsMap.equals(that.localeStatsMap) :
            that.localeStatsMap != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return localeStatsMap != null ? localeStatsMap.hashCode() : 0;
    }
}
