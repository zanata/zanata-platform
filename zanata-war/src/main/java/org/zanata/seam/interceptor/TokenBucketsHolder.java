package org.zanata.seam.interceptor;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.zanata.util.Introspectable;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * Singleton holding all active callers' token bucket.
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public enum TokenBucketsHolder implements Introspectable {
    HOLDER;

    @Delegate
    private final Cache<String, TokenBucket> activeCallers =
            CacheBuilder.newBuilder().maximumSize(100).build();

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public Collection<String> getFieldNames() {

        return Lists.newArrayList(IntrospectableFields.Buckets.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String get(String fieldName) {
        IntrospectableFields field = IntrospectableFields.valueOf(fieldName);
        switch (field) {
            case Buckets:
                return Iterables.toString(peekCurrentBuckets());
            default:
                throw new IllegalArgumentException("unknown field:" + fieldName);
        }
    }

    private Iterable<String> peekCurrentBuckets() {
        ConcurrentMap<String, TokenBucket> map = activeCallers.asMap();
        return Iterables.transform(map.entrySet(),
                new Function<Map.Entry<String, TokenBucket>, String>() {

                    @Override
                    public String apply(Map.Entry<String, TokenBucket> input) {

                        TokenBucket bucket = input.getValue();
                        return input.getKey() + ":" + bucket.currentSize();
                    }
                });
    }

    private enum IntrospectableFields {
        Buckets
    }
}
