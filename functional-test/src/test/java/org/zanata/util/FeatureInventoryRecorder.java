/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
import org.zanata.feature.Feature;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FeatureInventoryRecorder extends RunListener {

    private boolean fileReport;
    private ThreadLocal<Feature> currentFeature = new ThreadLocal<Feature>();
    private File report;
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<FeatureEntry> entries = Lists.newArrayList();

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        String locationPath =
                System.getProperty("featureInventoryLocation",
                        "./target/feature-inventory");
        File location = new File(locationPath);
        report = new File(location, "features.json");
        location.mkdirs();
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
        Optional<Feature> featureOptional = getFeature(description);
        if (featureOptional.isPresent()) {
            currentFeature.set(featureOptional.get());
        }
    }

    /**
     * annotation on method takes precedence over annotation on class. At the
     * moment class level annotation will cause multiple entries in the result
     * if there are more than one test methods.
     */
    private static Optional<Feature> getFeature(Description description) {
        Feature testClassFeature =
                description.getTestClass().getAnnotation(Feature.class);
        Feature testMethodFeature = description.getAnnotation(Feature.class);
        return Optional.fromNullable(testMethodFeature).or(
                Optional.fromNullable(testClassFeature));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        reportFeature(FeatureEntry.TestResult.Passed,
                description.getDisplayName());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        reportFeature(FeatureEntry.TestResult.Failed, failure
                .getDescription()
                .getDisplayName());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
        Optional<Feature> featureOptional = getFeature(description);
        if (featureOptional.isPresent()) {
            currentFeature.set(featureOptional.get());
        }
        reportFeature(FeatureEntry.TestResult.Ignored,
                description.getDisplayName());
    }

    private void reportFeature(FeatureEntry.TestResult result,
            String displayName) {
        if (currentFeature.get() == null) {
            return;
        }
        Feature feature = currentFeature.get();
        FeatureEntry entry = new FeatureEntry(feature, displayName, result);
        if (fileReport) {
            entries.add(entry);

        } else {
            // display to console
            try {
                Object json =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(entry),
                                FeatureEntry.class);
                System.out.println(objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(json));
            } catch (IOException e) {
                System.out.println("Error writing as JSON");
                e.printStackTrace();
            }

        }
        currentFeature.remove();
    }
}
