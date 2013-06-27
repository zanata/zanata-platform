package org.zanata.webtrans.client.service;

import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class GetTransUnitActionContextHolder
{
   private GetTransUnitActionContext context;
   private UserConfigHolder configHolder;

   @Inject
   public GetTransUnitActionContextHolder(UserConfigHolder configHolder)
   {
      this.configHolder = configHolder;
   }

   protected boolean isContextInitialized()
   {
      return context != null;
   }

   protected GetTransUnitActionContext initContext(DocumentInfo document, String findMessage, TransUnitId targetTransUnitId)
   {
      // @formatter:off
      context = new GetTransUnitActionContext(document)
            .changeCount(configHolder.getState().getEditorPageSize())
            .changeFindMessage(findMessage)
            .changeFilterNeedReview(configHolder.getState().isFilterByNeedReview())
            .changeFilterTranslated(configHolder.getState().isFilterByTranslated())
            .changeFilterUntranslated(configHolder.getState().isFilterByUntranslated())
            .changeFilterApproved(configHolder.getState().isFilterByApproved())
            .changeFilterRejected(configHolder.getState().isFilterByRejected())
            .changeFilterHasError(configHolder.getState().isFilterByHasError())
            .changeValidationIds(configHolder.getState().getEnabledValidationIds())
            .changeTargetTransUnitId(targetTransUnitId);
      // @formatter:on

      return context;
   }

   public GetTransUnitActionContext getContext()
   {
      return context;
   }

   public GetTransUnitActionContext changeOffset(int targetOffset)
   {
      context = context.changeOffset(targetOffset);
      return context;
   }

   public GetTransUnitActionContext changeTargetTransUnitId(TransUnitId transUnitId)
   {
      context = context.changeTargetTransUnitId(transUnitId);
      return context;
   }

   public GetTransUnitActionContext updateContext(GetTransUnitActionContext newContext)
   {
      context = newContext;
      return context;
   }
}
