/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.webtrans.client.ui.HighlightingLabel;
import net.openl10n.flies.webtrans.shared.model.TransUnit;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class SourcePanel extends Composite implements HasValue<TransUnit>
{

   private final FlowPanel panel;
   private final Label sourceLabel;
   private TransUnit value;

   private final NavigationMessages messages;

   public SourcePanel(TransUnit value, NavigationMessages messages)
   {
      this.value = value;
      this.messages = messages;
      panel = new FlowPanel();
      panel.setSize("100%", "100%");
      initWidget(panel);
      setStylePrimaryName("TableEditorSource");

      sourceLabel = new HighlightingLabel(value.getSource());
      sourceLabel.setStylePrimaryName("TableEditorContent");
      sourceLabel.setTitle(messages.sourceCommentLabel() + value.getSourceComment());

      panel.add(sourceLabel);

      refresh();
   }

   public void refresh()
   {
   }

   @Override
   public TransUnit getValue()
   {
      return value;
   }

   @Override
   public void setValue(TransUnit value)
   {
      setValue(value, true);
   }

   @Override
   public void setValue(TransUnit value, boolean fireEvents)
   {
      if (this.value != value)
      {
         this.value = value;
         if (fireEvents)
         {
            ValueChangeEvent.fire(this, value);
         }
         refresh();
      }
   }

   @Override
   public HandlerRegistration addValueChangeHandler(ValueChangeHandler<TransUnit> handler)
   {
      return addHandler(handler, ValueChangeEvent.getType());
   }
   
   public void highlightSearch(String search)
   {
      ((HighlightingLabel) sourceLabel).highlightSearch(search);
   }
   

}
