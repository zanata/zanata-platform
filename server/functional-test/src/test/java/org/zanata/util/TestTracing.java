/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class TestTracing extends RunListener {

    private boolean fileReport;
    private ThreadLocal<Trace> currentSpecification = new ThreadLocal<>();
    private File report;
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<TraceEntry> entries = Lists.newArrayList();

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        String locationPath =
                System.getProperty("traceLocation",
                        "./target/test-classes/traceability");
        File location = new File(locationPath);
        report = new File(location, "traceability.json");
        if (!location.exists() && !location.mkdirs()) {
            throw new RuntimeException("Unable to create traceability report dir");
        }
        if (report.exists() && !report.delete()) {
            throw new RuntimeException("Unable to remove traceability report");
        }
        fileReport = report.createNewFile();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(report, entries);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        Optional<Trace> specOptional = getSpecification(description);
        if (specOptional.isPresent()) {
            currentSpecification.set(specOptional.get());
        }
    }

    /**
     * annotation on method takes precedence over annotation on class. At the
     * moment class level annotation will cause multiple entries in the result
     * if there are more than one test methods.
     */
    private static Optional<Trace> getSpecification(Description description) {
        Trace testClassTrace =
                description.getTestClass().getAnnotation(Trace.class);
        Trace testMethodTrace = description.getAnnotation(Trace.class);
        return Optional.fromNullable(testMethodTrace).or(
                Optional.fromNullable(testClassTrace));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        reportSpecification(TraceEntry.TestResult.Passed,
                description.getDisplayName());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        reportSpecification(TraceEntry.TestResult.Failed, failure
                .getDescription()
                .getDisplayName());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
        Optional<Trace> specOptional = getSpecification(description);
        if (specOptional.isPresent()) {
            currentSpecification.set(specOptional.get());
        }
        reportSpecification(TraceEntry.TestResult.Ignored,
                description.getDisplayName());
    }

    private void reportSpecification(TraceEntry.TestResult result,
                                     String displayName) {
        if (currentSpecification.get() == null) {
            return;
        }
        Trace trace = currentSpecification.get();
        TraceEntry entry = new TraceEntry(trace, displayName, result);
        if (fileReport) {
            entries.add(entry);

        } else {
            // display to console
            try {
                Object json =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(entry),
                                TraceEntry.class);
                System.out.println(objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(json));
            } catch (IOException e) {
                System.out.println("Error writing as JSON");
                e.printStackTrace();
            }

        }
        currentSpecification.remove();
    }
}
