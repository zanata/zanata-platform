package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HTermComment;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

@Named("webtrans.gwt.UpdateGlossaryTermHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(UpdateGlossaryTermAction.class)
public class UpdateGlossaryTermHandler
        extends
        AbstractActionHandler<UpdateGlossaryTermAction, UpdateGlossaryTermResult> {
    @Inject
    private ZanataIdentity identity;

    @Inject
    private GlossaryDAO glossaryDAO;

    @Inject
    private LocaleService localeServiceImpl;

    @Override
    public UpdateGlossaryTermResult execute(UpdateGlossaryTermAction action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();

        GlossaryDetails selectedDetailEntry = action.getSelectedDetailEntry();

        HGlossaryEntry entry =
                glossaryDAO.getEntryBySrcLocaleAndContent(
                        selectedDetailEntry.getSrcLocale(),
                        selectedDetailEntry.getSource());

        HLocale targetLocale =
                localeServiceImpl.getByLocaleId(selectedDetailEntry
                        .getTargetLocale());

        HGlossaryTerm targetTerm = entry.getGlossaryTerms().get(targetLocale);
        if (targetTerm == null) {
            throw new ActionException(
                    "Update failed for glossary term with source content: "
                            + selectedDetailEntry.getSrcLocale()
                            + " and target locale: "
                            + selectedDetailEntry.getTargetLocale());
        } else if (selectedDetailEntry.getTargetVersionNum().compareTo(
                targetTerm.getVersionNum()) != 0) {
            throw new ActionException("Update failed for glossary term "
                    + selectedDetailEntry.getTarget() + " base versionNum "
                    + selectedDetailEntry.getTargetVersionNum()
                    + " does not match current versionNum "
                    + targetTerm.getVersionNum());
        } else {
            targetTerm.setContent(action.getNewTargetTerm());
            targetTerm.getComments().clear();

            for (String newComment : action.getNewTargetComment()) {
                targetTerm.getComments().add(new HTermComment(newComment));
            }

            HGlossaryEntry entryResult = glossaryDAO.makePersistent(entry);
            glossaryDAO.flush();

            ArrayList<String> srcComments = new ArrayList<String>();
            ArrayList<String> targetComments = new ArrayList<String>();

            for (HTermComment termComment : entryResult.getGlossaryTerms()
                    .get(entryResult.getSrcLocale()).getComments()) {
                srcComments.add(termComment.getComment());
            }

            for (HTermComment termComment : targetTerm.getComments()) {
                targetComments.add(termComment.getComment());
            }

            GlossaryDetails details =
                    new GlossaryDetails(entryResult.getGlossaryTerms()
                            .get(entryResult.getSrcLocale()).getContent(),
                            entryResult.getGlossaryTerms().get(targetLocale)
                                    .getContent(), srcComments, targetComments,
                            entryResult.getSourceRef(),
                            selectedDetailEntry.getSrcLocale(),
                            selectedDetailEntry.getTargetLocale(),
                            targetTerm.getVersionNum(),
                            targetTerm.getLastChanged());

            return new UpdateGlossaryTermResult(details);
        }
    }

    @Override
    public void rollback(UpdateGlossaryTermAction action,
            UpdateGlossaryTermResult result, ExecutionContext context)
            throws ActionException {
    }

}
