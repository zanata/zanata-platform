package org.zanata.webtrans.server.rpc;

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
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.zanata.webtrans.shared.rpc.TransMemoryDetails;
import org.zanata.webtrans.shared.rpc.TransMemoryDetailsList;

@Name("webtrans.gwt.GetTransMemoryDetailsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransMemoryDetailsAction.class)
public class GetTransMemoryDetailsHandler extends AbstractActionHandler<GetTransMemoryDetailsAction, TransMemoryDetailsList>
{

   @Logger
   private Log log;

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   @Override
   public TransMemoryDetailsList execute(GetTransMemoryDetailsAction action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      LocaleId locale = action.getWorkspaceId().getLocaleId();
      HLocale hLocale;
      try{
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale, action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e.getMessage());
      }

      ArrayList<Long> textFlowIds = action.getTransUnitIdList();
      log.info("Fetching TM details for TFs {0} in locale {1}", textFlowIds, locale);
      List<HTextFlow> textFlows = textFlowDAO.findByIdList(textFlowIds);
      ArrayList<TransMemoryDetails> items = new ArrayList<TransMemoryDetails>(textFlows.size());

      for (HTextFlow tf : textFlows)
      {
         HTextFlowTarget tft = tf.getTargets().get(hLocale);
         HSimpleComment sourceComment = tf.getComment();
         HSimpleComment targetComment = tft.getComment();
         String docId = tf.getDocument().getDocId();
         String iterationName = tf.getDocument().getProjectIteration().getSlug();
         String projectName = tf.getDocument().getProjectIteration().getProject().getName();
         items.add(new TransMemoryDetails(HSimpleComment.toString(sourceComment), HSimpleComment.toString(targetComment), projectName, iterationName, docId));
      }

      log.info("Returning {0} TM details", items.size());
      return new TransMemoryDetailsList(items);
   }

   @Override
   public void rollback(GetTransMemoryDetailsAction action, TransMemoryDetailsList result, ExecutionContext context) throws ActionException
   {
   }

}
