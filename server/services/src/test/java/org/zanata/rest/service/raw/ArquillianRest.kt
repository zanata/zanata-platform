package org.zanata.rest.service.raw

import org.jboss.shrinkwrap.descriptor.api.Descriptors
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.JBossDeploymentStructureDescriptor
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.slf4j.LoggerFactory
import org.wildfly.extras.creaper.commands.datasources.AddDataSource
import org.wildfly.extras.creaper.commands.logging.AddLogger
import org.wildfly.extras.creaper.commands.logging.ChangeConsoleLogHandler
import org.wildfly.extras.creaper.commands.logging.LogLevel
import org.wildfly.extras.creaper.commands.messaging.AddQueue
import org.wildfly.extras.creaper.commands.security.AddLoginModule
import org.wildfly.extras.creaper.commands.security.AddSecurityDomain
import org.wildfly.extras.creaper.core.ManagementClient
import org.wildfly.extras.creaper.core.offline.OfflineOptions
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
                "org.apache.deltaspike.cdictrl:deltaspike-cdictrl-api",
                "org.apache.deltaspike.cdictrl:deltaspike-cdictrl-weld",
                "org.apache.deltaspike.modules:deltaspike-security-module-api",
                "org.apache.deltaspike.modules:deltaspike-security-module-impl",
                "org.apache.deltaspike.modules:deltaspike-servlet-module-impl",
                "org.apache.oltu.oauth2:org.apache.oltu.oauth2.authzserver",
                "org.assertj:assertj-core",
                "org.codehaus.jackson:jackson-mapper-asl",
                "org.dbunit:dbunit",
                "org.reflections:reflections",
                "org.jetbrains.kotlin:kotlin-stdlib")
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
        return mutableListOf<Class<out Any>>(
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

    @JvmStatic
    @JvmSuppressWildcards
    fun beforeSetup() {
        log.info("beforeSetup")
        val jbossHome = System.getProperty("jboss.home") ?: throw RuntimeException(
                "System property jboss.home needs to be set")

        // see also this alternative: https://developer.jboss.org/wiki/AdvancedCLIScriptingWithGroovyRhinoJythonEtc

        val client = ManagementClient.offline(OfflineOptions.standalone()
                .rootDirectory(File(jbossHome))
                .configurationFile("standalone-full.xml")
                .build())

        client.apply(AddDataSource.Builder<AddDataSource.Builder<*>>("zanataDatasource")
                .enableAfterCreate()
                .jndiName("java:jboss/datasources/zanataDatasource")
                .driverName("h2")
                .connectionUrl("jdbc:h2:mem:zanata;DB_CLOSE_DELAY=-1")
                .usernameAndPassword("sa", "sa")
                .validateOnMatch(false)
                .backgroundValidation(false)
                .validConnectionCheckerClass("org.jboss.jca.adapters.jdbc.extensions.novendor.JDBC4ValidConnectionChecker")
                .exceptionSorterClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter")
                .useCcm(true)
                .replaceExisting()
                .build(),

                /* online only
                new AddLocalCache.Builder("zanata")
                        .jndiName("java:jboss/infinispan/container/zanata")
                        .statisticsEnabled(true)
                        .cacheContainer("zanata")
//                        .replaceExisting()
                        .build(),
                */

                ChangeConsoleLogHandler.Builder("CONSOLE")
                        .level(LogLevel.DEBUG)
                        .build(),
                AddLogger.Builder("org.hibernate.SQL")
                        .replaceExisting()
                        .level(LogLevel.DEBUG)
                        .build(),
                AddLogger.Builder("org.hibernate.tool.hbm2ddl")
                        .replaceExisting()
                        .level(LogLevel.DEBUG)
                        .build(),

                AddQueue.Builder("MailsQueue")
                        .durable(true)
                        .jndiEntries(
                                listOf("java:/jms/queue/MailsQueue"))
                        .replaceExisting()
                        .build(),

                AddSecurityDomain.Builder("zanata")
                        .replaceExisting()
                        .build(),
                AddLoginModule.Builder<AddLoginModule.Builder<*>>("org.zanata.security.ZanataCentralLoginModule", "ZanataCentralLoginModule")
                        .securityDomainName("zanata")
                        .replaceExisting()
                        .flag("required")
                        .build(),
                AddSecurityDomain.Builder("zanata.internal")
                        .replaceExisting()
                        .build(),
                AddLoginModule.Builder<AddLoginModule.Builder<*>>("org.zanata.security.jaas.InternalLoginModule", "ZanataInternalLoginModule")
                        .securityDomainName("zanata.internal")
                        .replaceExisting()
                        .flag("required")
                        .build(),
                AddSecurityDomain.Builder("zanata.openid")
                        .replaceExisting()
                        .build(),
                AddLoginModule.Builder<AddLoginModule.Builder<*>>("org.zanata.security.OpenIdLoginModule", "ZanataOpenIdLoginModule")
                        .securityDomainName("zanata.openid")
                        .replaceExisting()
                        .flag("required")
                        .build()
        )
    }

    // TODO this would allow us to reuse configuration, but
    // we will need to filter out the embed-server command from the cli file
//    fun afterStart() {
//        log.info("afterStart")
//        val client = ManagementClient.online(OnlineOptions
//                .standalone()
//                .hostAndPort("localhost", 9990)
//                .protocol(ManagementProtocol.HTTP_REMOTING)
//                .build())
//        client.apply(CliFile(File("../etc/scripts/zanata-config-arq-test.cli")))
//    }

}
