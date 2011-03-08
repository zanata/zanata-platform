package net.openl10n.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.TextFlowDAO;
import net.openl10n.flies.exception.FliesServiceException;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HSimpleComment;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import net.openl10n.flies.webtrans.shared.rpc.TransMemoryDetails;
import net.openl10n.flies.webtrans.shared.rpc.TransMemoryDetailsList;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

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
      FliesIdentity.instance().checkLoggedIn();
      LocaleId locale = action.getWorkspaceId().getLocaleId();
      HLocale hLocale;
      try{
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale, action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (FliesServiceException e)
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
         String iterationName = tf.getDocument().getProjectIteration().getName();
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
