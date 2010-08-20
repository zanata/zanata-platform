/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package net.openl10n.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * A {@link TextArea} that expands vertically as the user types, rather than
 * scrolling
 * 
 * Implementation based on {@link http
 * ://www.sitepoint.com/blogs/2009/07/30/build-auto-expanding-textarea-3/}.
 * 
 * @author asgeirf
 * 
 */
public class ExpandingTextArea extends TextArea
{

   public static final int EXPAND_MIN_DEFAULT = 0;
   public static final int EXPAND_MAX_DEFAULT = 9999;

   private int valLength;
   private int boxWidth;

   private int expandMin = EXPAND_MIN_DEFAULT;
   private int expandMax = EXPAND_MAX_DEFAULT;

   /**
    * Creates an empty text area.
    */
   public ExpandingTextArea()
   {
      super();
      initialize();
   }

   /**
    * This constructor may be used by subclasses to explicitly use an existing
    * element. This element must be a &lt;textarea&gt; element.
    * 
    * @param element the element to be used
    */
   public ExpandingTextArea(Element element)
   {
      super(element);
      initialize();
   }

   /**
    * Creates an empty text area.
    * 
    * @param expandMin minimum visible rows of text
    * @Param expandMax maximum rows of text to expand the area to
    */
   public ExpandingTextArea(int expandMin, int expandMax)
   {
      super();
      this.expandMax = expandMax;
      this.expandMin = expandMin;
      initialize();
   }

   /**
    * This constructor may be used by subclasses to explicitly use an existing
    * element. This element must be a &lt;textarea&gt; element.
    * 
    * @param element the element to be used
    * @param expandMin minimum visible rows of text
    * @Param expandMax maximum rows of text to expand the area to
    */
   public ExpandingTextArea(Element element, int expandMin, int expandMax)
   {
      super(element);
      this.expandMax = expandMax;
      this.expandMin = expandMin;
      initialize();
   }

   /**
    * Gets maximum number of rows to expand
    * 
    * @return maximum number of rows to expand
    */
   public int getExpandMax()
   {
      return expandMax;
   }

   /**
    * Gets minimum number of rows to expand
    * 
    * @return minimum number of rows to expand
    */
   public int getExpandMin()
   {
      return expandMin;
   }

   private void initialize()
   {
      getElement().getStyle().setPaddingTop(0, Unit.PX);
      getElement().getStyle().setPaddingBottom(0, Unit.PX);
      addFocusHandler(new FocusHandler()
      {
         @Override
         public void onFocus(FocusEvent event)
         {
            resizeToContents();
         }
      });

      addKeyUpHandler(new KeyUpHandler()
      {
         @Override
         public void onKeyUp(KeyUpEvent event)
         {
            resizeToContents();
         }
      });
   }

   private void resizeToContents()
   {

      int vlen = getText().length();
      int ewidth = getElement().getOffsetWidth();

      Element e = getElement();

      // TODO this doesn't work on msie and opera
      // need to add browser specific implementation
      boolean hCheck = true;

      if (vlen != valLength || ewidth != boxWidth)
      {
         if (hCheck && (vlen < valLength || ewidth != boxWidth))
         {
            e.getStyle().setHeight(0, Unit.PX);
         }
         int h = Math.max(expandMin, Math.min(e.getScrollHeight(), expandMax));

         e.getStyle().setOverflow(e.getScrollHeight() > h ? Overflow.AUTO : Overflow.HIDDEN);
         e.getStyle().setHeight(h, Unit.PX);

         valLength = vlen;
         boxWidth = ewidth;
      }
   }
}
