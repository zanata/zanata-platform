package org.zanata.webtrans.server.rpc;

import java.util.Date;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.shared.ActionException;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class UpdateGlossaryTermHandlerTest
{
   private UpdateGlossaryTermHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private GlossaryDAO glossaryDAO;
   @Mock
   private LocaleService localeServiceImpl;
   private HGlossaryEntry hGlossaryEntry;
   private HLocale targetHLocale = new HLocale(LocaleId.DE);
   private HLocale srcLocale = new HLocale(LocaleId.EN_US);

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("glossaryDAO", glossaryDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .ignoreNonResolvable()
            .autowire(UpdateGlossaryTermHandler.class);
      // @formatter:on
      hGlossaryEntry = new HGlossaryEntry();
   }

   @Test
   public void testExecute() throws Exception
   {
      GlossaryDetails selectedDetailEntry = new GlossaryDetails("source", "target", null, null, "sourceRef", srcLocale.getLocaleId(), targetHLocale.getLocaleId(), 0, "12/10/2012");
      UpdateGlossaryTermAction action = new UpdateGlossaryTermAction(selectedDetailEntry, "new target", Lists.newArrayList("new comment"));
      when(glossaryDAO.getEntryBySrcLocaleAndContent(selectedDetailEntry.getSrcLocale(), selectedDetailEntry.getSource())).thenReturn(hGlossaryEntry);
      when(localeServiceImpl.getByLocaleId(selectedDetailEntry.getTargetLocale())).thenReturn(targetHLocale);
      HGlossaryTerm targetTerm = new HGlossaryTerm("target");
      targetTerm.setVersionNum(0);
      targetTerm.setLastChanged(new Date());
      hGlossaryEntry.getGlossaryTerms().put(targetHLocale, targetTerm);
      hGlossaryEntry.setSrcLocale(srcLocale);
      hGlossaryEntry.getGlossaryTerms().put(srcLocale, new HGlossaryTerm("source")); //source term
      when(glossaryDAO.makePersistent(hGlossaryEntry)).thenReturn(hGlossaryEntry);

      UpdateGlossaryTermResult result = handler.execute(action, null);

      verify(identity).checkLoggedIn();
      assertThat(targetTerm.getComments(), Matchers.hasSize(1));
      assertThat(targetTerm.getComments().get(0).getComment(), Matchers.equalTo("new comment"));
      assertThat(targetTerm.getContent(), Matchers.equalTo("new target"));
      verify(glossaryDAO).makePersistent(hGlossaryEntry);
      verify(glossaryDAO).flush();
      assertThat(result.getDetail().getTarget(), Matchers.equalTo("new target"));

   }

   @Test(expectedExceptions = ActionException.class)
   public void testExecuteWhenTargetTermNotFound() throws Exception
   {
      GlossaryDetails selectedDetailEntry = new GlossaryDetails("source", "target", null, null, "sourceRef", srcLocale.getLocaleId(), targetHLocale.getLocaleId(), 0, "12/10/2012");
      UpdateGlossaryTermAction action = new UpdateGlossaryTermAction(selectedDetailEntry, "new target", Lists.newArrayList("new comment"));
      when(glossaryDAO.getEntryBySrcLocaleAndContent(selectedDetailEntry.getSrcLocale(), selectedDetailEntry.getSource())).thenReturn(hGlossaryEntry);
      when(localeServiceImpl.getByLocaleId(selectedDetailEntry.getTargetLocale())).thenReturn(targetHLocale);
      when(glossaryDAO.makePersistent(hGlossaryEntry)).thenReturn(hGlossaryEntry);

      handler.execute(action, null);
   }

   @Test(expectedExceptions = ActionException.class)
   public void testExecuteWhenTargetTermVersionNotMatch() throws Exception
   {
      GlossaryDetails selectedDetailEntry = new GlossaryDetails("source", "target", null, null, "sourceRef", srcLocale.getLocaleId(), targetHLocale.getLocaleId(), 0, "12/10/2012");
      UpdateGlossaryTermAction action = new UpdateGlossaryTermAction(selectedDetailEntry, "new target", Lists.newArrayList("new comment"));
      when(glossaryDAO.getEntryBySrcLocaleAndContent(selectedDetailEntry.getSrcLocale(), selectedDetailEntry.getSource())).thenReturn(hGlossaryEntry);
      when(localeServiceImpl.getByLocaleId(selectedDetailEntry.getTargetLocale())).thenReturn(targetHLocale);
      HGlossaryTerm targetTerm = new HGlossaryTerm("target");
      targetTerm.setVersionNum(1); //different version
      hGlossaryEntry.getGlossaryTerms().put(targetHLocale, targetTerm);

      handler.execute(action, null);
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
