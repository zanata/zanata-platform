package org.zanata.feature.startNewProject;

import org.concordion.api.extension.Extension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.zanata.concordion.IndexPageBuilderExtension;

import lombok.extern.slf4j.Slf4j;

/**
 * This is the index page for startNewProject package.
 *
 * @see CreateSampleProjectTestSuite
 * @see IndexPageBuilderExtension
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
public class StartNewProjectTest
{
   @Extension
   public IndexPageBuilderExtension extension = new IndexPageBuilderExtension(CreateSampleProjectTestSuite.class, "Example steps to start using Zanata for translating a sample project");
}
