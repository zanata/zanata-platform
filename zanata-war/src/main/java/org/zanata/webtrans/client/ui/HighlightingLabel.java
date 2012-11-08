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
package org.zanata.webtrans.client.ui;

import com.google.common.base.Strings;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;

public class HighlightingLabel extends HTML implements SourceContentWrapper
{

   private String plainText;

   public HighlightingLabel()
   {
      // workaround for https://bugzilla.mozilla.org/show_bug.cgi?id=116083
      super("<pre></pre>");
   }

   public HighlightingLabel(String text)
   {
      this();
      setText(text);
   }

   @Override
   public String getText()
   {
      return plainText;
   }

   @Override
   public void setText(String text)
   {
      this.plainText = text;
      highlight();
      updateHorizontalAlignment();
   }

   private void highlight()
   {
      Element preElement = getElement().getFirstChildElement();
      String text = plainText == null ? "" : plainText.replaceAll("\n", "¶\n");
      Highlighting.syntaxHighlight(text, preElement);
      preElement.addClassName("cm-s-default");
   }

   @Override
   public void refresh()
   {
      // no op
   }

   @Override
   public void highlight(String searchTerm)
   {
      Element element = getElement();
      if (Strings.isNullOrEmpty(searchTerm))
      {
         highlight();
      }
      else
      {
         Highlighting.searchHighlight(searchTerm, element);
      }
   }
}
