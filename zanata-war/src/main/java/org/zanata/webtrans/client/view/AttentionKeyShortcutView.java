package org.zanata.webtrans.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttentionKeyShortcutView extends Widget implements AttentionKeyShortcutDisplay
{
   private static AttentionKeyShortcutViewUiBinder uiBinder = GWT.create(AttentionKeyShortcutViewUiBinder.class);

   interface AttentionKeyShortcutViewUiBinder extends UiBinder<VerticalPanel, AttentionKeyShortcutView>
   {
   }

   @Inject
   public AttentionKeyShortcutView()
   {
      VerticalPanel widget = uiBinder.createAndBindUi(this);

      // TODO Auto-generated constructor stub
   }

   @Override
   public Widget asWidget()
   {
      // TODO Auto-generated method stub
      return this;
   }

}
