package org.zanata.rest.service.raw

import org.jboss.shrinkwrap.descriptor.api.Descriptors
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.JBossDeploymentStructureDescriptor
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.slf4j.LoggerFactory
import org.zanata.H2DocumentHistoryTrigger
import org.zanata.database.WrappedDatasourceConnectionProvider
import org.zanata.i18n.MessagesFactory
import org.zanata.jpa.EntityManagerFactoryProducer
import org.zanata.jpa.EntityManagerProducer
import org.zanata.rest.JaxRSApplication
import org.zanata.rest.ZanataRestSecurityInterceptor
import org.zanata.rest.helper.RemoteTestSignalerImpl
import org.zanata.security.SmartEntitySecurityListener
import org.zanata.servlet.ContextPathProducer
import org.zanata.servlet.SessionIdProducer
import org.zanata.ui.faces.FacesContextProducer
import org.zanata.util.DeltaSpikeWindowIdParam
import org.zanata.util.HashUtil
import org.zanata.util.SynchronizationInterceptor
import org.zanata.util.WithRequestScopeInterceptor
import java.io.File

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

object ArquillianRest {
    private val log = LoggerFactory.getLogger(ArquillianRest::class.java)

    @JvmStatic
    fun jarDependenciesForRest(): List<String> {
        return listOf("com.google.guava:guava",
                "com.google.gwt:gwt-servlet",
                "com.ibm.icu:icu4j",
                "io.javaslang:javaslang",
                "net.sf.okapi.steps:okapi-step-tokenization",
                "net.sourceforge.openutils:openutils-log4j",
                "org.apache.commons:commons-lang3",
                "org.apache.deltaspike.cdictrl:deltaspike-cdictrl-api",
                "org.apache.deltaspike.cdictrl:deltaspike-cdictrl-weld",
                "org.apache.deltaspike.modules:deltaspike-security-module-api",
                "org.apache.deltaspike.modules:deltaspike-security-module-impl",
                "org.apache.deltaspike.modules:deltaspike-servlet-module-impl",
                "org.apache.oltu.oauth2:org.apache.oltu.oauth2.authzserver",
                "org.assertj:assertj-core",
                "org.codehaus.jackson:jackson-mapper-asl",
                "org.dbunit:dbunit",
//                "org.hibernate:hibernate-core",
                "org.reflections:reflections",
                "org.jetbrains.kotlin:kotlin-stdlib",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk7",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    @JvmOverloads
    @JvmStatic
    fun libsForRest(moreDependencies: List<String> = emptyList()): Array<File> {
        val libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve(jarDependenciesForRest() + moreDependencies)
                .withTransitivity()
                .asFile()
        if (log.isInfoEnabled) {
            log.info("Found {} libs: {}", libs.size, listOf<File>(*libs))
        }
        return libs
    }

    @JvmStatic
    fun classesWithDependenciesForRest(): List<Class<*>> {
        return listOf<Class<out Any>>(
                JaxRSApplication::class.java,
                WithRequestScopeInterceptor::class.java,
                SynchronizationInterceptor::class.java,
                WrappedDatasourceConnectionProvider::class.java,
                SmartEntitySecurityListener::class.java,
                EntityManagerProducer::class.java,
                EntityManagerFactoryProducer::class.java,
                SessionIdProducer::class.java,
                ContextPathProducer::class.java,
                DeltaSpikeWindowIdParam::class.java,
                FacesContextProducer::class.java,
                MessagesFactory::class.java,
                H2DocumentHistoryTrigger::class.java,
                HashUtil::class.java,
                RemoteTestSignalerImpl::class.java,
                ZanataRestSecurityInterceptor::class.java,
                ServerPathAlt::class.java
        )
    }

    /**
     * Generates beans.xml
     */
    @JvmStatic
    fun beansXmlForRest(vararg alternatives: Class<*>): BeansDescriptor {
        val altNames = alternatives.map { it.name }.toList().toTypedArray<String>()
        // We should be able to use this if we need to make modifications to the existing beans.xml:
//        val beansXml = Descriptors.importAs(BeansDescriptor.class.java)
//                .fromStream(ArquillianRest.class.getResourceAsStream("/META-INF/beans.xml"))
//                .getOrCreateAlternatives()
//                .clazz(*altNames)
//                .up();
        val beansXml = Descriptors.create(BeansDescriptor::class.java)
                .orCreateInterceptors.clazz(
                "org.zanata.util.WithRequestScopeInterceptor",
                "org.zanata.util.SynchronizationInterceptor",
                "org.apache.deltaspike.security.impl.extension.SecurityInterceptor")
                .up()
                .orCreateAlternatives.clazz(*altNames)
                .up()
        return beansXml
    }

    /**
     * Generates jboss-deployment-structure.xml
     */
    @JvmStatic
    fun jbossDeploymentStructureForRest(): JBossDeploymentStructureDescriptor {
        // We need to exclude jackson2 and include jackson1, plus we need the H2 database.
        // NB if it weren't for exclusions, we could just use:
        // war.addAsManifestResource(new StringAsset("Dependencies: com.h2database.h2 optional"), "MANIFEST.MF")
        return Descriptors.create(JBossDeploymentStructureDescriptor::class.java)
                .orCreateDeployment
                .orCreateExclusions
                .createModule().name("org.jboss.resteasy.resteasy-jackson2-provider").up()
                .up()
                .orCreateDependencies
                .createModule().name("com.h2database.h2").optional(true).up()
                .createModule().name("org.jboss.resteasy.resteasy-jackson-provider").services("import").up()
                .up().up()
    }

}
