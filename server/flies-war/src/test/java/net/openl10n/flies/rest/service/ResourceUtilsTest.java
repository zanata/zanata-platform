package net.openl10n.flies.rest.service;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HSimpleComment;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.model.po.HPoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader;
import net.openl10n.flies.rest.dto.resource.TextFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = { "unit-tests" })
public class ResourceUtilsTest
{
   private static final Logger log = LoggerFactory.getLogger(ResourceUtilsTest.class);

   private static ResourceUtils resourceUtils = new ResourceUtils();

   @Test
   public void mergeNoTextFlows()
   {
      Runtime runtime = Runtime.getRuntime();
      log.info("total memory :" + runtime.totalMemory());
      log.info("unit tests free memory :" + runtime.freeMemory());
      List<TextFlow> from = new ArrayList<TextFlow>();
      HDocument to = new HDocument();
      boolean changed = resourceUtils.transferFromTextFlows(from, to, new HashSet<String>(), 1);

      assertThat(changed, is(false));
   }

   @Test
   public void mergeTextFlowWithOneFromChange()
   {
      List<TextFlow> from = new ArrayList<TextFlow>();

      TextFlow tf1 = new TextFlow("id", LocaleId.EN, "text1");
      from.add(tf1);

      HDocument to = new HDocument();
      boolean changed = resourceUtils.transferFromTextFlows(from, to, new HashSet<String>(), 1);

      assertThat(changed, is(true));
   }

   @Test
   public void pushCommentInitialImport()
   {
      PoTargetHeader fromHeader = new PoTargetHeader();
      String comment = "comment to import\nsecond line";
      fromHeader.setComment(comment);
      HPoTargetHeader toHeader = new HPoTargetHeader();
      resourceUtils.pushPoTargetComment(fromHeader, toHeader, MergeType.AUTO);
      assertThat("", toHeader.getComment().getComment(), is(comment));
   }
   
   @Test
   public void pushCommentSkip()
   {
      PoTargetHeader fromHeader = new PoTargetHeader();
      String comment = "comment to import\nskip this #zanata\nlast line";
      String expectedComment = "comment to import\nlast line";
      fromHeader.setComment(comment);
      HPoTargetHeader toHeader = new HPoTargetHeader();
      resourceUtils.pushPoTargetComment(fromHeader, toHeader, MergeType.IMPORT);
      assertThat("", toHeader.getComment().getComment(), is(expectedComment));
   }
   
   @Test
   public void pushCommentMerge()
   {
      PoTargetHeader fromHeader = new PoTargetHeader();
      String importedComment = "initial comment\nAlice #zanata\nCharlie";
      String expectedComment = "initial comment\nBob\nCharlie";
      fromHeader.setComment(importedComment);
      HPoTargetHeader toHeader = new HPoTargetHeader();
      toHeader.setComment(new HSimpleComment("initial comment\nBob"));
      resourceUtils.pushPoTargetComment(fromHeader, toHeader, MergeType.AUTO);
      assertThat("", toHeader.getComment().getComment(), is(expectedComment));
   }
   
   @Test
   public void pushCommentImport()
   {
      PoTargetHeader fromHeader = new PoTargetHeader();
      String importedComment = "initial comment\nAlice #zanata\nCharlie";
      String expectedComment = "initial comment\nCharlie";
      fromHeader.setComment(importedComment);
      HPoTargetHeader toHeader = new HPoTargetHeader();
      toHeader.setComment(new HSimpleComment("initial comment\nBob"));
      resourceUtils.pushPoTargetComment(fromHeader, toHeader, MergeType.IMPORT);
      assertThat("", toHeader.getComment().getComment(), is(expectedComment));
   }
   
   @Test
   public void pullCommentEmpty()
   {
      
      HPoTargetHeader fromHeader = new HPoTargetHeader();
      PoTargetHeader toHeader = new PoTargetHeader();
      
      List<HTextFlowTarget> hTargets = Collections.emptyList();
      resourceUtils.pullPoTargetComment(fromHeader, toHeader, hTargets );
      
      assertThat("", toHeader.getComment(), is(""));
   }
   
   @Test
   public void pullCommentInitial()
   {
      
      HPoTargetHeader fromHeader = new HPoTargetHeader();
      fromHeader.setComment(new HSimpleComment("initial comment"));
      String expectedComment = "initial comment";
      PoTargetHeader toHeader = new PoTargetHeader();
      
      List<HTextFlowTarget> hTargets = Collections.emptyList();
      resourceUtils.pullPoTargetComment(fromHeader, toHeader, hTargets );
      
      assertThat("", toHeader.getComment(), is(expectedComment));
   }
   
   @Test
   public void pullCommentWithCredits()
   {
      
      HPoTargetHeader fromHeader = new HPoTargetHeader();
      fromHeader.setComment(new HSimpleComment("initial comment"));
      String expectedComment = "initial comment\n" +
            "Alice <alice@example.org>, 2011. #zanata\n" +
      		"Alice <alice@example.org>, 2010. #zanata";
      PoTargetHeader toHeader = new PoTargetHeader();
      
      HPerson alice = new HPerson();
      alice.setName("Alice");
      alice.setEmail("alice@example.org");
      List<HTextFlowTarget> hTargets = new ArrayList<HTextFlowTarget>();
      HTextFlowTarget tft1 = new HTextFlowTarget();
      tft1.setLastChanged(new Date(1302671654000L)); // 13 Apr 2011
      tft1.setLastModifiedBy(alice);
      hTargets.add(tft1);

      HTextFlowTarget tft2 = new HTextFlowTarget();
      tft2.setLastChanged(new Date(1304329523000L)); // 2 May 2011
      tft2.setLastModifiedBy(alice);
      hTargets.add(tft2);
      
      HTextFlowTarget tft3 = new HTextFlowTarget();
      tft3.setLastChanged(new Date(1262419384000L)); // 2 Jan 2010
      tft3.setLastModifiedBy(alice);
      hTargets.add(tft3);
      
      resourceUtils.pullPoTargetComment(fromHeader, toHeader, hTargets );
      
      assertThat("", toHeader.getComment(), is(expectedComment));
   }
   
   @Test
   public void splitLinesSimple()
   {
      String s = "1\n2\n3";
      List<String>  expected = Arrays.asList("1", "2", "3");
      List<String> lines = ResourceUtils.splitLines(s, null);
      assertThat("", lines, is(expected));
   }

   @Test
   public void splitLinesEmpty()
   {
      String s = "";
      List<String>  expected = Collections.emptyList();
      List<String> lines = ResourceUtils.splitLines(s, null);
      assertThat("", lines, is(expected));
   }
   
   @Test
   public void splitLinesSkip()
   {
      String s = "1\n2 #zanata\n3";
      List<String>  expected = Arrays.asList("1", "3");
      List<String> lines = ResourceUtils.splitLines(s, "#zanata");
      assertThat("", lines, is(expected));
   }
   
   @Test
   public void splitLinesSkipAll()
   {
      String s = "1 #zanata\n2 #zanata\n3 #zanata";
      List<String>  expected = Collections.emptyList();
      List<String> lines = ResourceUtils.splitLines(s, "#zanata");
      assertThat("", lines, is(expected));
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
      assertThat("Decoding " + encoded, resourceUtils.decodeDocId(encoded), is(decoded));
   }

   @Test(dataProvider = "urlpatterns")
   public void encodeDocIds(String encoded, String decoded)
   {
      assertThat("Encoding " + decoded, resourceUtils.encodeDocId(decoded), is(encoded));
   }

}
