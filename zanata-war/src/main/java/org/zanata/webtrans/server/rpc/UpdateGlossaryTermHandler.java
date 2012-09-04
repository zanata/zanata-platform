package org.zanata.webtrans.server.rpc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HTermComment;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

@Name("webtrans.gwt.UpdateGlossaryTermHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(UpdateGlossaryTermAction.class)
public class UpdateGlossaryTermHandler extends AbstractActionHandler<UpdateGlossaryTermAction, UpdateGlossaryTermResult>
{

   @Logger
   private Log log;

   @In
   private GlossaryDAO glossaryDAO;
   
   @In
   private LocaleService localeServiceImpl;


   @Override
   public UpdateGlossaryTermResult execute(UpdateGlossaryTermAction action, ExecutionContext context) throws ActionException
   {
      HGlossaryEntry entry = glossaryDAO.getEntryBySrcLocaleAndContent(action.getSrcLocale(), action.getSrcContent());

      HLocale targetLocale = localeServiceImpl.getByLocaleId(action.getTargetLocale());

      HGlossaryTerm targetTerm = entry.getGlossaryTerms().get(targetLocale);
      if(targetTerm == null)
      {
         throw new ActionException("Update failed for glossary term with source content: " + action.getSrcContent() + " and target locale: " + action.getTargetLocale());
      }
      else if (action.getCurrentVerNum().compareTo(targetTerm.getVersionNum()) != 0)
      {
         throw new ActionException("Update failed for glossary term " + action.getTargetContent() + " base versionNum " + action.getCurrentVerNum() + " does not match current versionNum " + targetTerm.getVersionNum());
      }
      else
      {
         targetTerm.setContent(action.getTargetContent());
         HGlossaryEntry entryResult = glossaryDAO.makePersistent(entry);
         glossaryDAO.flush();

         ArrayList<String> srcComments = new ArrayList<String>();
         ArrayList<String> targetComments = new ArrayList<String>();

         for(HTermComment termComment: entryResult.getGlossaryTerms().get(entryResult.getSrcLocale()).getComments())
         {
            srcComments.add(termComment.getComment());
         }
         
         for(HTermComment termComment: targetTerm.getComments())
         {
            targetComments.add(termComment.getComment());
         }
         
         SimpleDateFormat dateFormat = new SimpleDateFormat();

         GlossaryDetails details = new GlossaryDetails(entryResult.getGlossaryTerms().get(entryResult.getSrcLocale()).getContent(), entryResult.getGlossaryTerms().get(targetLocale).getContent(), srcComments, targetComments, entryResult.getSourceRef(), action.getSrcLocale(), action.getTargetLocale(), targetTerm.getVersionNum(), dateFormat.format(targetTerm.getLastChanged()));
         
         return new UpdateGlossaryTermResult(details);
      }
   }

   @Override
   public void rollback(UpdateGlossaryTermAction action, UpdateGlossaryTermResult result, ExecutionContext context) throws ActionException
   {
   }

}
