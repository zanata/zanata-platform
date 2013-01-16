package org.zanata.concordion;

import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

import lombok.extern.slf4j.Slf4j;

/**
 * This concordion extension will build index for a package.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class IndexPageBuilderExtension implements ConcordionExtension
{
   private GeneratedIndexSource generatedIndexSource;

   public IndexPageBuilderExtension()
   {
      this(null, null);
   }

   public IndexPageBuilderExtension(String descriptionHeading)
   {
      this(null, descriptionHeading);
   }

   public IndexPageBuilderExtension(Class<?> testSuiteClass)
   {
      this(testSuiteClass, null);
   }

   public IndexPageBuilderExtension(Class<?> testSuiteClass, String descriptionHeading)
   {
      generatedIndexSource = new GeneratedIndexSource(testSuiteClass, descriptionHeading);
   }

   @Override
   public void addTo(ConcordionExtender concordionExtender)
   {
      concordionExtender.withSource(generatedIndexSource);

   }

}
