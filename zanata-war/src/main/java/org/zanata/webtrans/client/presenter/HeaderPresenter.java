package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

public class HeaderPresenter extends WidgetPresenter<HeaderPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getLogo();

      HasClickHandlers getProjects();

      HasClickHandlers getGroups();

      HasClickHandlers getLanguages();

      HasClickHandlers getHelp();

      HasClickHandlers getReportProblem();

      HasClickHandlers getKnownIssues();
   }

   private final UserWorkspaceContext userWorkspaceContext;
   private final WebTransMessages messages;

   private final static String REPORT_PROBLEM_LINK = "https://bugzilla.redhat.com/enter_bug.cgi?format=guided&amp;product=Zanata";
   private final static String KNOWN_PROBLEM_LINK = "https://bugzilla.redhat.com/buglist.cgi?product=Zanata&amp;bug_status=__open__";

   @Inject
   public HeaderPresenter(Display display, EventBus eventBus, final WebTransMessages messages, final UserWorkspaceContext userWorkspaceContext)
   {
      super(display, eventBus);

      this.messages = messages;
      this.userWorkspaceContext = userWorkspaceContext;
   }

   @Override
   protected void onBind()
   {
      display.getLogo().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.exitWorkspace();
            Application.redirectToZanataHome();
         }
      });

      display.getProjects().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.exitWorkspace();
            Application.redirectToZanataProject();

         }
      });

      display.getGroups().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.exitWorkspace();
            Application.redirectToZanataGroup();

         }
      });

      display.getLanguages().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.exitWorkspace();
            Application.redirectToZanataLanguage();

         }
      });

      display.getHelp().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.exitWorkspace();
            Application.redirectToZanataHelp();
         }
      });

      display.getReportProblem().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.openNewWindowToUrl(REPORT_PROBLEM_LINK);
         }
      });

      display.getKnownIssues().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.openNewWindowToUrl(KNOWN_PROBLEM_LINK);
         }
      });

   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

}
