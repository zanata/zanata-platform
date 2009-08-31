package org.fedorahosted.flies.webtrans.action;

import java.util.Map;

import javax.faces.model.DataModel;

import org.jboss.seam.databinding.DataSelector;

/**
 * Extracts the selected object (the element, or the value of a map) from a JSF DataModel.
 * 
 * @author Gavin King
 */
public class MyDataModelSelector implements DataSelector<MyDataModelSelection, DataModel>
{
   
   public String getVariableName(MyDataModelSelection in)
   {
      return in.value();
   }

   public Object getSelection(MyDataModelSelection in, DataModel wrapper)
   {
      if ( wrapper.getRowCount()==0 || wrapper.getRowIndex()<0 )
      {
         return null;
      }
      else
      {
         Object rowData = wrapper.getRowData();
         if (rowData instanceof Map.Entry)
         {
            return ( (Map.Entry) rowData ).getValue();
         }
         else
         {
            return rowData;
         }
      }
   }
   
}
