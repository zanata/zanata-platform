package org.zanata.webtrans.server.rpc;

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
import org.zanata.model.HTermComment;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

@Name("webtrans.gwt.GetGlossaryDetailsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(UpdateGlossaryTermAction.class)
public class UpdateGlossaryTermHandler extends AbstractActionHandler<UpdateGlossaryTermAction, UpdateGlossaryTermResult>
{

   @Logger
   private Log log;

   @In
   private GlossaryDAO glossaryDAO;
   


   @Override
   public UpdateGlossaryTermResult execute(UpdateGlossaryTermAction action, ExecutionContext context) throws ActionException
   {
      HGlossaryEntry entry = glossaryDAO.getEntryBySrcLocaleAndContent(action.getSrcLocale(), action.getSrcContent());
      
      HGlossaryTerm targetTerm = entry.getGlossaryTerms().get(action.getTargetLocale());
      if(targetTerm == null)
      {
         throw new ActionException("Update failed for glossary term with source content: " + action.getSrcContent() + " and target locale: " + action.getTargetLocale());
      }
      else if(action.getCurrentVerNum() != targetTerm.getVersionNum())
      {
         throw new ActionException("Update failed for glossary term " + action.getTargetContent() + " base versionNum " + action.getCurrentVerNum() + " does not match current versionNum " + targetTerm.getVersionNum());
      }
      else
      {
         targetTerm.setContent(action.getTargetContent());
         HGlossaryEntry entryResult = glossaryDAO.makePersistent(entry);
         
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
         
         GlossaryDetails details = new GlossaryDetails(action.getSrcContent(), action.getTargetContent(), srcComments, targetComments, entryResult.getSourceRef(), action.getSrcLocale(), action.getTargetLocale(), targetTerm.getVersionNum());
         
         return new UpdateGlossaryTermResult(details);
      }
   }

   @Override
   public void rollback(UpdateGlossaryTermAction action, UpdateGlossaryTermResult result, ExecutionContext context) throws ActionException
   {
   }

}
