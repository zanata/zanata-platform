package org.zanata;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class MockResourceFactory implements ResourceFactory {
    private PropertyInjector propertyInjector;
    private Object obj;

    public MockResourceFactory(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object createResource(HttpRequest request, HttpResponse response,
            ResteasyProviderFactory factory) {
        propertyInjector.inject(request, response, obj);
        return obj;
    }

    @Override
    public Class<?> getScannableClass() {
        return obj.getClass();
    }

    @Override
    public void registered(ResteasyProviderFactory factory) {
        this.propertyInjector =
                factory.getInjectorFactory().createPropertyInjector(getScannableClass(), factory);
    }

    @Override
    public void requestFinished(HttpRequest request, HttpResponse response,
            Object resource) {
    }

    @Override
    public void unregistered() {
    }

}
