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
import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.deltaspike.servlet.impl.produce.ServletObjectProducer;
import org.atteo.classindex.ClassIndex;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.datasources.AddDataSource;
import org.wildfly.extras.creaper.commands.foundation.online.CliFile;
import org.wildfly.extras.creaper.commands.infinispan.cache.AddLocalCache;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.security.AddLoginModule;
import org.wildfly.extras.creaper.commands.security.AddSecurityDomain;
import org.wildfly.extras.creaper.commands.security.RemoveSecurityDomain;
import org.wildfly.extras.creaper.commands.security.realms.AddLocalAuthentication;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.ManagementProtocol;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.zanata.RestTest;
import org.zanata.arquillian.LifecycleArquillian;
import org.zanata.arquillian.lifecycle.api.AfterStart;
import org.zanata.arquillian.lifecycle.api.BeforeDeploy;
import org.zanata.arquillian.lifecycle.api.BeforeSetup;
import org.zanata.arquillian.lifecycle.api.BeforeStart;
import org.zanata.dao.GlossaryDAO;
import org.zanata.database.WrappedDatasourceConnectionProvider;
import org.zanata.i18n.MessagesFactory;
import org.zanata.jpa.EntityManagerFactoryProducer;
import org.zanata.jpa.EntityManagerProducer;
import org.zanata.rest.JaxRSApplication;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.Account;
import org.zanata.rest.service.AccountService;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.security.AuthenticatedAccountSessionScopeHolder;
import org.zanata.security.SmartEntitySecurityListener;
import org.zanata.service.impl.ConfigurationServiceImpl;
import org.zanata.service.impl.GlossarySearchServiceImpl;
import org.zanata.service.impl.GravatarServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.servlet.ContextPathProducer;
import org.zanata.servlet.SessionIdProducer;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesContextProducer;
import org.zanata.util.DeltaSpikeWindowIdParam;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.arquillian.ArquillianUtil.addClassesWithDependencies;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

//@RunWith(LifecycleArquillian.class)
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({
        AccountService.class,
        ContextPathProducer.class,
        ConfigurationServiceImpl.class,
        DeltaSpikeWindowIdParam.class,
        EntityManagerFactoryProducer.class,
        EntityManagerProducer.class,
        FacesContextProducer.class,
        GravatarServiceImpl.class,
        GlossarySearchServiceImpl.class,
        LocaleServiceImpl.class,
        MessagesFactory.class,
        SessionIdProducer.class,
        SmartEntitySecurityListener.class,
//        WindowContextProducer.class,
        ServletObjectProducer.class,
        ZanataJpaIdentityStore.class,
        GlossaryDAO.class,
        AuthenticatedAccountSessionScopeHolder.class,
//        WindowBeanHolder.class,
        WrappedDatasourceConnectionProvider.class })
@SupportDeltaspikeCore
public class AccountRawRestITCase extends RestTest {

    static {
//        StackDumper.dumpWhenSysOutContains("interface org.zanata.servlet.annotations.AllJavaLocales");
    }

    private static final Logger log = LoggerFactory.getLogger(AccountRawRestITCase.class);

    public static Collection<Class> classesWithDependencies() {
        return asList(
                AccountService.class,
                ContextPathProducer.class,
                ConfigurationServiceImpl.class,
                DeltaSpikeWindowIdParam.class,
                EntityManagerFactoryProducer.class,
                EntityManagerProducer.class,
                FacesContextProducer.class,
                GravatarServiceImpl.class,
                GlossarySearchServiceImpl.class,
                LocaleServiceImpl.class,
                MessagesFactory.class,
                SessionIdProducer.class,
                SmartEntitySecurityListener.class,
                WrappedDatasourceConnectionProvider.class
        );
    }

    @Deployment(name = "AccountService", testable = false)
    public static WebArchive createDeployment()  {
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava",
                        "com.google.gwt:gwt-servlet",
                        "com.ibm.icu:icu4j",
                        "net.sourceforge.openutils:openutils-log4j",
                        "org.apache.deltaspike.modules:deltaspike-security-module-api",
                        "org.apache.deltaspike.modules:deltaspike-servlet-module-impl",
                        "org.jetbrains.kotlin:kotlin-stdlib"
                )
                .withTransitivity()
                .asFile();
        if (log.isInfoEnabled()) {
            log.info("Found {} libs: {}", libs.length, asList(libs));
        }
        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "AccountService.war")
                .addAsLibraries(libs)
                .addClasses(JaxRSApplication.class, ClassIndex.class)
//                    .addAsResource(
//                            EmptyAsset.INSTANCE,
//                            "beans.xml")
//                    .addAsWebInfResource(
//                            EmptyAsset.INSTANCE,
//                            "beans.xml")
                .addAsResource("arquillian/persistence.xml", "META-INF/persistence.xml")
                .addAsResource("META-INF/orm.xml")
                ;
        // NB: this only adds Zanata dependencies. For other dependencies, you
        // should add it to the libraries above (unless it's part of the
        // platform).
        addClassesWithDependencies(war,
                classesWithDependencies().toArray(new Class[0])
//                AccountService.class,
//                ContextPathProducer.class,
//                ConfigurationServiceImpl.class,
//                DeltaSpikeWindowIdParam.class,
//                EntityManagerFactoryProducer.class,
//                EntityManagerProducer.class,
//                FacesContextProducer.class,
//                GravatarServiceImpl.class,
//                GlossarySearchServiceImpl.class,
//                LocaleServiceImpl.class,
//                MessagesFactory.class,
//                SessionIdProducer.class,
//                SmartEntitySecurityListener.class,
//                WrappedDatasourceConnectionProvider.class
        );
//            war.content.forEach { path, _ -> println(path) }
        return war;
    }

    @BeforeSetup
    public static void beforeSetup() throws Exception {
        System.out.println("beforeSetup");
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
//
//    @BeforeStart
//    public static void beforeStart() throws Exception {
//        System.out.println("beforeStart");
//    }
//
//    @AfterStart
//    public static void afterStart() throws Exception {
//        System.out.println("afterStart");
//        OnlineManagementClient client = ManagementClient.online(OnlineOptions
//                .standalone()
//                .hostAndPort("localhost", 9990)
//                .protocol(ManagementProtocol.HTTP_REMOTING)
//                .build());
//        client.apply(new CliFile(new File("../etc/scripts/zanata-config-arq-test.cli")));
//    }
//
//    @BeforeDeploy
//    public static void beforeDeploy() throws Exception {
//        System.out.println("beforeDeploy");
//    }

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void before() {
        prepareDataBeforeTest();
    }

    @After
    public void after() {
        cleanDataAfterTest();
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
                assertThat(response.getStatus(),
                        is(Status.NOT_FOUND.getStatusCode()));
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
                assertThat(response.getStatus(), is(200));
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, Account.class);

                Account account = jaxbUnmarshal(entityString, Account.class);
                assertThat(account.getUsername(), is("admin"));
                assertThat(account.getPasswordHash(),
                        is("Eyox7xbNQ09MkIfRyH+rjg=="));
                assertThat(account.getEmail(), is("root@localhost"));
                assertThat(account.getApiKey(),
                        is("b6d7044e9ee3b2447c28fb7c50d86d98"));
                assertThat(account.getRoles().size(), is(1)); // 1 roles
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
                assertThat(response.getStatus(), is(200));
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, Account.class);

                Account account = jsonUnmarshal(entityString, Account.class);
                assertThat(account.getUsername(), is("admin"));
                assertThat(account.getPasswordHash(),
                        is("Eyox7xbNQ09MkIfRyH+rjg=="));
                assertThat(account.getEmail(), is("root@localhost"));
                assertThat(account.getApiKey(),
                        is("b6d7044e9ee3b2447c28fb7c50d86d98"));
                assertThat(account.getRoles().size(), is(1)); // 1 role
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
                assertThat(response.getStatus(),
                        is(Status.CREATED.getStatusCode()));
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
                assertThat(response.getStatus(),
                        is(Status.CREATED.getStatusCode()));
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
                assertThat(response.getStatus(),
                        is(Status.UNAUTHORIZED.getStatusCode()));
            }
        }.run();
    }

}
