package org.zanata.webtrans.server.rpc;

import java.util.Date;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.GlossaryUtil;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

import net.customware.gwt.dispatch.shared.ActionException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class UpdateGlossaryTermHandlerTest extends ZanataTest {
    @Inject @Any
    private UpdateGlossaryTermHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private GlossaryDAO glossaryDAO;
    @Produces @Mock
    private LocaleService localeServiceImpl;

    private HGlossaryEntry hGlossaryEntry;
    private HLocale targetHLocale = new HLocale(LocaleId.DE);
    private HLocale srcLocale = new HLocale(LocaleId.EN_US);

    @Before
    public void setUp() throws Exception {
        hGlossaryEntry = new HGlossaryEntry();
    }

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        Long id = 1L;
        GlossaryDetails selectedDetailEntry =
            new GlossaryDetails(id, "source", "target", "desc", "pos",
                "target comment", "sourceRef", srcLocale.getLocaleId(),
                targetHLocale.getLocaleId(), 0, new Date());

        UpdateGlossaryTermAction action =
                new UpdateGlossaryTermAction(selectedDetailEntry, "new target",
                        "new comment", "new pos", "new description");

        when(glossaryDAO.findById(id)).thenReturn(hGlossaryEntry);
        when(localeServiceImpl.getByLocaleId(selectedDetailEntry
                        .getTargetLocale())).thenReturn(targetHLocale);
        HGlossaryTerm targetTerm = new HGlossaryTerm("target");
        targetTerm.setVersionNum(0);
        targetTerm.setLastChanged(new Date());
        hGlossaryEntry.getGlossaryTerms().put(targetHLocale, targetTerm);
        hGlossaryEntry.setSrcLocale(srcLocale);
        hGlossaryEntry.getGlossaryTerms().put(srcLocale,
            new HGlossaryTerm("source")); // source term
        when(glossaryDAO.makePersistent(hGlossaryEntry)).thenReturn(
            hGlossaryEntry);

        UpdateGlossaryTermResult result = handler.execute(action, null);

        verify(identity).hasPermission("glossary-update", "");
        assertThat(targetTerm.getComment(), Matchers.equalTo("new comment"));
        assertThat(targetTerm.getContent(), Matchers.equalTo("new target"));
        verify(glossaryDAO).makePersistent(hGlossaryEntry);
        verify(glossaryDAO).flush();
        assertThat(result.getDetail().getTarget(),
                Matchers.equalTo("new target"));

    }

    @Test(expected = ActionException.class)
    @InRequestScope
    public void testExecuteWhenTargetTermNotFound() throws Exception {
        GlossaryDetails selectedDetailEntry =
            new GlossaryDetails(null, "source", "target", "desc", "pos",
                "target comment", "sourceRef", srcLocale.getLocaleId(),
                targetHLocale.getLocaleId(), 0, new Date());
        UpdateGlossaryTermAction action =
                new UpdateGlossaryTermAction(selectedDetailEntry, "new target",
                        "new comment", "new pos", "new description");
        String resId =
                GlossaryUtil.generateHash(
                    selectedDetailEntry.getSrcLocale(),
                    selectedDetailEntry.getSource(),
                    selectedDetailEntry.getPos(),
                    selectedDetailEntry.getDescription());
        when(glossaryDAO.getEntryByContentHash(resId)).thenReturn(hGlossaryEntry);
        when(localeServiceImpl.getByLocaleId(selectedDetailEntry
                        .getTargetLocale())).thenReturn(targetHLocale);
        when(glossaryDAO.makePersistent(hGlossaryEntry)).thenReturn(
                hGlossaryEntry);

        handler.execute(action, null);
    }

    @Test(expected = ActionException.class)
    @InRequestScope
    public void testExecuteWhenTargetTermVersionNotMatch() throws Exception {
        GlossaryDetails selectedDetailEntry =
            new GlossaryDetails(null, "source", "target", "desc", "pos",
                "target comment", "sourceRef", srcLocale.getLocaleId(),
                targetHLocale.getLocaleId(), 0, new Date());
        UpdateGlossaryTermAction action =
                new UpdateGlossaryTermAction(selectedDetailEntry, "new target",
                        "new comment", "new pos", "new description");
        String resId =
                GlossaryUtil.generateHash(
                        selectedDetailEntry.getSrcLocale(),
                        selectedDetailEntry.getSource(),
                        selectedDetailEntry.getPos(),
                        selectedDetailEntry.getDescription());
        when(glossaryDAO.getEntryByContentHash(resId)).thenReturn(hGlossaryEntry);
        when(localeServiceImpl.getByLocaleId(selectedDetailEntry
                .getTargetLocale())).thenReturn(targetHLocale);
        HGlossaryTerm targetTerm = new HGlossaryTerm("target");
        targetTerm.setVersionNum(1); // different version
        hGlossaryEntry.getGlossaryTerms().put(targetHLocale, targetTerm);

        handler.execute(action, null);
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
