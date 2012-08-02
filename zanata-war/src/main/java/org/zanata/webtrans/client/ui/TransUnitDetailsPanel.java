package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitDetailsPanel extends Composite
{
   @UiField
   Label headerLabel;

   @UiField
   LayoutPanel contentPanel;

   @UiField
   HorizontalPanel msgContextPanel;

   private static TransUnitDetailsPanelUiBinder uiBinder = GWT.create(TransUnitDetailsPanelUiBinder.class);
   private final TableEditorMessages messages;

   interface TransUnitDetailsPanelUiBinder extends UiBinder<Widget, TransUnitDetailsPanel>
   {
   }

   @UiField
   Label resIdLabel, resId, sourceCommentLabel, msgContextLabel, msgContext, sourceComment, lastModifiedByLabel, lastModifiedBy, lastModifiedTimeLabel, lastModifiedTime;

   @Inject
   public TransUnitDetailsPanel(TableEditorMessages messages)
   {
      this.messages = messages;
      initWidget(uiBinder.createAndBindUi(this));
      headerLabel.setText(messages.transUnitDetailsHeading());
   }

   public void setDetails(TransUnit transUnit)
   {
      resIdLabel.setText("Resource ID: ");
      resId.setText(transUnit.getResId());
      
      String context = transUnit.getMsgContext();

      if (context == null)
      {
         msgContextPanel.setVisible(false);
      }
      else
      {
         msgContextLabel.setText("Message Context: ");
         msgContext.setText(context);
         
         msgContextPanel.setVisible(true);
      }


      sourceCommentLabel.setText("Source Comment: ");
      sourceComment.setText(transUnit.getSourceComment());
      String person = transUnit.getLastModifiedBy();
      if (person != null && !person.isEmpty())
      {
         lastModifiedByLabel.setText("Last Modified By:");
         lastModifiedBy.setText(person);
         lastModifiedTimeLabel.setText("Last Modified Time:");
         lastModifiedTime.setText(transUnit.getLastModifiedTime());
      }
      else
      {
         lastModifiedByLabel.setText("");
         lastModifiedBy.setText("");
         lastModifiedTimeLabel.setText("");
         lastModifiedTime.setText("");
      }

      if (!Strings.isNullOrEmpty(context) || !Strings.isNullOrEmpty(transUnit.getSourceComment()))
      {
         headerLabel.setText(messages.transUnitDetailsHeadingWithInfo(transUnit.getRowIndex(), transUnit.getId().toString(), "(I)"));
      }
      else
      {
         headerLabel.setText(messages.transUnitDetailsHeadingWithInfo(transUnit.getRowIndex(), transUnit.getId().toString(), ""));
      }
//      expand();
      collapse();
   }

//   @UiHandler("headerLabel")
//   public void onHeaderLabelClick(ClickEvent event)
//   {
//
//      if (!contentPanel.isVisible())
//      {
//         expand();
//      }
//      else if (contentPanel.isVisible())
//      {
//         collapse();
//      }
//   }

   @UiHandler("headerLabel")
   public void onMouseHover(MouseOverEvent event)
   {
      if (!contentPanel.isVisible())
      {
         expand();
      }
   }

   @UiHandler("headerLabel")
   public void onMouseOut(MouseOutEvent event)
   {
      if (contentPanel.isVisible())
      {
         collapse();
      }
   }

   public void expand()
   {
      contentPanel.setHeight("95px");
      contentPanel.setVisible(true);
   }

   public void collapse()
   {
      contentPanel.setHeight("0px");
      contentPanel.setVisible(false);
   }
}
