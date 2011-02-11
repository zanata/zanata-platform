package net.openl10n.flies.rest.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.service.ResourceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@Test(groups = { "unit-tests" })
public class ResourceUtilsTest
{
   private static final Logger log = LoggerFactory.getLogger(ResourceUtilsTest.class);

   private static ResourceUtils documentUtils = new ResourceUtils();

   @Test
   public void mergeNoTextFlows()
   {
      Runtime runtime = Runtime.getRuntime();
      log.info("total memory :" + runtime.totalMemory());
      log.info("unit tests free memory :" + runtime.freeMemory());
      List<TextFlow> from = new ArrayList<TextFlow>();
      HDocument to = new HDocument();
      boolean changed = documentUtils.transferFromTextFlows(from, to, new HashSet<String>(), 1);

      assertThat(changed, is(false));
   }

   @Test
   public void mergeTextFlowWithOneFromChange()
   {
      List<TextFlow> from = new ArrayList<TextFlow>();

      TextFlow tf1 = new TextFlow("id", LocaleId.EN, "text1");
      from.add(tf1);

      HDocument to = new HDocument();
      boolean changed = documentUtils.transferFromTextFlows(from, to, new HashSet<String>(), 1);

      assertThat(changed, is(true));
   }

   private static final String[][] urlPatterns = new String[][] { new String[] { ",my,doc,id", "/my/doc/id" }, new String[] { ",my,,doc,id", "/my//doc/id" }, new String[] { "x+y", "x y" }, };

   @DataProvider(name = "urlpatterns")
   public String[][] createUrlPatterns()
   {
      return urlPatterns;
   }

   @Test(dataProvider = "urlpatterns")
   public void decodeDocIds(String encoded, String decoded)
   {
      assertThat("Decoding " + encoded, documentUtils.decodeDocId(encoded), is(decoded));
   }

   @Test(dataProvider = "urlpatterns")
   public void encodeDocIds(String encoded, String decoded)
   {
      assertThat("Encoding " + decoded, documentUtils.encodeDocId(decoded), is(encoded));
   }

}
