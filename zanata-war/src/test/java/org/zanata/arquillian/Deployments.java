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
package org.zanata.arquillian;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.RejectDependenciesStrategy;

/**
 * Contains Suite-wide deployments to avoid having to deploy the same package
 * for every test class.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class Deployments {
    public static final String DEPLOYMENT_NAME = "zanata-tests";

    public static void main(String[] args) {
        System.out.println("resolving dependencies:");
        List<File> depList = Arrays.asList(runtimeDependenciesFromPom());
        Collections.sort(depList);
        System.out.println(depList);
        System.out.println("dependency count: " + depList.size());
    }

    private static File[] runtimeDependenciesFromPom() {
        return Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .using(
                // JavaMelody's ServletFilter/Listener interfere with test
                // deployments
                new RejectDependenciesStrategy(false,
                        "net.bull.javamelody:javamelody-core")).asFile();
    }

    @Deployment(name = "zanata.war")
    public static Archive<?> createDeployment() {
        WebArchive archive =
                ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war");
        archive.addAsLibraries(runtimeDependenciesFromPom());

        // Test dependencies
        archive.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.hibernate:hibernate-testing")
                .withoutTransitivity().asFile());
        archive.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.dbunit:dbunit").withoutTransitivity().asFile());

        // Local packages
        archive.addPackages(true, new Filter<ArchivePath>() {
            @Override
            public boolean include(ArchivePath object) {
                // Avoid the model package (for some reason it's being included
                // as a class file)
                return !object.get().startsWith("/org/zanata/model/") &&
                // and the ui package (not needed)
                        !object.get().startsWith("/org/zanata/ui");
            }
        }, "org.zanata");

        // Resources (descriptors, etc)
        archive.addAsResource(EmptyAsset.INSTANCE, "seam.properties");
        archive.addAsResource("pluralforms.properties");
        archive.addAsResource(new ClassLoaderAsset("META-INF/orm.xml"),
                "META-INF/orm.xml");
        archive.addAsResource(
                new FileAsset(
                        new File(
                                "src/main/webapp-jboss/WEB-INF/classes/META-INF/components.xml")),
                "META-INF/components.xml");
        archive.addAsResource(
                new ClassLoaderAsset("arquillian/persistence.xml"),
                "META-INF/persistence.xml");
        archive.addAsResource(new ClassLoaderAsset(
                "arquillian/components.properties"), "components.properties");
        archive.addAsResource("import.sql");
        archive.addAsResource("security.drl");
        archive.addAsWebInfResource(new File(
                "src/main/webapp-jboss/WEB-INF/jboss-deployment-structure.xml"));
        archive.addAsWebInfResource(new ClassLoaderAsset(
                "arquillian/zanata.properties"), "classes/zanata.properties");
        archive.setWebXML("arquillian/test-web.xml");

        addRemoteHelpers(archive);

        // Export (to actually see what is being deployed)
        // archive.as(ZipExporter.class).exportTo(new
        // File("/home/camunoz/temp/archive.war"), true);

        return archive;
    }

    private static void addRemoteHelpers(WebArchive archive) {
        archive.addPackages(true, "org.zanata.rest.helper");
        archive.addPackages(true, "org.zanata.arquillian");
        archive.addAsResource("org/zanata/test/model/");
    }
}
