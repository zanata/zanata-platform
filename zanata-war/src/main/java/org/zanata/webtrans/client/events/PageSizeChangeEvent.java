package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationService;
import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.GwtEvent;

public class PageSizeChangeEvent extends GwtEvent<PageSizeChangeEventHandler> implements NavigationService.UpdateContextCommand
{
   public static Type<PageSizeChangeEventHandler> TYPE = new Type<PageSizeChangeEventHandler>();

   private int pageSize;

   public PageSizeChangeEvent(int pageSize)
   {
      this.pageSize = pageSize;
   }

   public Type<PageSizeChangeEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(PageSizeChangeEventHandler handler)
   {
      handler.onPageSizeChange(this);
   }

   @Override
   public GetTransUnitActionContext updateContext(GetTransUnitActionContext currentContext)
   {
      Preconditions.checkNotNull(currentContext, "current context can not be null");
      return currentContext.changeCount(pageSize);
   }

   public int getPageSize()
   {
      return pageSize;
   }
}
