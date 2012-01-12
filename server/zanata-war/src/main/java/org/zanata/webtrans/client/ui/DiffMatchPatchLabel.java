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

import com.google.gwt.user.client.ui.HTML;

public class DiffMatchPatchLabel extends HTML
{
   private String original;
   private String plainText;

   public DiffMatchPatchLabel()
   {
   }

   public DiffMatchPatchLabel(String orig, String text)
   {
      super();
      this.original = orig;
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
      String diff = Highlighting.diffAsHtml(original, plainText);
      setHTML(diff);
   }

}
