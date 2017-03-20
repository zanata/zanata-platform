/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jetbrains.annotations.NotNull;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.PropertiesHolder;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DatabaseDDLTest {
    private static final Logger log =
            LoggerFactory.getLogger(DatabaseDDLTest.class);
    private static final Pattern LIQUIBASE_FK_PATTERN = Pattern.compile(".+FOREIGN KEY .+ REFERENCES .+");

    private static File ddlExtractCmdFile;
    private static String ddlExtractCmd;

    @BeforeClass
    public static void setUp() {
        ddlExtractCmdFile =
                new File(PropertiesHolder.getProperty("mysqldump.bin"));
        // maven for some reason will fail to resolve mysql.socket in this property
        ddlExtractCmd = PropertiesHolder.getProperty("mysqldump.ddl.cmd");

        String mysqlSocket = PropertiesHolder.getProperty("mysql.socket");
        log.debug("mysqldump cmd before: {}, socket: {}", ddlExtractCmd, mysqlSocket);
        ddlExtractCmd = ddlExtractCmd.replace("${mysql.socket}",
                mysqlSocket);
        log.debug("mysqldump cmd after: {}", ddlExtractCmd);
    }

    @Test
    @Ignore("This will fail on first error. Eventually we want to fix all of the errors this reports.")
    public void testHibernateValidate() {
        String url = PropertiesHolder.getProperty("hibernate.connection.url");
        String username =
                PropertiesHolder.getProperty("hibernate.connection.username");
        String password =
                PropertiesHolder.getProperty("hibernate.connection.password");
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.connection.url", url);
        properties.put("hibernate.connection.username", username);
        properties.put("hibernate.connection.password", password);
        EntityManagerFactory emf = Persistence
                .createEntityManagerFactory("zanataDatasourcePU", properties);

        EntityManager em = emf.createEntityManager();
        em.close();
    }

    /**
     * When running this test in IDE, some of the properties may not be set (in
     * maven, there is some plugins and scripts to set those. e.g.
     * functional-test/etc/mysql-socket.groovy). IDE maven re-import will also
     * wipe out those properties. If you want to run this in IDE after cargo
     * wait, just copy those value from your console and paste them into
     * functional-test/target/test-classes/setup.properties.
     */
    @Test
    public void compareDDLBetweenLiquibaseAndHibernate() throws Exception {
        Assume.assumeTrue("mysqldump is available",
                ddlExtractCmdFile.exists() && ddlExtractCmdFile.canExecute());
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("hibernate-ddl/mysql.sql");
        Assume.assumeTrue("hibernate generated DDL is on classpath", inputStream != null);

        ProcessBuilder processBuilder = new ProcessBuilder(ddlExtractCmd.split(" "));

        // this is under target/ so will get clean up
        File ddlFromLiquibase = new File(ddlExtractCmdFile.getParentFile(),
                "liquibase-ddl.sql");
        log.info("liquibase-ddl.sql file: {}", ddlFromLiquibase);

        extractDDLFromLiquibaseGeneratedDatabase(processBuilder,
                ddlFromLiquibase);

        List<String> ddlFromLiquibaseLines =
                Files.readAllLines(ddlFromLiquibase.toPath(), Charsets.UTF_8);
        List<String> foreignKeyConstraints =
                getAllForeignKeyConstraintsFromLiquibaseDDL(ddlFromLiquibaseLines);

        log.info("foreign key from liquibase: {}", foreignKeyConstraints);
        List<String> uniqueKeyConstraints = ddlFromLiquibaseLines.stream()
                .filter(line -> line.contains("UNIQUE KEY")).collect(
                        Collectors.toList());

        log.info("unique key from liquibase: {}", uniqueKeyConstraints);

        List<String> hibernateDDLConstraints =
                readDDLFromHibernate(inputStream).build();


        List<String> hbmForeignKeys = hibernateDDLConstraints.stream()
                .filter(line -> line.contains("foreign key"))
                .sorted((one, two) -> {
                    int refIndex1 = one.indexOf("references");
                    int refIndex2 = two.indexOf("references");
                    return one.substring(refIndex1)
                            .compareTo(two.substring(refIndex2));
                }).collect(Collectors.toList());
        log.info("hibernate ddl: {}", hbmForeignKeys);


        List<String> hbmUniqueKeys = hibernateDDLConstraints.stream()
                .filter(line -> line.contains("unique")).collect(
                        Collectors.toList());
        log.info("hibernate unique key: {}", hbmUniqueKeys);
    }

    private static ImmutableList.Builder<String> readDDLFromHibernate(
            InputStream inputStream) throws IOException {
        ImmutableList.Builder<String> hibernateDDLConstraints = ImmutableList.builder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("alter table")) {
                    // read entire constraint until a blank new line appears
                    StringBuilder constraint = new StringBuilder(line);
                    while (line != null && !line.trim().isEmpty()) {
                        line = reader.readLine();
                        if (line != null) {
                            constraint.append(" ").append(line.trim());
                        }
                    }
                    hibernateDDLConstraints.add(constraint.toString());
                } else {
                    // ignore lines that are not constraint related
                    line = reader.readLine();
                }

            }
        }
        return hibernateDDLConstraints;
    }

    private static void extractDDLFromLiquibaseGeneratedDatabase(
            ProcessBuilder processBuilder, File outputFile)
            throws IOException, InterruptedException {
        Process process =
                processBuilder.redirectOutput(outputFile).start();
        boolean terminated = process.waitFor(3, TimeUnit.MINUTES);
        if (!terminated) {
            log.warn(
                    "mysqldump has not finished within the timeout period. will kill it");
            process.destroy();
            throw new RuntimeException("mysqldump timeout");
        }
    }

    private static List<String> getAllForeignKeyConstraintsFromLiquibaseDDL(
            List<String> ddlFromLiquibaseLines) {
        return ddlFromLiquibaseLines.stream()
                .filter(line -> line.contains("FOREIGN KEY"))
                .map(line -> {
                    Matcher matcher = LIQUIBASE_FK_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String match = matcher.group();
                        if (match.endsWith(",")) {
                            return match.substring(0, line.length() - 1).trim();
                        }
                        return match.trim();
                    }
                    throw new RuntimeException("can't find a match in:" + line);
                })
                .sorted((one, two) -> {
                    int refIndex1 = one.indexOf("REFERENCES");
                    int refIndex2 = two.indexOf("REFERENCES");
                    return one.substring(refIndex1)
                            .compareTo(two.substring(refIndex2));
                })
                .collect(Collectors.toList());
    }
}
