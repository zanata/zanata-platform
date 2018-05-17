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
package org.zanata;

import java.net.URL;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.arquillian.RemoteAfter;
import org.zanata.arquillian.RemoteBefore;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequestEnvironment;

import com.google.common.collect.ImmutableMap;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

/**
 * Provides basic test utilities to test raw REST APIs and compatibility.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(Arquillian.class)
@Exclude
public abstract class RestTest {
    private static final Logger log = LoggerFactory.getLogger(RestTest.class);

    // Admin credentials
    protected static final String ADMIN = "admin";
    protected static final String ADMIN_KEY =
            "b6d7044e9ee3b2447c28fb7c50d86d98";
    // Translator credentials
    protected static final String TRANSLATOR = "demo";
    protected static final String TRANSLATOR_KEY =
            "23456789012345678901234567890123";

    static {
        // Tell DeltaSpike to give more warning messages
        ProjectStageProducer.setProjectStage(ProjectStage.IntegrationTest);
    }

    // Authorized environment with valid credentials
    private static final ResourceRequestEnvironment ENV_AUTHORIZED =
            () -> ImmutableMap.of(
                    "X-Auth-User", ADMIN,
                    "X-Auth-Token", ADMIN_KEY);

    private DBUnitProvider dbUnitProvider = new DBUnitProvider(
            RestTest.this::getConnection);

    @ArquillianResource
    private URL deploymentUrl;

    /**
     * Implement this in a subclass.
     * <p/>
     * Use it to stack DBUnit <tt>DataSetOperation</tt>'s with the
     * <tt>beforeTestOperations</tt> and <tt>afterTestOperations</tt> lists.
     */
    protected abstract void prepareDBUnitOperations();

    public void
            addBeforeTestOperation(DBUnitProvider.DataSetOperation operation) {
        dbUnitProvider.addBeforeTestOperation(operation);
    }

    public void
            addAfterTestOperation(DBUnitProvider.DataSetOperation operation) {
        dbUnitProvider.addAfterTestOperation(operation);
    }

    /**
     * Invoked on the arquillian container before the test is run.
     */
    @RemoteBefore
    public void prepareDataBeforeTest() {
        log.info("Executing prepareDataBeforeTest()");
        String dataSetToClear = getDataSetToClear();
        if (dataSetToClear != null) {
            addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                    dataSetToClear,
                    DatabaseOperation.DELETE_ALL));
            addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                    dataSetToClear,
                    DatabaseOperation.DELETE_ALL));
        }
        prepareDBUnitOperations();
        dbUnitProvider.prepareDataBeforeTest();
        // Clear the hibernate cache
        entityManagerFactory().getCache().evictAll();
    }

    private EntityManagerFactory entityManagerFactory() {
        return CDI.current().select(EntityManagerFactory.class).get();
    }

    @CheckForNull
    protected String getDataSetToClear() {
        return "org/zanata/test/model/ClearAllTables.dbunit.xml";
    }

    /**
     * Invoked in the arquillian container after the test is run.
     */
    @RemoteAfter
    public void cleanDataAfterTest() {
        log.info("Executing cleanDataAfterTest()");
        dbUnitProvider.cleanDataAfterTest();
    }

    @Rule
    public TestName testName = new TestName();

    @Before
    public void signalBeforeTest() throws Exception {
        ResteasyWebTarget webTarget = new ResteasyClientBuilder().build()
                .target(getRestEndpointUrl() + "test/remote/signal/before");
        // test resources allow anonymous access
        Response response = webTarget
                .queryParam("c", this.getClass().getName())
                .queryParam("m", testName.getMethodName())
                .request().build("post").invoke();
        Response.StatusType statusInfo = response.getStatusInfo();
        if (statusInfo.getFamily() != SUCCESSFUL) {
            throw new Exception("bad response: " + statusInfo.getStatusCode() + " " + statusInfo);
        }
    }

    @After
    public void signalAfterTest() throws Exception {
        ResteasyWebTarget webTarget = new ResteasyClientBuilder().build()
                .target(getRestEndpointUrl() + "test/remote/signal/after");
        // test resources allow anonymous access
        Response response = webTarget
                .queryParam("c", this.getClass().getName())
                .queryParam("m", testName.getMethodName())
                .request().build("post").invoke();
        if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
            throw new Exception("bad response: " + response.getStatusInfo());
        }
    }

    private IDatabaseConnection getConnection() {
        try {
            DataSource dataSource = getDataSource();
            DatabaseConnection dbConn =
                    new DatabaseConnection(dataSource.getConnection());
//            dbConn.getConfig().setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, false);
            return dbConn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected DataSource getDataSource() throws NamingException {
        return (DataSource) new InitialContext().lookup(
                "java:jboss/datasources/zanataDatasource");
    }

    /**
     * @return The artifact's base deployment Url.
     */
    public URL getDeploymentUrl() {
        return deploymentUrl;
    }

    /**
     * Gets the full Url for a Rest endpoint.
     *
     * @param resourceUrl
     *            The relative resource url.
     * @return The full absolute url of the deployed resource.
     */
    public final String getRestEndpointUrl(String resourceUrl) {
        StringBuilder fullUrl =
                new StringBuilder(getDeploymentUrl() + "rest");
        if (!resourceUrl.startsWith("/")) {
            fullUrl.append("/");
        }
        return fullUrl.append(resourceUrl).toString();
    }

    /**
     * Gets the artifact's deployed url for REST endpoints.
     *
     * @return The full absolute root url of the deployed artifact.
     * @see RestTest#getRestEndpointUrl(String)
     */
    public final String getRestEndpointUrl() {
        return getRestEndpointUrl("/");
    }

    /**
     * Gets a valid Authorized REST environment.
     *
     * @return A Resource Request execution environment with valid test
     *         credentials.
     */
    public static ResourceRequestEnvironment getAuthorizedEnvironment() {
        return ENV_AUTHORIZED;
    }

    /**
     * Gets an empty header for REST request.
     */
    public static ResourceRequestEnvironment getEmptyHeaderEnvironment() {
        return ResourceRequestEnvironment.EMPTY;
    }

    public static ResourceRequestEnvironment getTranslatorHeaders() {
        return () -> ImmutableMap.of(
                "X-Auth-User", TRANSLATOR,
                "X-Auth-Token", TRANSLATOR_KEY);
    }

    protected ResteasyWebTarget addExtensionToRequest(Set<String> extensions,
            ResteasyWebTarget webTarget) {
        if (extensions != null && !extensions.isEmpty()) {
            for (String extension: extensions) {
                webTarget = webTarget.queryParam("ext", extension);
            }
        }
        return webTarget;
    }

}
