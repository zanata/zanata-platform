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
package org.zanata.rest.service.raw;

import java.io.File;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.JBossDeploymentStructureDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.datasources.AddDataSource;
import org.wildfly.extras.creaper.commands.logging.AddLogger;
import org.wildfly.extras.creaper.commands.logging.ChangeConsoleLogHandler;
import org.wildfly.extras.creaper.commands.logging.LogLevel;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.security.AddLoginModule;
import org.wildfly.extras.creaper.commands.security.AddSecurityDomain;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.zanata.H2DocumentHistoryTrigger;
import org.zanata.RestTest;
import org.zanata.arquillian.LifecycleArquillian;
import org.zanata.arquillian.lifecycle.api.BeforeSetup;
import org.zanata.database.WrappedDatasourceConnectionProvider;
import org.zanata.exception.handler.AccessDeniedExceptionHandler;
import org.zanata.i18n.MessagesFactory;
import org.zanata.jpa.EntityManagerFactoryProducer;
import org.zanata.jpa.EntityManagerProducer;
import org.zanata.model.HLocaleMember;
import org.zanata.rest.AccessDeniedExceptionMapper;
import org.zanata.rest.JaxRSApplication;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.ZanataRestSecurityInterceptor;
import org.zanata.rest.dto.Account;
import org.zanata.rest.helper.RemoteTestSignalerImpl;
import org.zanata.rest.service.AccountService;
import org.zanata.security.SmartEntitySecurityListener;
import org.zanata.servlet.ContextPathProducer;
import org.zanata.servlet.SessionIdProducer;
import org.zanata.ui.faces.FacesContextProducer;
import org.zanata.util.DeltaSpikeWindowIdParam;
import org.zanata.util.HashUtil;
import org.zanata.util.SynchronizationInterceptor;
import org.zanata.util.WithRequestScopeInterceptor;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.arquillian.ArquillianUtil.addClassesWithDependencies;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

@RunWith(LifecycleArquillian.class)
public class AccountRawRestITCase extends RestTest {
    private static final Logger log = LoggerFactory.getLogger(AccountRawRestITCase.class);

    public static Class<?>[] classesWithDependencies() {
        return new Class<?>[]{
                AccountRawRestITCase.class,
                AccountService.class,
                HLocaleMember.class,
                WithRequestScopeInterceptor.class,
                SynchronizationInterceptor.class,
                WrappedDatasourceConnectionProvider.class,
                SmartEntitySecurityListener.class,
                EntityManagerProducer.class,
                EntityManagerFactoryProducer.class,
                SessionIdProducer.class,
                ContextPathProducer.class,
                ServerPathAlt.class,
                DeltaSpikeWindowIdParam.class,
                FacesContextProducer.class,
                MessagesFactory.class,
                H2DocumentHistoryTrigger.class,
                HashUtil.class,
                AccessDeniedExceptionMapper.class,
                AccessDeniedExceptionHandler.class,
                RemoteTestSignalerImpl.class,
                ZanataRestSecurityInterceptor.class,
        };
    }

    @Deployment(name = "AccountService")
    public static WebArchive createDeployment()  {
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava",
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
                        "org.jetbrains.kotlin:kotlin-stdlib"
                )
                .withTransitivity()
                .asFile();
        if (log.isInfoEnabled()) {
            log.info("Found {} libs: {}", libs.length, asList(libs));
        }

        // beans.xml:
//        BeansDescriptor beansXml = Descriptors.importAs(BeansDescriptor.class)
//                .fromStream(AccountRawRestITCase.class.getResourceAsStream("/META-INF/beans.xml"))
//                .getOrCreateAlternatives()
//                .clazz(ServerPathAlt.class.getName())
//                .up();
        BeansDescriptor beansXml = Descriptors.create(BeansDescriptor.class)
                .getOrCreateInterceptors().clazz(
                        "org.zanata.util.WithRequestScopeInterceptor",
                        "org.zanata.util.SynchronizationInterceptor",
                        "org.apache.deltaspike.security.impl.extension.SecurityInterceptor")
                .up()
                .getOrCreateAlternatives().clazz(ServerPathAlt.class.getName())
                .up();

        // jboss-deployment-structure.xml:
        // We need to exclude jackson2 and include jackson1, plus we need the H2 database.
        // NB if it weren't for exclusions, we could just use:
        // war.addAsManifestResource(new StringAsset("Dependencies: com.h2database.h2 optional"), "MANIFEST.MF")
        JBossDeploymentStructureDescriptor jbossDeployXml =
                Descriptors.create(JBossDeploymentStructureDescriptor.class)
                        .getOrCreateDeployment()
                        .getOrCreateExclusions()
                        .createModule().name("org.jboss.resteasy.resteasy-jackson2-provider").up()
                        .up()
                        .getOrCreateDependencies()
                        .createModule().name("com.h2database.h2").optional(true).up()
                        .createModule().name("org.jboss.resteasy.resteasy-jackson-provider").services("import").up()
                        .up().up();
        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "AccountService.war")
                .addAsLibraries(libs)
                .addClasses(JaxRSApplication.class)
                .addAsWebInfResource(new StringAsset(beansXml.exportAsString()), "beans.xml")
                .addAsResource("arquillian/persistence.xml", "META-INF/persistence.xml")
                .addAsResource("META-INF/orm.xml")
                .addAsResource("org/zanata/test/model/AccountData.dbunit.xml")
                .addAsResource("import.sql")
                .addAsWebInfResource(new StringAsset(jbossDeployXml.exportAsString()), jbossDeployXml.getDescriptorName())
                ;
        // NB: this only adds Zanata dependencies. For other dependencies, you
        // should add it to the libraries above (unless it's part of the
        // platform).
        addClassesWithDependencies(war, classesWithDependencies());
//            war.content.forEach { path, _ -> println(path) }
        return war;
    }

    @BeforeSetup
    public static void beforeSetup() throws Exception {
        log.info("beforeSetup");
        String jbossHome = System.getProperty("jboss.home");
        if (jbossHome == null) {
            throw new RuntimeException(
                    "System property jboss.home needs to be set");
        }

        // see also this alternative: https://developer.jboss.org/wiki/AdvancedCLIScriptingWithGroovyRhinoJythonEtc

        OfflineManagementClient client =
                ManagementClient.offline(OfflineOptions.standalone()
                        .rootDirectory(new File(jbossHome))
                        .configurationFile("standalone-full.xml")
                        .build());

        client.apply(new AddDataSource.Builder("zanataDatasource")
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

                new ChangeConsoleLogHandler.Builder("CONSOLE")
                        .level(LogLevel.DEBUG)
                        .build(),
                new AddLogger.Builder("org.hibernate.SQL")
                        .replaceExisting()
                        .level(LogLevel.DEBUG)
                        .build(),
                new AddLogger.Builder("org.hibernate.tool.hbm2ddl")
                        .replaceExisting()
                        .level(LogLevel.DEBUG)
                        .build(),

                new AddQueue.Builder("MailsQueue")
                        .durable(true)
                        .jndiEntries(
                                singletonList("java:/jms/queue/MailsQueue"))
                        .replaceExisting()
                        .build(),

                new AddSecurityDomain.Builder("zanata")
                        .replaceExisting()
                        .build(),
                new AddLoginModule.Builder<>("org.zanata.security.ZanataCentralLoginModule", "ZanataCentralLoginModule")
                        .securityDomainName("zanata")
                        .replaceExisting()
                        .flag("required")
                        .build(),
                new AddSecurityDomain.Builder("zanata.internal")
                        .replaceExisting()
                        .build(),
                new AddLoginModule.Builder<>("org.zanata.security.jaas.InternalLoginModule", "ZanataInternalLoginModule")
                        .securityDomainName("zanata.internal")
                        .replaceExisting()
                        .flag("required")
                        .build(),
                new AddSecurityDomain.Builder("zanata.openid")
                        .replaceExisting()
                        .build(),
                new AddLoginModule.Builder<>("org.zanata.security.OpenIdLoginModule", "ZanataOpenIdLoginModule")
                        .securityDomainName("zanata.openid")
                        .replaceExisting()
                        .flag("required")
                        .build()

                );
    }

//    @AfterStart
//    public static void afterStart() throws Exception {
//        log.info("afterStart");
//        OnlineManagementClient client = ManagementClient.online(OnlineOptions
//                .standalone()
//                .hostAndPort("localhost", 9990)
//                .protocol(ManagementProtocol.HTTP_REMOTING)
//                .build());
//        client.apply(new CliFile(new File("../etc/scripts/zanata-config-arq-test.cli")));
//    }

    @NotNull
    @Override
    protected String getDataSetToClear() {
        return "org/zanata/test/model/AccountData.dbunit.xml";
    }

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void xmlGetUnavailable() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/NOT_AVAILABLE"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void xmlGet() throws Exception {
        new ResourceRequest(getRestEndpointUrl("accounts/u/admin"), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, Account.class);

                Account account = jaxbUnmarshal(entityString, Account.class);
                assertThat(account.getUsername()).isEqualTo("admin");
                assertThat(account.getPasswordHash()).isEqualTo("Eyox7xbNQ09MkIfRyH+rjg==");
                assertThat(account.getEmail()).isEqualTo("root@localhost");
                assertThat(account.getApiKey()).isEqualTo("b6d7044e9ee3b2447c28fb7c50d86d98");
                // 1 role
                assertThat(account.getRoles().size()).isEqualTo(1);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void jsonGet() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/accounts/u/admin"), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, Account.class);

                Account account = jsonUnmarshal(entityString, Account.class);
                assertThat(account.getUsername()).isEqualTo("admin");
                assertThat(account.getPasswordHash()).isEqualTo("Eyox7xbNQ09MkIfRyH+rjg==");
                assertThat(account.getEmail()).isEqualTo("root@localhost");
                assertThat(account.getApiKey()).isEqualTo("b6d7044e9ee3b2447c28fb7c50d86d98");
                // 1 role
                assertThat(account.getRoles().size()).isEqualTo(1);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void xmlPut() throws Exception {
        final Account account =
                new Account("test@testing.com", "Test Account", "testuser",
                        "Eyox7xbNQ09MkIfRyH+rjg==");
        account.setEnabled(false);

        new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(account), MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void jsonPut() throws Exception {
        final Account account =
                new Account("test@testing.com", "Test Account", "testuser",
                        "Eyox7xbNQ09MkIfRyH+rjg==");
        account.setEnabled(false);

        new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(account), MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void unauthorizedPut() throws Exception {
        final Account account =
                new Account("test@testing.com", "Test Account", "testuser",
                        "Eyox7xbNQ09MkIfRyH+rjg==");
        account.setEnabled(false);

        new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(account), MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
            }
        }.run();
    }

}
