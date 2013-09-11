package org.zanata.feature;

import org.concordion.api.extension.Extension;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.zanata.concordion.IndexPageBuilderExtension;

/**
 * 
 * Top level index page.
 * 
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions(TimestampFormatterExtension.class)
public class FeatureTest
{
   @Extension
   public IndexPageBuilderExtension extension = new IndexPageBuilderExtension(null, "Zanata features");
}
