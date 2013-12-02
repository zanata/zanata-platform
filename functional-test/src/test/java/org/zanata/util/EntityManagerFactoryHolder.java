package org.zanata.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerFactoryHolder {
    private static final EntityManagerFactoryHolder HOLDER =
            new EntityManagerFactoryHolder();
    private static EntityManagerFactory emFactory;
    private final Properties overrideProps;

    private EntityManagerFactoryHolder() {
        copyPersistenceXML();
        overrideProps = new Properties();
        overrideProps.put("hibernate.hbm2ddl.auto", "");
        overrideProps
                .put("hibernate.connection.provider_class",
                        "org.hibernate.service.jdbc.connections.internal.DriverManagerConnectionProviderImpl");
        overrideProps.put("hibernate.connection.url",
                PropertiesHolder.getProperty("hibernate.connection.url"));
        overrideProps.put("hibernate.search.default.indexBase",
                PropertiesHolder
                        .getProperty("hibernate.search.default.indexBase"));
    }

    /**
     * This is a temporary workaround to reduce another copy of persistence.xml.
     * It rely on zanata-war being built (so that the persistenc.xml appears in
     * target/test-classes/META-INF)
     */
    private static void copyPersistenceXML() {
        URL resource =
                Thread.currentThread().getContextClassLoader().getResource(".");
        File currentDir = new File(resource.getPath());
        String current = currentDir.getAbsolutePath();
        String testPersistenceXML =
                current.replace("functional-test/target/test-classes",
                        "zanata-war/target/test-classes/META-INF/persistence.xml");

        File xml = new File(testPersistenceXML);
        Preconditions.checkState(
                xml.exists() && xml.getName().equals("persistence.xml"),
                "%s does not exists. You will need to build zanata-war first.",
                xml.getAbsolutePath());

        File destDir = new File(currentDir, "META-INF");
        destDir.mkdirs();
        try {
            Files.copy(xml, new File(destDir, "persistence.xml"));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static EntityManagerFactoryHolder holder() {
        return HOLDER;
    }

    public EntityManagerFactory getEmFactory() {
        if (emFactory == null) {
            synchronized (HOLDER) {
                if (emFactory == null) {
                    emFactory =
                            Persistence.createEntityManagerFactory(
                                    "zanataTestDatasourcePU", overrideProps);
                }
            }
        }
        return emFactory;
    }

}
