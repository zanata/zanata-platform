package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.HeaderPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.ui.ImageLabel;
import org.zanata.webtrans.shared.auth.Identity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderView extends Composite implements HeaderPresenter.Display
{
   interface HeaderPanelUiBinder extends UiBinder<Widget, HeaderView>
   {
   }

   private static HeaderPanelUiBinder uiBinder = GWT.create(HeaderPanelUiBinder.class);

   @UiField
   Image logo;

   @UiField(provided = true)
   ImageLabel projects, groups, languages, system, help, reportProblem, knownIssues, user;
   
   @Inject
   public HeaderView(Resources resources, Identity identity)
   {
      projects = new ImageLabel(resources.projects(), "Project", null);
      groups = new ImageLabel(resources.groups(), "Groups", null);
      languages = new ImageLabel(resources.languages(), "Languages", null);
      system = new ImageLabel("", "System", resources.chevronDown());
      help = new ImageLabel(resources.help(), "Help", null);
      reportProblem = new ImageLabel(resources.bug(), "Report a problem", null);
      knownIssues = new ImageLabel(resources.error(), "Known issues", null);
      user = new ImageLabel(identity.getPerson().getAvatarUrl(), identity.getPerson().getName(), resources.chevronDown());

      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public HasClickHandlers getLogo()
   {
      return logo;
   }

   @Override
   public HasClickHandlers getProjects()
   {
      return projects;
   }

   @Override
   public HasClickHandlers getGroups()
   {
      return groups;
   }

   @Override
   public HasClickHandlers getLanguages()
   {
      return languages;
   }

   @Override
   public HasClickHandlers getHelp()
   {
      return help;
   }

   @Override
   public HasClickHandlers getReportProblem()
   {
      return reportProblem;
   }

   @Override
   public HasClickHandlers getKnownIssues()
   {
      return knownIssues;
   }
}
