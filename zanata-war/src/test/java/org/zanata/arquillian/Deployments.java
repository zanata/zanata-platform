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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Joiner;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/**
 * Contains Suite-wide deployments to avoid having to deploy the same package
 * for every test class.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class Deployments {
    private static final Logger log =
            LoggerFactory.getLogger(Deployments.class);
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
            return !object.get().startsWith("/org/zanata/model/") &&
                    !object.get().startsWith("/org/zanata/util/RequestContextValueStore") &&
                    notUnusedGwtClientCode(object) &&
                    notUnitTest(object);
        };
        archive.addPackages(true, archivePathFilter, "org.zanata");

        // Resources (descriptors, etc)
        archive.addAsResource("pluralforms.properties");
        archive.addAsResource("META-INF/apache-deltaspike.properties");
        // JaxRSClassIndexProcessor generated class index
        File jaxRsPathIndex = concatenatePathClassIndice();
        archive.addAsResource(jaxRsPathIndex,
                "META-INF/annotations/javax.ws.rs.Path");
        archive.addAsResource("META-INF/annotations/javax.ws.rs.ext.Provider");

        archive.addAsResource(new ClassLoaderAsset("META-INF/orm.xml"),
                "META-INF/orm.xml");
        archive.addAsResource(
                new ClassLoaderAsset("arquillian/persistence.xml"),
                "META-INF/persistence.xml");
        archive.addAsResource("import.sql");
        archive.addAsResource("messages.properties");
        archive.addAsWebInfResource(
                new File("src/main/webapp-jboss/WEB-INF/jboss-web.xml"));
        archive.addAsWebInfResource(
                new File("src/main/webapp-jboss/WEB-INF/beans.xml"));
        archive.addAsWebInfResource(new File(
                "src/main/webapp-jboss/WEB-INF/jboss-deployment-structure.xml"));
        archive.setWebXML("arquillian/test-web.xml");

        addRemoteHelpers(archive);

        // uncomment to see what will be in the war
//        ArrayList<ArchivePath> paths =
//                new ArrayList<>(archive.getContent().keySet());
//        Collections.sort(paths);
//        String contents = "  " + Joiner.on("\n  ").join(paths);
//        System.out.println(contents);

        // Export (to actually see what is being deployed)
//        archive.as(ZipExporter.class).exportTo(
//                 new File("/tmp/zanata-arquillian.war"), true);
        return archive;
    }

    /**
     * There are two copy of generated class index file from
     * JaxRSClassIndexProcessor, one for production under target/classes and one
     * for test under target/test-classes. Here we concatenate the content of
     * the two and generate a new one for arquillian archive to use.
     *
     * @return class index file with concatenated content
     */
    private static File concatenatePathClassIndice() {
        try {
            Enumeration<URL> resources =
                    Thread.currentThread().getContextClassLoader()
                            .getResources(
                                    "META-INF/annotations/javax.ws.rs.Path");
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            forEachRemaining(resources, url -> {
                File file = new File(url.getPath());
                if (file.exists() && file.canRead()) {
                    try {
                        Files.lines(Paths.get(url.toURI()),
                                StandardCharsets.UTF_8)
                                .forEach(builder::add);
                    } catch (URISyntaxException | IOException e) {
                        log.error("error handling file: {}", file, e);
                    }
                }
            });
            List<String> pathClasses = builder.build();
            log.debug("all javax.ws.rs.Path classes: {}", pathClasses);
            URL metaInf = Thread.currentThread().getContextClassLoader()
                    .getResource("META-INF/annotations/javax.ws.rs.Path");
            if (metaInf != null) {
                File allPathClassesIndex = new File(metaInf.getPath() + "-concatenated");
                Files.write(
                        allPathClassesIndex.toPath(), pathClasses, StandardCharsets.UTF_8);
                return allPathClassesIndex;
            }
            throw new IllegalStateException("can not find annotation index file");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private static boolean notUnitTest(ArchivePath object) {
        String context = object.get();
        // TODO find a better way to exclude all unit tests being included and registered as CDI bean
        return context.contains("ArquillianTest")
                || context.contains("RestTest")
                || context.contains("TestAsyncBean")
                || context.contains("ResourceTestObjectFactory")
                // unit test classes
                || !(context.matches(".+Test(s)?\\.class$") ||
                // inner classes of unit test classes
                context.matches(".+Test(s)?\\$.*\\.class$"));
    }

    private static boolean notUnusedGwtClientCode(ArchivePath object) {
        String context = object.get();
        // we need this class in ValidationFactoryProvider
        return context.startsWith(
                "/org/zanata/webtrans/client/resources/ValidationMessages") ||
                !context.startsWith("/org/zanata/webtrans/client");
    }

    private static <T> void forEachRemaining(Enumeration<T> enumeration,
            Consumer<? super T> consumer) {
        while (enumeration.hasMoreElements()) {
            consumer.accept(enumeration.nextElement());
        }
    }

    private static void addRemoteHelpers(WebArchive archive) {
        archive.addPackages(true, "org.zanata.rest.helper");
        archive.addPackages(true, "org.zanata.arquillian");
        archive.addAsResource("org/zanata/test/model/");
    }
}
