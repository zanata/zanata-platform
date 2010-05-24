package org.fedorahosted.flies.rest.service;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResourceFactory;

public class MockResourceFactory implements ResourceFactory {
	private PropertyInjector propertyInjector;
	private Object obj;
	
	public MockResourceFactory(Object obj) {
		this.obj = obj;
	}
	
	@Override
	public Object createResource(HttpRequest request, HttpResponse response,
			InjectorFactory factory) {
		propertyInjector.inject(request, response, obj);
		return obj;
	}

	@Override
	public Class<?> getScannableClass() {
		return obj.getClass();
	}

	@Override
	public void registered(InjectorFactory factory) {
		this.propertyInjector = factory
				.createPropertyInjector(getScannableClass());
	}

	@Override
	public void requestFinished(HttpRequest request, HttpResponse response,
			Object resource) {
	}

	@Override
	public void unregistered() {
	}

}
