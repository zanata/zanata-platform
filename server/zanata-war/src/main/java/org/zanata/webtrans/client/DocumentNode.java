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
package org.zanata.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.rpc.GetStatusCount;
import org.zanata.webtrans.shared.rpc.GetStatusCountResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

public class DocumentNode extends Node<DocumentInfo>
{

   private static DocumentNodeUiBinder uiBinder = GWT.create(DocumentNodeUiBinder.class);

   interface DocumentNodeUiBinder extends UiBinder<Widget, DocumentNode>
   {
   }

   interface Styles extends CssResource
   {
      String mouseOver();

      String selected();
   }

   private static final HyperlinkImpl impl = new HyperlinkImpl();

   @UiField
   Label translatedWordsLabel;

   @UiField
   Label untranslatedWordsLabel;

   @UiField
   Label hoursLeftLabel;

   @UiField
   Label documentLabel;


   @UiField(provided = true)
   final Resources resources;

   @UiField(provided = true)
   TransUnitCountGraph transUnitCountGraph;

   @UiField(provided = true)
   final FlowPanel rootPanel;

   @UiField
   Styles style;

   final WebTransMessages messages;
   private final TranslationStats statusCount = new TranslationStats();
   private final CachingDispatchAsync dispatcher;

   public DocumentNode(Resources resources, WebTransMessages messages, CachingDispatchAsync dispatcher)
   {
      this.resources = resources;
      this.messages = messages;
      this.transUnitCountGraph = new TransUnitCountGraph(messages);
      this.dispatcher = dispatcher;

      rootPanel = new FlowPanel()
      {
         public void onBrowserEvent(Event event)
         {
            switch (event.getTypeInt())
            {
            case Event.ONMOUSEOVER:
               addStyleName(style.mouseOver());
               break;
            case Event.ONMOUSEOUT:
               removeStyleName(style.mouseOver());
               break;
            case Event.ONCLICK:
               if (event.getButton() == NativeEvent.BUTTON_LEFT && impl.handleAsClick(event))
               {
                  ClickEvent.fireNativeEvent(event, DocumentNode.this);
               }
            }

            super.onBrowserEvent(event);
         };
      };

      initWidget(uiBinder.createAndBindUi(this));
      rootPanel.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONCLICK);
   }


   public DocumentNode(Resources resources, WebTransMessages messages, DocumentInfo doc, CachingDispatchAsync dispatcher)
   {
      this(resources, messages, dispatcher);
      setDataItem(doc);
   }

   public DocumentNode(Resources resources, WebTransMessages messages, DocumentInfo doc, CachingDispatchAsync dispatcher, ClickHandler clickHandler, EventBus eventBus)
   {
      this(resources, messages, doc, dispatcher);
      addHandler(clickHandler, ClickEvent.getType());
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            if (event.getDocumentId().equals(getDataItem().getId()))
            {
               TransUnitCount unitCount = statusCount.getUnitCount();
               TransUnitWords wordCount = statusCount.getWordCount();
               unitCount.decrement(event.getPreviousStatus());
               unitCount.increment(event.getTransUnit().getStatus());
               wordCount.decrement(event.getPreviousStatus(), event.getWordCount());
               wordCount.increment(event.getTransUnit().getStatus(), event.getWordCount());
               getTransUnitCountGraph().setStats(statusCount);

               translatedWordsLabel.setText(getTransUnitCountGraph().getWordsApproved() + " words");
               untranslatedWordsLabel.setText(getTransUnitCountGraph().getWordsUntranslated() + " words");
               hoursLeftLabel.setText(getTransUnitCountGraph().getRemainingWordsHours() + " hours");
            }
         }
      });
   }

   public void refresh()
   {
      rootPanel.getElement().setId("doc-#" + getDataItem().getId().toString());
      documentLabel.setText(getDataItem().getName());
      requestStatusCount(getDataItem().getId());
   }

   @Override
   boolean isDocument()
   {
      return true;
   }

   public TransUnitCountGraph getTransUnitCountGraph()
   {
      return this.transUnitCountGraph;
   }

   public void setSelected(boolean selected)
   {
      if (selected)
      {
         rootPanel.addStyleName(style.selected());
      }
      else
      {
         rootPanel.removeStyleName(style.selected());
      }

   }
   private void requestStatusCount(final DocumentId newDocumentId)
   {
      dispatcher.execute(new GetStatusCount(newDocumentId), new AsyncCallback<GetStatusCountResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("error fetching GetStatusCount: " + caught.getMessage());
         }

         @Override
         public void onSuccess(GetStatusCountResult result)
         {
            statusCount.set(result.getCount());
            getTransUnitCountGraph().setStats(statusCount);
            translatedWordsLabel.setText(getTransUnitCountGraph().getWordsApproved() + " words");
            untranslatedWordsLabel.setText(getTransUnitCountGraph().getWordsUntranslated() + " words");
            hoursLeftLabel.setText(getTransUnitCountGraph().getRemainingWordsHours() + " hours");
         }
      });
   }
   
}
