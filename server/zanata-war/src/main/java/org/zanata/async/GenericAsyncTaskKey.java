package org.zanata.async;

import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GenericAsyncTaskKey implements AsyncTaskKey {
    private static final long serialVersionUID = -8648519833116851231L;
    private final String id;

    GenericAsyncTaskKey() {
        id = UUID.randomUUID().toString();
    }

    public GenericAsyncTaskKey(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericAsyncTaskKey that = (GenericAsyncTaskKey) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
