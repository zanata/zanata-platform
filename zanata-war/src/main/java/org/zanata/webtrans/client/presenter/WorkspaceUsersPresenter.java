package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.shared.model.Person;

import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{

   private ArrayList<Person> translatorList = new ArrayList<Person>();

   public interface Display extends WidgetDisplay
   {
      void clearUserList();

      void addUser(Person person);
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

   private void updateUserList()
   {
      display.clearUserList();
      for (Person p : translatorList)
      {
         display.addUser(p);
      }
   }

   public void setUserList(ArrayList<Person> users)
   {
      translatorList = users;
      updateUserList();
   }

   public void removeTranslator(Person person)
   {
      translatorList.remove(person);
      updateUserList();
   }

   public void addTranslator(Person person)
   {
      translatorList.add(person);
      updateUserList();
   }

   public int getTranslatorsSize()
   {
      return translatorList.size();
   }

}
