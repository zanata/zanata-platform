package org.zanata.webtrans.server.rpc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HTermComment;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;

@Name("webtrans.gwt.GetGlossaryDetailsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetGlossaryDetailsAction.class)
public class GetGlossaryDetailsHandler extends AbstractActionHandler<GetGlossaryDetailsAction, GetGlossaryDetailsResult>
{

   @Logger
   private Log log;

   @In
   private GlossaryDAO glossaryDAO;

   @In
   private LocaleService localeServiceImpl;

   @Override
   public GetGlossaryDetailsResult execute(GetGlossaryDetailsAction action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      LocaleId locale = action.getWorkspaceId().getLocaleId();
      HLocale hLocale;
      try{
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale, action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e);
      }
      ArrayList<Long> sourceIds = action.getSourceIdList();
      
      
      log.info("Fetching glossary details for entry{0} in locale {1}", sourceIds, hLocale);
      List<HGlossaryTerm> srcTerms = glossaryDAO.findByIdList(sourceIds);
      ArrayList<GlossaryDetails> items = new ArrayList<GlossaryDetails>(srcTerms.size());
      
      for(HGlossaryTerm srcTerm: srcTerms)
      {
         HGlossaryEntry entry = srcTerm.getGlossaryEntry();
         ArrayList<String> srcComments = new ArrayList<String>();
         ArrayList<String> targetComments = new ArrayList<String>();

         for(HTermComment termComment: srcTerm.getComments())
         {
            srcComments.add(termComment.getComment());
         }
         
         for(HTermComment termComment: entry.getGlossaryTerms().get(hLocale).getComments())
         {
            targetComments.add(termComment.getComment());
         }
         
         SimpleDateFormat dateFormat = new SimpleDateFormat();
         
         items.add(new GlossaryDetails(srcTerm.getContent(), entry.getGlossaryTerms().get(hLocale).getContent(), srcComments, targetComments, entry.getSourceRef(), entry.getSrcLocale().getLocaleId(), hLocale.getLocaleId(), entry.getGlossaryTerms().get(hLocale).getVersionNum(), dateFormat.format(entry.getGlossaryTerms().get(hLocale).getLastChanged())));
      }

      return new GetGlossaryDetailsResult(items);
   }

   @Override
   public void rollback(GetGlossaryDetailsAction action, GetGlossaryDetailsResult result, ExecutionContext context) throws ActionException
   {
   }

}
