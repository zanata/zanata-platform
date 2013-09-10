package org.zanata.webtrans.client.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.model.TestFixture.*;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class GetTransUnitActionContextTest
{
   private static final boolean NEEDED = true;
   private static final boolean NO_NEED = false;
   private GetTransUnitActionContext context;

   @BeforeMethod
   public void setUp() throws Exception
   {
      context = new GetTransUnitActionContext(documentInfo(1, ""));
   }

   @Test
   public void testChangeDocument() throws Exception
   {
      DocumentInfo document = context.getDocument();
      GetTransUnitActionContext newContext = context.changeDocument(documentInfo(99, ""));

      assertThat(context.getDocument(), Matchers.sameInstance(document));
      assertThat(newContext.getDocument(), Matchers.equalTo(documentInfo(99, "")));
   }

   @Test
   public void testChangeFindMessage() throws Exception
   {
      String findMessage = context.getFindMessage();
      GetTransUnitActionContext result = context.changeFindMessage("new find message");

      assertThat(context.getFindMessage(), Matchers.sameInstance(findMessage));
      assertThat(result.getFindMessage(), Matchers.equalTo("new find message"));
   }

   @Test
   public void testChangeFilterTranslated() throws Exception
   {
      GetTransUnitActionContext result = context.changeFilterTranslated(true);

      assertThat(context.isFilterTranslated(), Matchers.is(false));
      assertThat(result.isFilterTranslated(), Matchers.is(true));
   }

   @Test
   public void testChangeFilterNeedReview() throws Exception
   {
      GetTransUnitActionContext result = context.changeFilterFuzzy(true);

      assertThat(context.isFilterFuzzy(), Matchers.is(false));
      assertThat(result.isFilterFuzzy(), Matchers.is(true));
   }

   @Test
   public void testChangeFilterUntranslated() throws Exception
   {
      GetTransUnitActionContext result = context.changeFilterUntranslated(true);

      assertThat(context.isFilterUntranslated(), Matchers.is(false));
      assertThat(result.isFilterUntranslated(), Matchers.is(true));
   }
   
   @Test
   public void testChangeFilterApproved() throws Exception
   {
      GetTransUnitActionContext result = context.changeFilterApproved(true);

      assertThat(context.isFilterApproved(), Matchers.is(false));
      assertThat(result.isFilterApproved(), Matchers.is(true));
   }
   
   @Test
   public void testChangeFilterRejected() throws Exception
   {
      GetTransUnitActionContext result = context.changeFilterRejected(true);

      assertThat(context.isFilterRejected(), Matchers.is(false));
      assertThat(result.isFilterRejected(), Matchers.is(true));
   }

   @Test
   public void testNeedReloadList() throws Exception
   {
      verifyNeedReloadTransUnits(context, context.changeFilterFuzzy(true), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeFilterUntranslated(true), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeFilterTranslated(true), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeFilterApproved(true), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeFilterRejected(true), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeDocument(documentInfo(99, "")), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeFindMessage("find message"), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeCount(2), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeOffset(2), NEEDED);
      verifyNeedReloadTransUnits(context, context.changeTargetTransUnitId(new TransUnitId(2)), NO_NEED);
   }

   private static void verifyNeedReloadTransUnits(GetTransUnitActionContext context, GetTransUnitActionContext newContext, boolean needed)
   {
      boolean result = context.needReloadList(newContext);
      assertThat(result, Matchers.is(needed));
   }

   @Test
   public void testNeedReloadNavigationIndex() throws Exception
   {
      verifyNeedReloadNavigationIndex(context, context.changeFilterFuzzy(true), NEEDED);
      verifyNeedReloadNavigationIndex(context, context.changeFilterUntranslated(true), NEEDED);
      verifyNeedReloadNavigationIndex(context, context.changeFilterTranslated(true), NEEDED);
      verifyNeedReloadNavigationIndex(context, context.changeFilterApproved(true), NEEDED);
      verifyNeedReloadNavigationIndex(context, context.changeFilterRejected(true), NEEDED);
      verifyNeedReloadNavigationIndex(context, context.changeDocument(documentInfo(99, "")), NEEDED);
      verifyNeedReloadNavigationIndex(context, context.changeFindMessage("find message"), NEEDED);

      verifyNeedReloadNavigationIndex(context, context.changeCount(2), NO_NEED);
      verifyNeedReloadNavigationIndex(context, context.changeOffset(2), NEEDED);
      verifyNeedReloadNavigationIndex(context, context.changeTargetTransUnitId(new TransUnitId(2)), NO_NEED);
   }

   private static void verifyNeedReloadNavigationIndex(GetTransUnitActionContext context, GetTransUnitActionContext newContext, boolean needed)
   {
      boolean result = context.needReloadNavigationIndex(newContext);
      assertThat(result, Matchers.is(needed));
   }
}
