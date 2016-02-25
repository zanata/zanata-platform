/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands;

import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidator;
import static org.zanata.client.commands.Messages.get;
import static org.zanata.util.VersionUtility.getVersionInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.fedorahosted.openprops.Properties;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * This class checks whether there is newer version of client available. It will
 * check a file on disk to determine check frequency and whether should check
 * now. If yes will query OSS sonatype for latest zanata client version and then
 * compare to current version.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class UpdateChecker {
    private static final Logger log =
            LoggerFactory.getLogger(UpdateChecker.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final String OSS_URL =
            "https://oss.sonatype.org/service/local/";
    // update marker file valid properties
    private static final String LAST_CHECKED = "lastChecked";
    private static final String FREQUENCY = "frequency";
    private static final String NO_ASKING = "noAsking";

    private final String sonatypeRestUrl;
    private final ConsoleInteractor console;
    private final String currentVersionNo;
    private final File updateMarker;

    public UpdateChecker(ConsoleInteractor console) {
        this(OSS_URL, defaultUpdateMarkerFile(), console,
                getVersionInfo(UpdateChecker.class).getVersionNo());
    }

    private static File defaultUpdateMarkerFile() {
        return new File(new File(System.getProperty("user.home"), ".config"),
                "zanata-client-update.properties");
    }

    @VisibleForTesting
    protected UpdateChecker(String sonatypeRestUrl,
            File updateMarker,
            ConsoleInteractor console, String currentVersionNo) {
        this.sonatypeRestUrl = sonatypeRestUrl;
        this.console = console;
        this.currentVersionNo = currentVersionNo;
        this.updateMarker = updateMarker;
    }

    public boolean needToCheckUpdates(boolean interactiveMode) {
        DateTime today = new DateTime();
        try {
            if (!updateMarker.exists()) {
                createUpdateMarkerFile(updateMarker);
                console.printfln(get("update.marker.created"), updateMarker);
                console.printfln(get("update.marker.hint"));
                return true;
            }
            // read the content and see if we need to check
            Properties props = loadFileToProperties(updateMarker);
            DateTime lastCheckedDate = readLastCheckedDate(props);
            Days daysPassed = Days.daysBetween(lastCheckedDate, today);
            Frequency frequency = readFrequency(props);
            boolean timeToCheck = daysPassed.compareTo(frequency.days()) >= 0;
            boolean noAsking = readNoAsking(props);
            if (timeToCheck && !noAsking && interactiveMode) {
                console.printf(get("check.update.yes.no"), daysPassed.getDays());
                String check = console.expectAnswerWithRetry(
                        AnswerValidator.YES_NO);
                if (check.toLowerCase().startsWith("n")) {
                    return false;
                }
            }
            return timeToCheck;
        } catch (Exception e) {
            log.debug("Error checking update marker file", e);
            log.warn("Error checking update marker file {}", updateMarker);
            log.warn("Please make sure its permission and content format");
            return false;
        }
    }

    private static DateTime readLastCheckedDate(Properties props) {
        return DATE_FORMATTER.parseDateTime(props.getProperty(LAST_CHECKED));
    }

    private static Frequency readFrequency(Properties props) {
        return Frequency.from(props.getProperty(FREQUENCY,
                Frequency.weekly.name()));
    }
    private static boolean readNoAsking(Properties props) {
        return props.getProperty(NO_ASKING, "false").equalsIgnoreCase("true");
    }

    private static Properties loadFileToProperties(File updateMarker) {
        Properties props = new Properties();
        try (InputStreamReader reader =
                new InputStreamReader(new FileInputStream(updateMarker),
                        Charsets.UTF_8)) {
            props.load(reader);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return props;
    }

    private static void createUpdateMarkerFile(File updateMarker)
            throws IOException {
        boolean created = updateMarker.createNewFile();
        Preconditions.checkState(created, get("create.file.failure"),
                updateMarker);
        String today = DATE_FORMATTER.print(new DateTime());
        Properties props = new Properties();
        props.setProperty(LAST_CHECKED, today);
        props.setComment(FREQUENCY, get("valid.frequency"));
        props.setProperty(FREQUENCY, "weekly");
        props.setProperty(NO_ASKING, "true");
        props.setComment(NO_ASKING, get("no.check.update.prompt"));
        props.store(new BufferedWriter(new FileWriterWithEncoding(updateMarker,
                Charsets.UTF_8)), null);
    }

    public void checkNewerVersion() {
        Optional<String> latestVersion = checkLatestVersion(console);
        if (!latestVersion.isPresent()) {
            return;
        }
        if (latestVersion.get().compareTo(currentVersionNo) > 0) {
            console.printfln(get("suggest.update"), latestVersion.get());
        } else {
            console.printfln(get("latest.version.confirm"));
        }
        try {
            Properties props = loadFileToProperties(updateMarker);
            String today = DATE_FORMATTER.print(new DateTime());
            props.setProperty(LAST_CHECKED, today);
            props.store(new BufferedWriter(new FileWriterWithEncoding(
                    updateMarker, Charsets.UTF_8)), null);
        } catch (IOException e) {
            log.warn("failed to load file {}", updateMarker);
        }
    }

    /**
     * This calls oss.sonatype.org's REST api and resolve latest version of
     * client.
     *
     * @return latest version of client in sonatype oss
     */
    private Optional<String> checkLatestVersion(ConsoleInteractor console) {
        ClientResponse response;
        try {
            DefaultClientConfig clientConfig =
                    new DefaultClientConfig();

            Client client = com.sun.jersey.api.client.Client.create(
                    clientConfig);
            WebResource target =
                    client.resource(sonatypeRestUrl)
                            .path("artifact/maven/resolve")
                            .queryParam("g", "org.zanata")
                            .queryParam("a", "client")
                            .queryParam("p", "pom")
                            .queryParam("v", "LATEST")
                            .queryParam("r", "releases");
            response = target.get(ClientResponse.class);
            if (response.getClientResponseStatus() != ClientResponse.Status.OK) {
                log.debug(
                        "Failed to resolve latest client artifact [status {}]. Ignored",
                        response.getStatus());
                console.printfln(get("check.update.failed"));
                return Optional.absent();
            }
        } catch (Exception e) {
            log.warn("Exception when checking updates", e);
            console.printfln(get("check.update.failed"));
            return Optional.absent();
        }
        // cheap xml parsing
        String payload =
                response.getEntity(String.class).replaceAll("\\n", "");
        Pattern pattern = Pattern.compile("^.+<version>(.+)</version>.+");
        Matcher matcher = pattern.matcher(payload);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional
                .<String> absent();
    }

    private static enum Frequency {
        weekly, monthly, daily;
        static Frequency from(String value) {
            try {
                return valueOf(value);
            } catch (Exception e) {
                log.warn("unrecognized value [{}]. Fall back to weekly.", value);
                return weekly;
            }
        }
        Days days() {
            switch (this) {
                case monthly:
                    return Days.days(30);
                case daily:
                    return Days.ONE;
                default:
                    return Weeks.ONE.toStandardDays();
            }
        }
    }
}
