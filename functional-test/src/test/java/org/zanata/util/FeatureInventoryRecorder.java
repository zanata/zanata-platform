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
        fileReport = report.canWrite();
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
