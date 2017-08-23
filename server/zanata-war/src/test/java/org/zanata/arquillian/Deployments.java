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
import java.util.ArrayList;
import java.util.Collections;

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

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME;
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST;

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

    public static void main(String[] args) throws Exception {
        Archive<?> archive = Deployments.createDeployment();
        // see what will be in the war
        printArchiveContents(archive);

        // Uncomment this if you want to export a .war (to actually see
        // what is being deployed):
//        archive.as(ZipExporter.class).exportTo(
//                new File("/tmp/zanata-arquillian.war"), true);

        // OR uncomment this if you want an exploded war directory:
//        File exploded = new File("/tmp/zanata-arquillian-exploded.war");
//        org.apache.commons.io.FileUtils.deleteDirectory(exploded);
//        archive.as(org.jboss.shrinkwrap.api.exporter.ExplodedExporter.class)
//                .exportExplodedInto(exploded);
    }

    @SuppressWarnings("unchecked")
    private static void printArchiveContents(Archive archive) {
        // We could just use archive.toString(verbose=true), but it's
        // nicer to have sorting.
        ArrayList<ArchivePath> paths =
                new ArrayList<>(archive.getContent().keySet());
        Collections.sort(paths);
        log.info("Deployment contents:");
        paths.forEach(it -> log.info("  " + it.get()));
    }

    static File[] runtimeAndTestDependenciesFromPom() {
        return Maven.configureResolver()
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
                                // We shouldn't need apicompat interfaces in
                                // the server, but we will also need to
                                // exclude the tests which refer to them.
//                                "org.zanata:zanata-common-api:jar:compat:?"
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
        archive.delete("/WEB-INF/classes/arquillian");
        archive.delete("/WEB-INF/classes/arquillian.xml");
        // Remove log4j.xml to prevent JBoss from activating per-deployment
        // logging (which breaks stdout and org.zanata logging).
        archive.delete("/WEB-INF/classes/log4j.xml");
        // Note: see the main method if you want to see or extract the contents of the deployment
        return archive;
    }

    private static boolean notUnitTest(ArchivePath object) {
        String context = object.get();
        // TODO find a better way to exclude all unit tests being included and registered as CDI bean
        return context.contains("ArquillianTest")
                || context.contains("RestTest")
                || context.contains("TestAsyncBean")
//                || context.contains("ResourceTestObjectFactory")
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
}
