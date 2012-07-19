package org.zanata.webtrans.server.rpc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("webtrans.gwt.GetTranslationHistoryHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTranslationHistoryAction.class)
@Slf4j
public class GetTranslationHistoryHandler extends AbstractActionHandler<GetTranslationHistoryAction, GetTranslationHistoryResult>
{
   @In
   ZanataIdentity identity;

   @In
   LocaleService localeServiceImpl;

   @In
   TextFlowDAO textFlowDAO;

   @Override
   public GetTranslationHistoryResult execute(GetTranslationHistoryAction action, ExecutionContext context) throws ActionException
   {
      identity.checkLoggedIn();
      log.debug("get translation history for text flow id {}", action.getTransUnitId());

      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e);
      }

      HTextFlow hTextFlow = textFlowDAO.findById(action.getTransUnitId().getId(), false);

      HTextFlowTarget hTextFlowTarget = hTextFlow.getTargets().get(hLocale.getId());
      Map<Integer,HTextFlowTargetHistory> history = Maps.newHashMap();
      if (hTextFlowTarget != null)
      {
         history = hTextFlowTarget.getHistory();
      }

      Collection<TransHistoryItem> historyItems = Collections2.transform(history.values(), new TargetHistoryToTransHistoryItemFunction());
      log.info("found {} history for text flow id {}", historyItems.size(), action.getTransUnitId());
      return new GetTranslationHistoryResult(historyItems);
   }

   @Override
   public void rollback(GetTranslationHistoryAction action, GetTranslationHistoryResult result, ExecutionContext context) throws ActionException
   {
   }

   private static class TargetHistoryToTransHistoryItemFunction implements Function<HTextFlowTargetHistory, TransHistoryItem>
   {
      private final SimpleDateFormat dateFormat;

      public TargetHistoryToTransHistoryItemFunction()
      {
         this.dateFormat = new SimpleDateFormat();
      }

      @Override
      public TransHistoryItem apply(HTextFlowTargetHistory targetHistory)
      {
         //targetHistory.getContents will return Hibernate persistentList which RPC can't handle
         ArrayList<String> contents = Lists.newArrayList(targetHistory.getContents());
         return new TransHistoryItem(targetHistory.getVersionNum(), contents, targetHistory.getState(), targetHistory.getLastModifiedBy().getName(), dateFormat.format(targetHistory.getLastChanged()));
      }
   }
}
