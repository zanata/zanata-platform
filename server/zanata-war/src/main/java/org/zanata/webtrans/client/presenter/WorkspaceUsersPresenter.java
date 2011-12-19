package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{


   public interface Display extends WidgetDisplay
   {
      String updateUserList(ArrayList<Person> userList);
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus, WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
   }

   @Override
   protected void onBind()
   {
   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

}
