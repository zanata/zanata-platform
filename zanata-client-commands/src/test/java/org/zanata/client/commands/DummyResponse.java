package org.zanata.client.commands;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;
import org.jboss.resteasy.util.GenericType;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 * @param <T>
 */
class DummyResponse<T> extends ClientResponse<T>
{
   private final Status status;
   private final T entity;

   protected DummyResponse(Status status, T entity)
   {
      this.status = status;
      this.entity = entity;
   }

   @Override
   public T getEntity()
   {
      return entity;
   }

   @Override
   public <T> T getEntity(Class<T> arg0)
   {
      return null;
   }

   @Override
   public <T> T getEntity(GenericType<T> arg0)
   {
      return null;
   }

   @Override
   public <T> T getEntity(Class<T> arg0, Type arg1)
   {
      return null;
   }

   @Override
   public <T> T getEntity(GenericType<T> arg0, Annotation[] arg1)
   {
      return null;
   }

   @Override
   public <T> T getEntity(Class<T> arg0, Type arg1, Annotation[] arg2)
   {
      return null;
   }

   @Override
   public Link getHeaderAsLink(String arg0)
   {
      return null;
   }

   @Override
   public MultivaluedMap<String, String> getHeaders()
   {
      return null;
   }

   @Override
   public LinkHeader getLinkHeader()
   {
      return null;
   }

   @Override
   public Link getLocation()
   {
      return null;
   }

   @Override
   public Status getResponseStatus()
   {
      return status;
   }

   @Override
   public void releaseConnection()
   {
   }

   @Override
   public int getStatus()
   {
      return status.getStatusCode();
   }

   @Override
   public MultivaluedMap<String, Object> getMetadata()
   {
      return null;
   }
}