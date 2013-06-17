/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.seam.util.Naming;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.zanata.arquillian.RemoteAfter;
import org.zanata.arquillian.RemoteBefore;
import org.zanata.provider.DBUnitProvider;
import org.zanata.provider.JPAProvider;
import org.zanata.rest.ResourceRequestEnvironment;
import org.zanata.rest.client.TestProxyFactory;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.rest.helper.RemoteTestSignaler;

/**
 * Provides basic test utilities to test raw REST APIs and compatibility.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(Arquillian.class)
public abstract class RestTest
{
   // Admin credentials
   protected static final String ADMIN = "admin";
   protected static final String ADMIN_KEY = "b6d7044e9ee3b2447c28fb7c50d86d98";
   // Translator credentials
   protected static final String TRANSLATOR = "demo";
   protected static final String TRANSLATOR_KEY = "23456789012345678901234567890123";

   // Authorized environment with valid credentials
   private static final ResourceRequestEnvironment ENV_AUTHORIZED =
         new ResourceRequestEnvironment()
         {
            @Override
            public Map<String, Object> getDefaultHeaders()
            {
               return new HashMap<String, Object>()
               {
                  {
                     put("X-Auth-User", ADMIN);
                     put("X-Auth-Token", ADMIN_KEY);
                  }
               };
            }
         };


   private DBUnitProvider dbUnitProvider = new DBUnitProvider() {
      @Override
      protected IDatabaseConnection getConnection()
      {
         return RestTest.getConnection();
      }
   };

   @ArquillianResource
   protected URL deploymentUrl;

   /**
    * Implement this in a subclass.
    * <p/>
    * Use it to stack DBUnit <tt>DataSetOperation</tt>'s with the
    * <tt>beforeTestOperations</tt> and <tt>afterTestOperations</tt> lists.
    */
   protected abstract void prepareDBUnitOperations();

   public void addBeforeTestOperation(DBUnitProvider.DataSetOperation operation)
   {
      dbUnitProvider.addBeforeTestOperation(operation);
   }

   public void addAfterTestOperation(DBUnitProvider.DataSetOperation operation)
   {
      dbUnitProvider.addAfterTestOperation(operation);
   }

   /**
    * Invoked on the arquillian container before the test is run.
    */
   @RemoteBefore
   public void prepareDataBeforeTest()
   {
      prepareDBUnitOperations();
      dbUnitProvider.prepareDataBeforeTest();
   }

   /**
    * Invoked in the arquillian container after the test is run.
    */
   @RemoteAfter
   public void cleanDataAfterTest()
   {
      dbUnitProvider.cleanDataAfterTest();
   }

   @Before
   public void signalBeforeTest()
   {
      RemoteTestSignaler signaler = ProxyFactory.create(RemoteTestSignaler.class, getRestEndpointUrl());
      try
      {
         signaler.signalBeforeTest(this.getClass().getName());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @After
   public void signalAfterTest()
   {
      RemoteTestSignaler signaler = ProxyFactory.create(RemoteTestSignaler.class, getRestEndpointUrl());
      try
      {
         signaler.signalAfterTest(this.getClass().getName());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private static IDatabaseConnection getConnection()
   {
      try
      {
         DataSource dataSource = (DataSource)Naming.getInitialContext().lookup("java:jboss/datasources/zanataTestDatasource");
         DatabaseConnection dbConn = new DatabaseConnection(dataSource.getConnection());
         // NB: Specific to H2
         dbConn.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
         return dbConn;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * @return The artifact's base deployment Url.
    */
   public String getDeploymentUrl()
   {
      return deploymentUrl.toString();
   }

   /**
    * Gets the full Url for a Rest endpoint.
    *
    * @param resourceUrl The relative resource url.
    * @return The full absolute url of the deployed resource.
    */
   public final String getRestEndpointUrl(String resourceUrl)
   {
      StringBuilder fullUrl = new StringBuilder(getDeploymentUrl() + "seam/resource/restv1");
      if( !resourceUrl.startsWith("/") )
      {
         fullUrl.append("/");
      }
      return fullUrl.append(resourceUrl).toString();
   }

   /**
    * Gets the artifact's deployed url for REST endpoints.
    *
    * @return The full absolute root url of the deployed artifact.
    * @see RestTest#getRestEndpointUrl(String)
    */
   public final String getRestEndpointUrl()
   {
      return getRestEndpointUrl("/");
   }

   /**
    * Gets a valid Authorized REST environment.
    *
    * @return A Resource Request execution environment with valid test credentials.
    */
   public static final ResourceRequestEnvironment getAuthorizedEnvironment()
   {
      return ENV_AUTHORIZED;
   }

   /**
    * Creates and returns a new instance of a proxy factory for the given credentials.
    * This method aids with the testing of Rest API classes.
    *
    * @param username The username that the proxy factory will authenticate with.
    * @param apiKey The apiKey for the user name.
    * @return A new instance of a proxy factory to create Rest API resources.
    */
   public final ZanataProxyFactory createClientProxyFactory( String username, String apiKey )
   {
      try
      {
         return new ZanataProxyFactory(new URI(getRestEndpointUrl()),
                                       username,
                                       apiKey,
                                       null,
                                       new VersionInfo("Test", "Test"),
                                       false)
         {
            @Override
            protected String getUrlPrefix()
            {
               return ""; // No prefix for tests
            }
         };
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   protected <T> T createProxy( ZanataProxyFactory clientFactory, Class<T> clientClass, String baseUri )
   {
      try
      {
         return clientFactory.createProxy(clientClass, new URI( getRestEndpointUrl(baseUri) ));
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

}
