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
import java.util.List;
import javax.ws.rs.core.HttpHeaders;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.api.maven.filter.MavenResolutionFilter;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.MavenResolutionStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class GreeterTest
{

   private static final String DEPLOYMENT_NAME = "zanata-tests";

   @ArquillianResource
   URL deploymentUrl;

   @Deployment(name = "zanata.war", testable = false)
   public static Archive<?> createDeployment()
   {

      WebArchive archive =  ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war");
      archive.addAsLibraries(Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importRuntimeDependencies()
            .asFile());
      // Test dependencies
      archive.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("org.hibernate:hibernate-testing:4.1.6.Final").withoutTransitivity().asFile());
      // Missing dependencies
      // This one resolves to the gwteventservice file inside the maven resolver. Could be a bug with the alpha version
      archive.addAsLibraries(new File("/home/camunoz/.m2/repository/de/novanic/gwteventservice/eventservice/1.2.1/eventservice-1.2.1.jar"));
      // This doesn't work either...
      //archive.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("de.novanic.gwteventservice:eventservice").withoutTransitivity().asFile());

      // Local packages
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

      // Resources (descriptors, etc)
      archive.addAsResource(EmptyAsset.INSTANCE, "seam.properties");
      archive.addAsWebInfResource(new File("src/test/resources/arquillian/jboss-deployment-structure.xml"));
      archive.addAsResource(new FileAsset(new File("src/main/resources/META-INF/orm.xml")), "META-INF/orm.xml");
      archive.addAsResource(new FileAsset(new File("src/test/jboss-embedded-bootstrap/META-INF/persistence.xml")), "META-INF/persistence.xml");
      archive.addAsResource(new FileAsset(new File("src/main/webapp-jboss/WEB-INF/classes/META-INF/components.xml")), "META-INF/components.xml");
      archive.addAsResource(new FileAsset(new File("target/zanata-seamtests/WEB-INF/classes/components.properties")), "components.properties");
      archive.addAsResource("security.drl");
      archive.addAsWebInfResource(new File("src/test/resources/arquillian/zanata.properties"),
            "classes/zanata.properties");
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
      new ResourceRequest(deploymentUrl.toString() + "/" + DEPLOYMENT_NAME + "/seam/resource/restv1/version", "GET") {
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

}
