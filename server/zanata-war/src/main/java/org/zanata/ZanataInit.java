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
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.management.InstanceAlreadyExistsException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.jmx.StatisticsService;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.security.auth.login.XMLLoginConfigMBean;
import org.zanata.exception.ZanataInitializationException;
import org.zanata.security.AuthenticationType;
import org.zanata.util.DBUnitImporter;

/**
 * Doesn't do much useful stuff except printing a log message and firing the
 * "Zanata.startup" event.
 * 
 * @author Christian Bauer
 */
@Name("zanataInit")
@Scope(ScopeType.APPLICATION)
public class ZanataInit
{

   public static final String EVENT_Zanata_Startup = "Zanata.startup";
   public static final String UNKNOWN_VERSION = "UNKNOWN";

   @Logger
   static Log log;

   private boolean debug;
   private boolean hibernateStatistics = false;
   private int authenticatedSessionTimeoutMinutes = 0;
   private String version;
   private String buildTimestamp;
   private boolean enableCopyTrans = true;
   private String[] additionalSecurityDomains;


   public boolean getEnableCopyTrans()
   {
      return enableCopyTrans;
   }

   public void setEnableCopyTrans(boolean enableCopyTrans)
   {
      this.enableCopyTrans = enableCopyTrans;
   }

   @In(required = false)
   DBUnitImporter dbunitImporter;
   
   @In
   private ApplicationConfiguration applicationConfiguration;

   private ObjectName hibernateMBeanName;

   @Observer("org.jboss.seam.postInitialization")
   public void initZanata() throws Exception
   {
      log.info(">>>>>>>>>>>> Starting Zanata...");

      ServletContext servletContext = ServletLifecycle.getCurrentServletContext();
      String appServerHome = servletContext.getRealPath("/");

      File manifestFile = new File(appServerHome, "META-INF/MANIFEST.MF");

      if (manifestFile.canRead())
      {
         Manifest mf = new Manifest();
         final FileInputStream fis = new FileInputStream(manifestFile);
         try
         {
            mf.read(fis);
         }
         finally
         {
            fis.close();
         }

         Attributes atts = mf.getMainAttributes();

         version = atts.getValue("Implementation-Version");
         buildTimestamp = atts.getValue("Implementation-Build");
         if (version == null)
         {
            version = UNKNOWN_VERSION;
         }
         if (buildTimestamp == null)
         {
            buildTimestamp = UNKNOWN_VERSION;
         }
         log.info("Server version: {0}", version);
         log.info("Server build: {0}", buildTimestamp);
      }
      if (isDebug())
      {
         log.info("debug: enabled");
      }
      boolean authlogged = false;
      
      if( applicationConfiguration.getAuthenticationType() == null )
      {
         throw new ZanataInitializationException("Authentication type not configured in the Database.");
      }
      if ( applicationConfiguration.isInternalAuth() )
      {
         log.info("Internal authentication: enabled");
         authlogged = true;
      }
      if ( applicationConfiguration.isFedoraOpenIdAuth() )
      {
         log.info("Fedora OpenID authentication: enabled");
         authlogged = true;
      }
      if ( applicationConfiguration.isKerberosAuth() )
      {
         log.info("SPNEGO/Kerberos authentication: enabled");
         authlogged = true;
      }
      if (!authlogged)
      {
         log.info("Using JAAS authentication");
      }
      log.info("Enable copyTrans: {0}", getEnableCopyTrans());

      // if (dbunitImporter != null) {
      // log.info("Importing development test data");
      // dbunitImporter.importDatasets();
      // }

      if (hibernateStatistics)
      {
         log.info("registering Hibernate statistics MBean");
         try
         {
            hibernateMBeanName = new ObjectName("Hibernate:type=statistics,application=zanata");
            StatisticsService mBean = new StatisticsService();
            mBean.setSessionFactoryJNDIName("SessionFactories/zanataSF");
            ManagementFactory.getPlatformMBeanServer().registerMBean(mBean, hibernateMBeanName);
         }
         catch (InstanceAlreadyExistsException e)
         {
            log.info("Hibernate statistics MBean is already started");
         }
         catch (Exception e)
         {
            log.error("Hibernate statistics MBean failed to start", e);
         }
      }
      
      this.initSecurityConfig();

      Events.instance().raiseEvent(EVENT_Zanata_Startup);

      log.info("Started Zanata...");
   }

   @Destroy
   public void shutdown() throws Exception
   {
      log.info("<<<<<<<<<<<<< Stopping Zanata...");

      if (hibernateStatistics)
      {
         log.info("unregistering Hibernate statistics MBean");
         try
         {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(hibernateMBeanName);
         }
         catch (Exception e)
         {
            log.error("Failed to unregister Hibernate statistics MBean", e);
         }
      }
      
      try
      {
         this.destroySecurityConfig();
      }
      catch (Exception e)
      {
         log.error("Failed to remove Zanata security config. A server restart might be required.", e);
      }

      log.info("Stopped Zanata...");
   }
   
   /**
    * Initializes the Zanata security configuration.
    */
   private void initSecurityConfig() throws Exception {
      
      log.info("Initializing Zanata Security...");
      
      // Look for the authConf as resource
      final String loginConfigUrl = applicationConfiguration.getLoginConfigUrl();
      
      if( loginConfigUrl != null )
      {
         URL loginConfig = new URL( loginConfigUrl );
      
         if( loginConfig != null )
         {
            log.info("Using security configuration at: " + loginConfig.getFile());
            
            XMLLoginConfigMBean config = (XMLLoginConfigMBean) MBeanProxy.get(XMLLoginConfigMBean.class,
                  new ObjectName("jboss.security:service=XMLLoginConfig"), 
                  MBeanServerLocator.locateJBoss());
            this.additionalSecurityDomains = config.loadConfig(loginConfig);
            
            log.info("Loaded the following security domains: " + ArrayUtils.toString(this.additionalSecurityDomains));
         }
      }
   }
   
   /**
    * Destroys the Zanata security configuration
    */
   private void destroySecurityConfig() throws Exception
   {
      log.info("Stopping Zanata Security...");
      
      if( this.additionalSecurityDomains != null )
      {
         log.info("Removing security domains: " +  ArrayUtils.toString(this.additionalSecurityDomains));
         
         XMLLoginConfigMBean config = (XMLLoginConfigMBean) MBeanProxy.get(XMLLoginConfigMBean.class,
               new ObjectName("jboss.security:service=XMLLoginConfig"), 
               MBeanServerLocator.locateJBoss());
         config.removeConfigs( this.additionalSecurityDomains );
      }
   }

   public String getBuildTimestamp()
   {
      return buildTimestamp;
   }

   public String getVersion()
   {
      return version;
   }

   public boolean isDebug()
   {
      return debug;
   }

   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   public boolean isHibernateStatistics()
   {
      return hibernateStatistics;
   }

   public void setHibernateStatistics(boolean hibernateStatistics)
   {
      this.hibernateStatistics = hibernateStatistics;
   }

   public int getAuthenticatedSessionTimeoutMinutes()
   {
      return authenticatedSessionTimeoutMinutes;
   }

   public void setAuthenticatedSessionTimeoutMinutes(int authenticatedSessionTimeoutMinutes)
   {
      this.authenticatedSessionTimeoutMinutes = authenticatedSessionTimeoutMinutes;
   }

   /** Utility to debug JBoss JNDI problems */
   public static String listJNDITree(String namespace)
   {
      StringBuffer buffer = new StringBuffer(4096);
      try
      {
         Properties props = new Properties();
         Context context = new InitialContext(props); // From jndi.properties
         if (namespace != null)
            context = (Context) context.lookup(namespace);
         buffer.append("Namespace: " + namespace + "\n");
         buffer.append("#####################################\n");
         list(context, " ", buffer, true);
         buffer.append("#####################################\n");
      }
      catch (NamingException e)
      {
         buffer.append("Failed to get InitialContext, " + e.toString(true));
      }
      return buffer.toString();
   }

   private static void list(Context ctx, String indent, StringBuffer buffer, boolean verbose)
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
         NamingEnumeration<NameClassPair> ne = ctx.list("");
         while (ne.hasMore())
         {
            NameClassPair pair = ne.next();

            String name = pair.getName();
            String className = pair.getClassName();
            boolean recursive = false;
            boolean isLinkRef = false;
            boolean isProxy = false;
            Class<?> c = null;
            try
            {
               c = loader.loadClass(className);

               if (Context.class.isAssignableFrom(c))
                  recursive = true;
               if (LinkRef.class.isAssignableFrom(c))
                  isLinkRef = true;

               isProxy = Proxy.isProxyClass(c);
            }
            catch (ClassNotFoundException cnfe)
            {
               // If this is a $Proxy* class its a proxy
               if (className.startsWith("$Proxy"))
               {
                  isProxy = true;
                  // We have to get the class from the binding
                  try
                  {
                     Object p = ctx.lookup(name);
                     c = p.getClass();
                  }
                  catch (NamingException e)
                  {
                     Throwable t = e.getRootCause();
                     if (t instanceof ClassNotFoundException)
                     {
                        // Get the class name from the exception msg
                        String msg = t.getMessage();
                        if (msg != null)
                        {
                           // Reset the class name to the CNFE class
                           className = msg;
                        }
                     }
                  }
               }
            }

            buffer.append(indent + " +- " + name);

            // Display reference targets
            if (isLinkRef)
            {
               // Get the
               try
               {
                  Object obj = ctx.lookupLink(name);

                  LinkRef link = (LinkRef) obj;
                  buffer.append("[link -> ");
                  buffer.append(link.getLinkName());
                  buffer.append(']');
               }
               catch (Throwable t)
               {
                  buffer.append("invalid]");
               }
            }

            // Display proxy interfaces
            if (isProxy)
            {
               buffer.append(" (proxy: " + pair.getClassName());
               if (c != null)
               {
                  Class<?>[] ifaces = c.getInterfaces();
                  buffer.append(" implements ");
                  for (int i = 0; i < ifaces.length; i++)
                  {
                     buffer.append(ifaces[i]);
                     buffer.append(',');
                  }
                  buffer.setCharAt(buffer.length() - 1, ')');
               }
               else
               {
                  buffer.append(" implements " + className + ")");
               }
            }
            else if (verbose)
            {
               buffer.append(" (class: " + pair.getClassName() + ")");
            }

            buffer.append('\n');
            if (recursive)
            {
               try
               {
                  Object value = ctx.lookup(name);
                  if (value instanceof Context)
                  {
                     Context subctx = (Context) value;
                     list(subctx, indent + " |  ", buffer, verbose);
                  }
                  else
                  {
                     buffer.append(indent + " |   NonContext: " + value);
                     buffer.append('\n');
                  }
               }
               catch (Throwable t)
               {
                  buffer.append("Failed to lookup: " + name + ", errmsg=" + t.getMessage());
                  buffer.append('\n');
               }
            }
         }
         ne.close();
      }
      catch (NamingException ne)
      {
         buffer.append("error while listing context " + ctx.toString() + ": " + ne.toString(true));
      }
   }

}
