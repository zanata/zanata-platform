package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.client.editor.filter.TransFilterPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanelPresenter extends WidgetPresenter<SidePanelPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      void setFilterView(Widget widget);

      void setWorkspaceUsersView(Widget widget);

      void setTransUnitDetailView(Widget widget);

      void collapseUsersPanel();

      void expandUsersPanel();
   }

   private final TransFilterPresenter transFilterPresenter;
   private final WorkspaceUsersPresenter workspaceUsersPresenter;
   private final TransUnitDetailsPresenter transUnitDetailsPresenter;

   @Inject
   public SidePanelPresenter(final Display display, final EventBus eventBus, final TransUnitDetailsPresenter transUnitDetailsPresenter, final WorkspaceUsersPresenter workspaceUsersPresenter, final TransFilterPresenter transFilterPresenter)
   {
      super(display, eventBus);
      this.workspaceUsersPresenter = workspaceUsersPresenter;
      this.transFilterPresenter = transFilterPresenter;
      this.transUnitDetailsPresenter = transUnitDetailsPresenter;
   }

   @Override
   public Place getPlace()
   {
      return null;
   }

   @Override
   protected void onBind()
   {

      transUnitDetailsPresenter.bind();
      display.setTransUnitDetailView(transUnitDetailsPresenter.getDisplay().asWidget());

      workspaceUsersPresenter.bind();
      display.setWorkspaceUsersView(workspaceUsersPresenter.getDisplay().asWidget());

      transFilterPresenter.bind();
      display.setFilterView(transFilterPresenter.getDisplay().asWidget());
   }

   @Override
   protected void onPlaceRequest(PlaceRequest request)
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void refreshDisplay()
   {
   }

   @Override
   public void revealDisplay()
   {
   }

}
