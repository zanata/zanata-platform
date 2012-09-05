/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.ui.table.column;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class CopyButtonColumn<T> extends Column<T, String>
{

   public CopyButtonColumn(final String buttonText, final String buttonTooltip)
   {
      super(new ButtonCell()
      {
         @Override
         public void render(Context context, SafeHtml data, SafeHtmlBuilder sb)
         {
            sb.appendHtmlConstant("<button type=\"button\" tabindex=\"-1\" title=\"" + buttonTooltip + "\">");
            sb.appendHtmlConstant(buttonText);
            sb.appendHtmlConstant("</button>");
         }
      });
   }

   @Override
   public String getValue(T object)
   {
      return "Copy";
   }
}
