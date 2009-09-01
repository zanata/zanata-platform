package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.jboss.resteasy.spi.InternalDispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.touri.ObjectToURI;

public class UriAdapter extends XmlAdapter<String, Object> {
  @Override
  public String marshal(Object domainObject) throws Exception {
    return ObjectToURI.getInstance().resolveURI(domainObject);
  }

  @Override
  public Object unmarshal(String uri) throws Exception {
    return ResteasyProviderFactory.getContextData(InternalDispatcher.class)
            .getEntity(uri);
  }
}