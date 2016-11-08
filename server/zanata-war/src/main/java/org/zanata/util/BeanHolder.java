package org.zanata.util;

import java.io.Serializable;

import javax.inject.Provider;

import org.apache.deltaspike.core.api.provider.DependentProvider;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class BeanHolder<T> implements Provider<T>, AutoCloseable,
        Serializable {
    private final DependentProvider<T> provider;
    private final T bean;

    public BeanHolder(DependentProvider<T> provider) {
        this.provider = provider;
        bean = null;
    }

    @VisibleForTesting
    BeanHolder(T bean) {
        this.bean = bean;
        provider = null;
    }

    /**
     * This will not do anything for normal scoped (non-dependent) beans,
     * nor for beans handled in autowire tests.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        if (provider != null) {
            provider.destroy();
        } else {
            // NB lifecycle method will not be run in autowire tests
        }
    }

    @Override
    public T get() {
        if (provider != null) {
            return provider.get();
        } else {
            return bean;
        }
    }
}
