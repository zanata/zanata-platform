package org.zanata.client.commands;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;
import org.jboss.resteasy.util.GenericType;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 * @param <T>
 */
public class DummyResponse<T> extends ClientResponse<T>
{
   private final Status status;
   private final T entity;

   public DummyResponse(Status status, T entity)
   {
      super();
      this.status = status;
      this.entity = entity;
   }

   @Override
   public T getEntity()
   {
      return entity;
   }

   @Override
   public <T2> T2 getEntity(Class<T2> arg0)
   {
      return null;
   }

   @Override
   public <T2> T2 getEntity(GenericType<T2> arg0)
   {
      return null;
   }

   @Override
   public <T2> T2 getEntity(Class<T2> arg0, Type arg1)
   {
      return null;
   }

   @Override
   public <T2> T2 getEntity(GenericType<T2> arg0, Annotation[] arg1)
   {
      return null;
   }

   @Override
   public <T2> T2 getEntity(Class<T2> arg0, Type arg1, Annotation[] arg2)
   {
      return null;
   }

   @Override
   public Link getHeaderAsLink(String arg0)
   {
      return null;
   }

   @Override
   public MultivaluedMap<String, Object> getHeaders()
   {
      return null;
   }

   @Override
   public MultivaluedMap<String, String> getResponseHeaders()
   {
      return null;
   }

   @Override
   public Link getLocationLink()
   {
      return null;
   }

   @Override
   public StatusType getStatusInfo()
   {
      return null;
   }

   @Override
   public <T> T readEntity(Class<T> entityType)
   {
      return null;
   }

   @Override
   public <T> T readEntity(javax.ws.rs.core.GenericType<T> entityType)
   {
      return null;
   }

   @Override
   public <T> T readEntity(Class<T> entityType, Annotation[] annotations)
   {
      return null;
   }

   @Override
   public <T> T readEntity(javax.ws.rs.core.GenericType<T> entityType, Annotation[] annotations)
   {
      return null;
   }

   @Override
   public boolean hasEntity()
   {
      return false;
   }

   @Override
   public boolean bufferEntity()
   {
      return false;
   }

   @Override
   public void close()
   {
   }

   @Override
   public MediaType getMediaType()
   {
      return null;
   }

   @Override
   public Locale getLanguage()
   {
      return null;
   }

   @Override
   public int getLength()
   {
      return 0;
   }

   @Override
   public Set<String> getAllowedMethods()
   {
      return null;
   }

   @Override
   public Map<String, NewCookie> getCookies()
   {
      return null;
   }

   @Override
   public EntityTag getEntityTag()
   {
      return null;
   }

   @Override
   public Date getDate()
   {
      return null;
   }

   @Override
   public Date getLastModified()
   {
      return null;
   }

   @Override
   public Set<javax.ws.rs.core.Link> getLinks()
   {
      return null;
   }

   @Override
   public boolean hasLink(String relation)
   {
      return false;
   }

   @Override
   public javax.ws.rs.core.Link getLink(String relation)
   {
      return null;
   }

   @Override
   public javax.ws.rs.core.Link.Builder getLinkBuilder(String relation)
   {
      return null;
   }

   @Override
   public MultivaluedMap<String, String> getStringHeaders()
   {
      return null;
   }

   @Override
   public String getHeaderString(String name)
   {
      return null;
   }

   @Override
   public LinkHeader getLinkHeader()
   {
      return null;
   }

   @Override
   public URI getLocation()
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

   @Override
   public Map<String, Object> getAttributes()
   {
      return null;
   }

   @Override
   public void resetStream()
   {
   }
}