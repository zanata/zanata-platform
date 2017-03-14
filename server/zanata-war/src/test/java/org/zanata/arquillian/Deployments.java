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
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.CombinedStrategy;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.RejectDependenciesStrategy;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.TransitiveStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.*;

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
        Archive<?> archive = Deployments.createDeployment();
        // see what will be in the war
        ArrayList<ArchivePath> paths =
                new ArrayList<>(archive.getContent().keySet());
        Collections.sort(paths);
        System.out.println("Deployment contents:");
        paths.forEach(it -> System.out.println("  " + it));
    }

    static File[] runtimeAndTestDependenciesFromPom() {
        return Maven.configureResolver()
                .workOffline()
                // we want to get zanata-platform dependencies from Maven
                // reactor classpath if possible, not from repositories
                .withClassPathResolution(true)
                .loadPomFromFile("pom.xml")
                .importDependencies(COMPILE, RUNTIME, TEST)
                .resolve()
                .using(new CombinedStrategy(
                        new RejectDependenciesStrategy(
                                false,
                                // JavaMelody's ServletFilter/Listener
                                // interferes with test deployments.
                                "net.bull.javamelody:javamelody-core"
                        ),
                        TransitiveStrategy.INSTANCE))
                .asFile();
    }

    @Deployment(name = "zanata.war")
    public static Archive<?> createDeployment() {
        WebArchive archive =
                ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war");
        // TODO add org.zanata packages on classpath first, exclude any libraries with colliding classes
        archive.addAsLibraries(runtimeAndTestDependenciesFromPom());

        // Local packages from zanata-war, minus GWT client and unit test classes.
        // TODO apply the individual filters with importDirectory()
        // instead of archive.merge() - paths may have different prefix.
        Filter<ArchivePath> archivePathFilter = object ->
                notUnusedGwtClientCode(object) &&
                notUnitTest(object);
        archive.merge(ShrinkWrap.create(GenericArchive.class)
                        .as(ExplodedImporter.class)
                        .importDirectory("target/classes")
                        // this includes RemoteTestSignalerImpl
                        // and RemoteAfter/RemoteBefore
                        .importDirectory("target/test-classes")
                        .as(GenericArchive.class),
                "/WEB-INF/classes", archivePathFilter);

        // JaxRSClassIndexProcessor generated class index
        File jaxRsPathIndex = concatenatePathClassIndice();
        archive.addAsResource(jaxRsPathIndex,
                "META-INF/annotations/javax.ws.rs.Path");
        archive.addAsResource(
                new ClassLoaderAsset("arquillian/persistence.xml"),
                "META-INF/persistence.xml");
        archive.addAsWebInfResource(
                new File("src/main/webapp-jboss/WEB-INF/jboss-web.xml"));
        archive.addAsWebInfResource(
                new File("src/main/webapp-jboss/WEB-INF/beans.xml"));
        archive.addAsWebInfResource(new File(
                "src/main/webapp-jboss/WEB-INF/jboss-deployment-structure.xml"));
        archive.setWebXML("arquillian/test-web.xml");

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
            if (!resources.hasMoreElements()) {
                throw new IllegalStateException(
                        "cannot find any annotation index files (javax.ws.rs.Path)");
            }
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            forEachRemaining(resources, url -> {
                File file = new File(url.getPath());
                if (file.exists() && file.canRead()) {
                    try {
                        Files.lines(Paths.get(url.toURI()), UTF_8)
                                .forEach(builder::add);
                    } catch (URISyntaxException | IOException e) {
                        log.error("error handling file: {}", file, e);
                    }
                }
            });
            List<String> pathClasses = builder.build();
            log.debug("all javax.ws.rs.Path classes: {}", pathClasses);
            File concatenatedIndex = File.createTempFile(
                    "javax.ws.rs.Path-concatenated", ".tmp");
            concatenatedIndex.deleteOnExit();
            Files.write(concatenatedIndex.toPath(), pathClasses, UTF_8);
            return concatenatedIndex;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                || !unitTestsAndUnitTestInnerClasses(context);
    }

    private static boolean unitTestsAndUnitTestInnerClasses(String context) {
        return context.matches(".+Test(s)?\\.class$") ||
        // inner classes of unit test classes
        context.matches(".+Test(s)?\\$.*\\.class$");
    }

    private static boolean notUnusedGwtClientCode(ArchivePath object) {
        String context = object.get();
        // we need these classes in ValidationFactoryProvider
        // the context appears to be /WEB-INF/classes/...
        return context.matches(
                ".*/org/zanata/webtrans/client/resources/ValidationMessages") ||
                context.matches(".*/org/zanata/webtrans/server/locale/.*") ||
                !context.contains("/org/zanata/webtrans/client") &&
                !context.contains("/org/zanata/webtrans/server");
    }

    private static <T> void forEachRemaining(Enumeration<T> enumeration,
            Consumer<? super T> consumer) {
        while (enumeration.hasMoreElements()) {
            consumer.accept(enumeration.nextElement());
        }
    }
}
