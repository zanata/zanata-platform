package org.zanata.feature.glossary;

import java.io.File;
import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.ClientWorkFlow;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @see <a href="https://tcms.engineering.redhat.com/case/167901/">TCMS test
 *      case 167901</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions({ ScreenshotExtension.class, TimestampFormatterExtension.class,
        CustomResourceExtension.class })
public class UnauthorizedGlossaryDeleteTest {
    @Rule
    public TestRule sampleProjectRule = new SampleProjectRule();

    private ClientWorkFlow clientWorkFlow = new ClientWorkFlow();
    private File projectRootPath;

    public String getUserConfigPath() {
        return ClientWorkFlow.getUserConfigPath("glossarist");
    }

    public String getProjectLocation(String project) {
        projectRootPath = clientWorkFlow.getProjectRootPath(project);
        return projectRootPath.getAbsolutePath();
    }

    public List<String> push(String command, String configPath)
            throws Exception {
        return clientWorkFlow.callWithTimeout(projectRootPath, command
                + configPath);
    }

    public boolean isPushFailed(List<String> output) {
        return !clientWorkFlow.isPushSuccessful(output);
    }

    public String resultByLines(List<String> output) {
        return Joiner.on("\n").join(output);
    }

    public boolean containsError(List<String> output, final String error) {
        return Iterables.tryFind(output, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.contains(error);
            }
        }).isPresent();
    }
}
