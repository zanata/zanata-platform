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
package net.openl10n.flies.webtrans.shared.rpc;



public class GetTransUnitsNavigation extends AbstractWorkspaceAction<GetTransUnitsNavigationResult>
{

   private static final long serialVersionUID = 1L;

   private int count;
   private Long id;
   private boolean reverse;
   private String phrase;

   @SuppressWarnings("unused")
   private GetTransUnitsNavigation()
   {
   }

   public GetTransUnitsNavigation(Long id, int count, boolean reverse, String phrase)
   {
      this.id = id;
      this.count = count;
      this.setReverse(reverse);
      this.phrase = phrase;
   }


   public int getCount()
   {
      return count;
   }

   public void setCount(int count)
   {
      this.count = count;
   }

   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   public void setReverse(boolean reverse)
   {
      this.reverse = reverse;
   }

   public boolean isReverse()
   {
      return reverse;
   }

   public String getPhrase()
   {
      return this.phrase;
   }
}