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

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
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

    static {
        // Tell DeltaSpike to give more warning messages
        ProjectStageProducer.setProjectStage(ProjectStage.IntegrationTest);
    }

    public static void main(String[] args) {
        System.out.println("resolving dependencies:");
        List<File> depList = Arrays.asList(runtimeAndTestDependenciesFromPom());
        Collections.sort(depList);
        System.out.println(depList);
        System.out.println("dependency count: " + depList.size());
    }

    private static File[] runtimeAndTestDependenciesFromPom() {
        return Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies()
                .resolve()
                .using(
                // JavaMelody's ServletFilter/Listener interfere with test
                // deployments.
                // google-collections gets pulled in by arquillian and
                // conflict with guava.
                new RejectDependenciesStrategy(false,
                        "net.bull.javamelody:javamelody-core",
                        "org.zanata:zanata-model",
                        "com.google.collections:google-collections"))
                .asFile();
    }

    @Deployment(name = "zanata.war")
    public static Archive<?> createDeployment() {
        WebArchive archive =
                ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war");
        archive.addAsLibraries(runtimeAndTestDependenciesFromPom());

        // Local packages
        Filter<ArchivePath> archivePathFilter = object -> {
            // Avoid the model package (for some reason it's being included
            // as a class file)
            return /*!object.get().startsWith("/org/zanata/model/") &&
                    !object.get().startsWith("/org/zanata/util/RequestContextValueStore") &&*/
                    !object.get().startsWith("/org/zanata/seam/AutowireContexts") &&
                    !object.get().startsWith("/org/zanata/seam/AutowireInstance") &&
                    !object.get().startsWith("/org/zanata/seam/AutowireTransaction") &&
                    !object.get().startsWith("/org/zanata/seam/FieldComponentAccessor") &&
                    !object.get().startsWith("/org/zanata/seam/MethodComponentAccessor") &&
                    !object.get().startsWith("/org/zanata/seam/SeamAutowire") &&
                    !object.get().startsWith("/org/zanata/seam/test") &&
                    !object.get().startsWith("/org/zanata/webtrans/client") &&
                    !object.get().matches(".+Test.*") &&
                    !object.get().matches(".+ITCase.*");
        };
        archive.addPackages(true, archivePathFilter, "org.zanata");

        // Resources (descriptors, etc)
        archive.addAsResource("pluralforms.properties");
        archive.addAsResource(new ClassLoaderAsset("META-INF/orm.xml"),
                "META-INF/orm.xml");
        archive.addAsResource(
                new ClassLoaderAsset("arquillian/persistence.xml"),
                "META-INF/persistence.xml");
        archive.addAsResource("import.sql");
        archive.addAsResource("messages.properties");
        archive.addAsWebInfResource(
                new File("src/main/webapp-jboss/WEB-INF/jboss-web.xml"));
        archive.addAsWebInfResource(new File("src/main/webapp-jboss/WEB-INF/beans.xml"));
        archive.addAsWebInfResource(new File(
                "src/main/webapp-jboss/WEB-INF/jboss-deployment-structure.xml"));
        archive.setWebXML("arquillian/test-web.xml");

        addRemoteHelpers(archive);

        // Export (to actually see what is being deployed)
//         archive.as(ZipExporter.class).exportTo(new
//         File("/home/pahuang/temp/archive.war"), true);

        return archive;
    }

    private static void addRemoteHelpers(WebArchive archive) {
        archive.addPackages(true, "org.zanata.rest.helper");
        archive.addPackages(true, "org.zanata.arquillian");
        archive.addAsResource("org/zanata/test/model/");
    }
}
