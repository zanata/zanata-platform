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
package org.zanata.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;

import org.jboss.seam.Component;
import org.richfaces.component.SortOrder;
import org.richfaces.component.UIColumn;
import org.richfaces.component.UIDataTable;

/**
 * Custom component implementation for a column Sorting component.
 *
 * This might not be needed once richfaces' data table is updated to support this feature.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@FacesComponent("columnSortCompositeComponent")
public class ColumnSortCompositeComponent extends UINamingContainer
{
   public UIColumn getParentColumn()
   {
      return (UIColumn)getFirstParent(UIColumn.class);
   }

   private UIComponent getFirstParent( Class<? extends UIComponent> parentClass )
   {
      UIComponent parent = this;

      while( parent != null && !parentClass.isAssignableFrom(parent.getClass()) )
      {
         parent = parent.getParent();
      }

      return parent;
   }

   public String getParentTableClientId()
   {
      UIDataTable dataTable = (UIDataTable)getFirstParent(UIDataTable.class);
      return dataTable.getClientId();
   }

   public Collection<UIColumn> getTableColumns()
   {
      UIDataTable dataTable = (UIDataTable)getFirstParent(UIDataTable.class);
      Set<UIColumn> cols = new HashSet<UIColumn>();

      for( UIComponent child : dataTable.getChildren() )
      {
         if( child instanceof UIColumn )
         {
            cols.add( (UIColumn)child );
         }
      }

      return cols;
   }
}
