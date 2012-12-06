package org.zanata.webtrans.client.presenter;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;

import static org.hamcrest.MatcherAssert.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PathDocumentFilterTest
{
   private PathDocumentFilter filter;

   @BeforeMethod
   public void setUp() throws Exception
   {
      filter = new PathDocumentFilter();
   }

   private static DocumentInfo docInfo(String name, String path)
   {
      return new DocumentInfo(new DocumentId(1L), name, path, LocaleId.EN_US, new TranslationStats());
   }

   @Test
   public void testAcceptWithCaseInsensitiveAndNotExactMatch() throws Exception
   {
      filter.setPattern("a,b b,   c   , , d");

      assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
      assertThat(filter.accept(docInfo("b b", "/pot/")), Matchers.equalTo(true));
      assertThat(filter.accept(docInfo("c", "/pot/")), Matchers.equalTo(true));
      assertThat(filter.accept(docInfo("C", "/pot/")), Matchers.equalTo(true));
      assertThat(filter.accept(docInfo("d", "/pot/")), Matchers.equalTo(true));
      assertThat(filter.accept(docInfo("b", "/pot/")), Matchers.equalTo(false));
   }

   @Test
   public void testAcceptWithCaseSensitiveAndNotExactMatch() throws Exception
   {
      filter.setPattern("a").setCaseSensitive(true);

      assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
      assertThat(filter.accept(docInfo("A", "/pot/")), Matchers.equalTo(false));
   }

   @Test
   public void testSetFullText() throws Exception
   {
      filter.setPattern("/pot/a").setFullText(true);

      assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
      assertThat(filter.accept(docInfo("a", "")), Matchers.equalTo(false));
   }

   @Test
   public void alwaysAcceptIfNoPattern()
   {
      assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
   }
}
