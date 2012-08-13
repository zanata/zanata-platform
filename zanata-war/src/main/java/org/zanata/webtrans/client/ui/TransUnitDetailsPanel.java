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
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitDetailsPanel extends Composite
{
   private static TransUnitDetailsPanelUiBinder uiBinder = GWT.create(TransUnitDetailsPanelUiBinder.class);
   @UiField
   TableEditorMessages messages;

   @UiField
   Label headerLabel;
   @UiField
   Label resId, msgContext, sourceComment, lastModifiedBy, lastModifiedTime;

   public TransUnitDetailsPanel()
   {
      initWidget(uiBinder.createAndBindUi(this));
      headerLabel.setText(messages.transUnitDetailsHeading());
   }

   public void setDetails(TransUnit transUnit)
   {
      resId.setText(transUnit.getResId());

      String context = transUnit.getMsgContext();
      msgContext.setText(Strings.nullToEmpty(context));

      sourceComment.setText(transUnit.getSourceComment());
      String person = transUnit.getLastModifiedBy();
      if (person != null && !person.isEmpty())
      {
         lastModifiedBy.setText(person);
         lastModifiedTime.setText(transUnit.getLastModifiedTime());
      }
      else
      {
         lastModifiedBy.setText("");
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
   }

   interface TransUnitDetailsPanelUiBinder extends UiBinder<Widget, TransUnitDetailsPanel>
   {
   }
}
