package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.shared.model.Person;

import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      void clearUserList();

      void addUser(String name);
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus)
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
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public void setUserList(ArrayList<Person> users)
   {
      display.clearUserList();
      for (Person p : users)
      {
         display.addUser(p.getName());
      }
   }

}
