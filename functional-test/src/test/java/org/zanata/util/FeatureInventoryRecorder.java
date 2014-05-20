package org.zanata.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.zanata.feature.Feature;
import com.google.common.base.Optional;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FeatureInventoryRecorder extends RunListener {

    public static final String PLAIN_FORMAT =
            "************** rhbz%d - %s -> [%s][%s]";
    private boolean fileReport;
    private ThreadLocal<Feature> currentFeature = new ThreadLocal<Feature>();
    private PrintWriter printWriter;
    private HTMLFeatureReporter htmlFeatureReporter = new HTMLFeatureReporter();

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        String locationPath =
                System.getProperty("featureInventoryLocation",
                        "./target/feature-inventory");
        File location = new File(locationPath);
        File report = new File(location, "zanata-features.html");
        if (location.mkdirs()) {
            try {
                // may just output csv file and let another script to do the
                // transformation
                printWriter = new PrintWriter(new FileWriter(report, true));
                printWriter.append("<html>");
                printWriter
                        .append("<head><title>Zanata Features</title></head>");
                printWriter.append("<body><h3>Zanata Features</h3>");
                printWriter.append("<hr>");
                printWriter.append("<ul>");
                fileReport = true;
            } catch (IOException e) {
                System.out
                        .println("can not create feature report at " + report);
            }
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        if (fileReport) {
            printWriter.append("</body></html>");
        }
        printWriter.close();
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
        reportFeature(FeatureReporter.TestResult.Passed,
                description.getDisplayName());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        reportFeature(FeatureReporter.TestResult.Failed, failure
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
        reportFeature(FeatureReporter.TestResult.Ignored,
                description.getDisplayName());
    }

    private void reportFeature(FeatureReporter.TestResult result,
            String displayName) {
        if (currentFeature.get() == null) {
            return;
        }
        Feature feature = currentFeature.get();
        if (fileReport) {
            printWriter.append(htmlFeatureReporter.toReportEntry(feature,
                    displayName, result));
        } else {
            String msg =
                    String.format(PLAIN_FORMAT, feature.bugzilla(),
                            feature.summary(), displayName, result);
            System.out.println(msg);
        }
        currentFeature.remove();
    }
}
