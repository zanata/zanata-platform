package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.editor.table.TableResources;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransUnitDetailsPanel extends Composite
{
   private final TransUnit transUnit;
   private final NavigationMessages messages;

   enum State
   {
      IS_HIDDEN, IS_SHOWN;
   }

   @UiField(provided = true)
   TableResources resources;

   @UiField
   Label headerLabel;

   @UiField
   LayoutPanel contentPanel;

   private static TransUnitDetailsPanelUiBinder uiBinder = GWT.create(TransUnitDetailsPanelUiBinder.class);

   interface TransUnitDetailsPanelUiBinder extends UiBinder<Widget, TransUnitDetailsPanel>
   {
   }

   @UiField
   Label resIdLabel, resId, sourceCommentLabel, sourceComment, msgContextLabel, msgContext, lastModifiedByLabel, lastModifiedBy, lastModifiedTimeLabel, lastModifiedTime;

   public TransUnitDetailsPanel(final TableResources resources, final NavigationMessages messages, TransUnit transUnit)
   {
      this.transUnit = transUnit;
      this.resources = resources;
      this.messages = messages;

      initWidget(uiBinder.createAndBindUi(this));
      setDetails();
      expand();
   }

   public void setDetails()
   {
      resIdLabel.setText("Resource ID: ");
      resId.setText(transUnit.getResId());
      
      String context = transUnit.getMsgContext();
      if (context != null)
      {
         msgContextLabel.setText("Message Context: ");
         msgContext.setText(context);
         msgContextLabel.setVisible(true);
         msgContext.setVisible(true);
      }
      else
      {
         msgContextLabel.setVisible(false);
         msgContext.setVisible(false);
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
   }

   public void setHeader(String header)
   {
      headerLabel.setText(header);
   }

   @UiHandler("headerLabel")
   public void onHeaderLabelClick(ClickEvent event)
   {

      if (!contentPanel.isVisible())
      {
         expand();
      }
      else if (contentPanel.isVisible())
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
