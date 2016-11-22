package org.zanata.service;

import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQL5Dialect;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EntityManagerFactoryRule extends ExternalResource {
    private static final Logger log =
            LoggerFactory.getLogger(EntityManagerFactoryRule.class);
    private static final String PERSIST_NAME = "zanataDatasourcePU";
    private static final String MYSQL_TEST_DB_URL =
            "jdbc:log4jdbc:mysql://localhost:3306/zanata_unit_test?characterEncoding=UTF-8";
    private static final String MYSQL_DIALECT =
            MySQL5Dialect.class.getCanonicalName();

    private EntityManagerFactory emf;
    private TestProfile profile;

    // NOTE: if you use mysql and you have the schema created, just change it to
    // update will speed up subsequent test runs
    // private static String hbm2ddl = "create";
    // protected static String hbm2ddl = "validate";
    private String hbm2ddlAuto = firstNonNull(System.getenv("test_hbm2ddl"), "create_drop");

    public EntityManagerFactoryRule(TestProfile profile) {
        this.profile = profile;
    }

    public EntityManagerFactoryRule(TestProfile profile, String hbm2ddlAuto) {
        this.profile = profile;
        this.hbm2ddlAuto = hbm2ddlAuto;
    }

    public static String mySqlUrl() {
        return firstNonNull(System.getenv("test_db_url"), MYSQL_TEST_DB_URL);
    }

    public static String mySqlUsername() {
        return firstNonNull(System.getenv("test_db_username"), "root");
    }

    public static String mySqlPassword() {
        return firstNonNull(System.getenv("test_db_password"), "root");
    }

    @Override
    protected void before() throws Throwable {
        log.debug("Initializing EMF");
        switch (profile) {
            case NormalBuild:
                emf = Persistence.createEntityManagerFactory(PERSIST_NAME);
                break;
            case ManualPerformanceProfiling:
                Map<String, String> MySQLProps =
                        ImmutableMap
                                .<String, String>builder()
                                .put(Environment.URL, mySqlUrl())
                                .put(Environment.USER, mySqlUsername())
                                .put(Environment.PASS, mySqlPassword())
                                .put(Environment.HBM2DDL_AUTO, hbm2ddlAuto)
                                // .put(Environment.USE_DIRECT_REFERENCE_CACHE_ENTRIES,
                                // "true")
                                // .put(Environment.DEFAULT_BATCH_FETCH_SIZE, "50")
                                // .put(Environment.STATEMENT_BATCH_SIZE, "50")
                                // .put(Environment.USE_SECOND_LEVEL_CACHE, "false")
                                // .put(Environment.USE_QUERY_CACHE, "false")
                                .put(Environment.DIALECT, MYSQL_DIALECT)
                                .build();
                emf = Persistence
                        .createEntityManagerFactory(PERSIST_NAME, MySQLProps);
                break;
        }
    }

    @Override
    protected void after() {
        log.debug("Shutting down EMF");
        emf.close();
        emf = null;
    }

    public EntityManagerFactory getEmf() {
        return emf;
    }

    public enum TestProfile {
        /**
         * This will use the default in-memory h2 database for testing.
         */
        NormalBuild,
        /**
         * This will use a mysql database with {@link EntityManagerFactoryRule#MYSQL_TEST_DB_URL}
         * and {@link EntityManagerFactoryRule#hbm2ddlAuto} as HBM2DDL auto
         * setting.
         */
        ManualPerformanceProfiling
    }
}
