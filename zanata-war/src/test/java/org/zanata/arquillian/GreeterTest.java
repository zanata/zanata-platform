/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.arquillian;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.JUnitSeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.api.maven.filter.MavenResolutionFilter;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.MavenResolutionStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.rest.MediaTypes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class GreeterTest //extends JUnitSeamTest
{

   /*@Deployment(order = 1, name = "mysql-driver.jar")
   public static Archive<?> createDatabaseDriverDeployment()
   {
      MavenDependencyResolver mavenResolver = DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom("pom.xml");
      // The mysql driver artifact does not have any dependencies. This method returns the artifcat plus all its
      // transitive dependencies
      return mavenResolver.artifact("mysql:mysql-connector-java:5.1.23").resolveAs(JavaArchive.class).iterator().next();
   }*/

   /*@Deployment(order = 1, name = "zanata-test-ds.xml")
   public static Descriptor createDataSourceDeployment()
   {
      return Descriptors.create(DataSourceDescrip)
      return new JBoss5DataSourceDescriptor(
            "zanata-test-ds.xml",
            "zanataTestDatasource",
            "jdbc:h2:mem:zanata;DB_CLOSE_DELAY=-1",
            "org.h2.Driver",
            "sa",
            null);
   }*/

   @Deployment(order = 2, name = "zanata.war", testable = false)
   public static Archive<?> createMassiveDeployment()
   {
      MavenResolverSystem mavenResolver = Maven.resolver();

      // Assume there is already a target directory called zanata-seamtests
      WebArchive archive =
            ShrinkWrap.create(WebArchive.class, "zanata.war");

      archive.as(ExplodedImporter.class).importDirectory("target/zanata-seamtests");

      // Add the zanata.properties file directly to the archive
      archive.addAsWebInfResource(new File("src/test/resources/arquillian/zanata.properties"),
            "classes/zanata.properties");
      // Replace the jboss-deployment-structure with the testverison
      archive.delete("WEB-INF/jboss-deployment-structure.xml");
      archive.addAsWebInfResource(new File("src/test/resources/arquillian/jboss-deployment-structure.xml"));
      //Replace web.xml
      archive.delete("WEB-INF/web.xml");
      archive.addAsWebInfResource("arquillian/test-web.xml", "web.xml");
      //Replace persistence.xml
      archive.delete("WEB-INF/classes/META-INF/persistence.xml");
      archive.addAsResource(new FileAsset(new File("src/test/jboss-embedded-bootstrap/META-INF/persistence.xml")), "META-INF/persistence.xml");
      // Add any test scoped dependencies that might not be in the original archive
      archive.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("org.hibernate:hibernate-testing:4.1.6.Final").withoutTransitivity().asFile());
      //archive.addPackages(true, "org.hamcrest");

      // Remove xhtml files
      //archive.delete("account");

      // Remove libraries
      archive.delete("WEB-INF/lib/javamelody-core-1.41.0.jar");

      // Export (to actually see what is being deployed)
      //archive.as(ZipExporter.class).exportTo( new File("/opt/jboss-eap-6.0-standalone/standalone/deployments/archive.war"), true );
      archive.as(ZipExporter.class).exportTo(new File("/home/camunoz/temp/archive-massive.war"), true);

      return archive;
   }

   /*@Deployment(order = 2, name = "zanata.war")
   public static Archive<?> miniDeployment()
   {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
      archive.addClass(GreeterTest.class);
      return archive;
   }*/

   //@Deployment(order = 2, name = "zanata.war")
   public static Archive<?> createDeployment()
   {

      WebArchive archive =  ShrinkWrap.create(WebArchive.class, "zanata.war");
      //archive.as(ExplodedImporter.class).importDirectory("server/zanata-war/target/zanata-seamtests");
      archive.addAsLibraries(Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importRuntimeDependencies(new MavenResolutionStrategy()
            {
               @Override
               public MavenResolutionFilter[] getResolutionFilters()
               {
                  return new MavenResolutionFilter[]{
                        new MavenResolutionFilter()
                        {
                           @Override
                           public boolean accepts(MavenDependency dependency, List<MavenDependency> dependenciesForResolution)
                           {
                              /*if (dependency.getGroupId().equals("org.jboss.resteasy") ||
                                    dependency.getGroupId().equals("org.jboss.seam") ||
                                    dependency.getGroupId().equals("org.javassist") ||
                                    dependency.getGroupId().equals("org.apache.solr") ||
                                    dependency.getGroupId().equals("com.ibm.icu") ||
                                    dependency.getGroupId().equals("net.sourceforge.openutils") ||
                                    dependency.getGroupId().startsWith("org.hibernate") ||
                                    dependency.getGroupId().equals("org.apache.lucene") ||
                                    dependency.getGroupId().equals("org.zanata") )
                              {
                                 return true;
                              }*/
                              return true;
                           }
                        }
                  };
               }

               @Override
               public MavenResolutionFilter[] getPreResolutionFilters()
               {
                  return new MavenResolutionFilter[0];  //To change body of implemented methods use File | Settings | File Templates.
               }
            })
            .asFile());
      // Test dependencies
      archive.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("org.hibernate:hibernate-testing:4.1.6.Final").withoutTransitivity().asFile());
      // Missing dependencies (for some strange reason)
      archive.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("org.jboss.seam:jboss-seam:ejb:2.3.0.Final").withoutTransitivity().asFile());
      archive.addPackages(true, new Filter<ArchivePath>()
      {
         @Override
         public boolean include(ArchivePath object)
         {
            // Avoid the model package (for some reason it's being included as a class file)
            return !object.get().startsWith("/org/zanata/model/") &&
            // and the ui package (not needed)
                   !object.get().startsWith("/org/zanata/ui");
         }
      }, "org.zanata");
      archive.addAsResource(EmptyAsset.INSTANCE, "seam.properties");
      archive.addAsWebInfResource(new File("src/test/resources/arquillian/jboss-deployment-structure.xml"));
      archive.addAsResource(new FileAsset(new File("src/main/resources/META-INF/orm.xml")), "META-INF/orm.xml");
      archive.addAsResource(new FileAsset(new File("src/test/jboss-embedded-bootstrap/META-INF/persistence.xml")), "META-INF/persistence.xml");
      archive.addAsResource(new FileAsset(new File("src/main/webapp-jboss/WEB-INF/classes/META-INF/components.xml")), "META-INF/components.xml");
      archive.addAsResource(new FileAsset(new File("target/zanata-seamtests/WEB-INF/classes/components.properties")), "components.properties");
      archive.addAsWebInfResource("arquillian/test-web.xml", "web.xml");

      // Export (to actually see what is being deployed)
      archive.as(ZipExporter.class).exportTo(new File("/home/camunoz/temp/archive.war"), true);
      //archive.as(ZipExporter.class).exportTo(new File("/opt/jboss-eap-6.0-standalone/standalone/deployments/archive.war"), true);
      return archive;
   }

   @Test
   @RunAsClient
   public void testHelloWorld() throws Exception
   {
      /*new ResourceRequestEnvironment.ResourceRequest(new ResourceRequestEnvironment(this)
      {
         @Override
         public Map<String, Object> getDefaultHeaders()
         {
            return new HashMap<String, Object>();
         }
      }, ResourceRequestEnvironment.Method.GET, "/restv1/version")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200));
         }
      }.run();*/
      new MockResourceRequest(deploymentUrl.toString() + "/restv1/version", "GET") {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_VERSION_XML);
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertThat(response.getStatus(), is(200));
         }
      }.run();
   }

   public static void main( String ... args )
   {
      //createMassiveDeployment();
      createDeployment();
   }

   @ArquillianResource
   URL deploymentUrl;

   private abstract class MockResourceRequest {
      private String resourceUrl;
      private String method;

      protected MockResourceRequest(String resourceUrl, String method)
      {
         this.resourceUrl = resourceUrl;
         this.method = method;
      }

      protected abstract void prepareRequest(ClientRequest request);

      protected abstract void onResponse(ClientResponse response);

      public void run() throws Exception {
         ClientRequest request = new ClientRequest(resourceUrl);
         request.setHttpMethod(method);
         prepareRequest(request);
         ClientResponse response = request.execute();
         onResponse(response);
      }
   }

}
