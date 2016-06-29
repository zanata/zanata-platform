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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ClientRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.zanata.arquillian.RemoteAfter;
import org.zanata.arquillian.RemoteBefore;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequestEnvironment;
import org.zanata.util.ServiceLocator;

import com.google.common.collect.Lists;

/**
 * Provides basic test utilities to test raw REST APIs and compatibility.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(Arquillian.class)
@Exclude
public abstract class RestTest {
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
            new ResourceRequestEnvironment() {
                @Override
                public Map<String, Object> getDefaultHeaders() {
                    return new HashMap<String, Object>() {
                        {
                            put("X-Auth-User", ADMIN);
                            put("X-Auth-Token", ADMIN_KEY);
                        }
                    };
                }
            };

    private DBUnitProvider dbUnitProvider = new DBUnitProvider() {
        @Override
        protected IDatabaseConnection getConnection() {
            return RestTest.getConnection();
        }
    };

    @ArquillianResource
    protected URL deploymentUrl;

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
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        prepareDBUnitOperations();
        addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        dbUnitProvider.prepareDataBeforeTest();
        // Clear the hibernate cache
        ServiceLocator.instance().getEntityManagerFactory().getCache()
                .evictAll();
    }

    /**
     * Invoked in the arquillian container after the test is run.
     */
    @RemoteAfter
    public void cleanDataAfterTest() {
        dbUnitProvider.cleanDataAfterTest();
    }

    @Rule
    public final TestName testName = new TestName();

    @Before
    public void signalBeforeTest() {
        ClientRequest clientRequest =
                new ClientRequest(getRestEndpointUrl() + "test/remote/signal/before");
        // test resources allow anonymous access
        try {
            clientRequest
                    .queryParameter("c", this.getClass().getName())
                    .queryParameter("m", testName.getMethodName())
                    .post();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void signalAfterTest() {
        ClientRequest clientRequest =
                new ClientRequest(getRestEndpointUrl() + "test/remote/signal/after");
        // test resources allow anonymous access
        try {
            clientRequest
                    .queryParameter("c", this.getClass().getName())
                    .queryParameter("m", testName.getMethodName())
                    .post();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static IDatabaseConnection getConnection() {
        try {
            DataSource dataSource =
                    (DataSource) new InitialContext().lookup(
                            "java:jboss/datasources/zanataDatasource");
            DatabaseConnection dbConn =
                    new DatabaseConnection(dataSource.getConnection());
            return dbConn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The artifact's base deployment Url.
     */
    public String getDeploymentUrl() {
        return deploymentUrl.toString();
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
    public static final ResourceRequestEnvironment getAuthorizedEnvironment() {
        return ENV_AUTHORIZED;
    }

    /**
     * Gets an empty header for REST request.
     */
    public static final ResourceRequestEnvironment getEmptyHeaderEnvironment() {
        return new ResourceRequestEnvironment() {
            @Override
            public Map<String, Object> getDefaultHeaders() {
                return new HashMap<String, Object>();
            }
        };
    }

    public static ResourceRequestEnvironment getTranslatorHeaders() {
        return new ResourceRequestEnvironment() {
            @Override
            public Map<String, Object> getDefaultHeaders() {
                return new HashMap<String, Object>() {
                    {
                        put("X-Auth-User", TRANSLATOR);
                        put("X-Auth-Token", TRANSLATOR_KEY);
                    }
                };
            }
        };
    }

    protected void addExtensionToRequest(Set<String> extensions,
            ClientRequest request) {
        if (extensions != null) {
            request.getQueryParameters().put("ext", Lists
                    .newArrayList(extensions));
        }
    }

}
