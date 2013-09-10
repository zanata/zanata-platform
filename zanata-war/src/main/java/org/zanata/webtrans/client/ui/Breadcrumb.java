/**
 * 
 */
package org.zanata.webtrans.client.ui;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * @author aeng
 *
 */
public class Breadcrumb extends Composite implements HasClickHandlers
{
   
   interface BreadcrumbUiBinder extends UiBinder<HTMLPanel, Breadcrumb>
   {
   }
   
   private static BreadcrumbUiBinder uiBinder = GWT.create(BreadcrumbUiBinder.class);
   
   @UiField
   InlineLabel rightChevron;
   
   @UiField
   Anchor link;
   
   @UiField
   Styles style;
   
   interface Styles extends CssResource
   {
      String readOnlyLink();
      
      String link();
   }
   
   public Breadcrumb(boolean isFirstNode, boolean isAnchorReadOnly, String href)
   {
      initWidget(uiBinder.createAndBindUi(this));
      
      if(isFirstNode)
      {
         rightChevron.setVisible(false);
      }
      
      if(isAnchorReadOnly)
      {
         link.removeStyleName(style.link());
         link.setStyleName(style.readOnlyLink());
      }
      
      if(!Strings.isNullOrEmpty(href))
      {
         link.setHref(href);
      }
   }
   
   public void setText(String text)
   {
      link.setText(text);
   }

   @Override
   public HandlerRegistration addClickHandler(ClickHandler handler)
   {
      return link.addClickHandler(handler);
   }
}
