package org.zanata.concordion;

import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

import lombok.extern.slf4j.Slf4j;

/**
 * This concordion extension will build index page for a package.
 * Note: If you want concordion breadcrumb to work, you still need to put an empty html spec file.
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

   /**
    *
    * @param testSuiteClass if you use test suite to control the test run, then specify it here. If null will list spec files under the package in natural order.
    * @param descriptionHeading description text that will appear in the generated index page. If null will use generated title from package name.
    */
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
