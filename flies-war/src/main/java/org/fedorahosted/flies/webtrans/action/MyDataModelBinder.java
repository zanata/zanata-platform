package org.fedorahosted.flies.webtrans.action;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.databinding.DataBinder;
import org.jboss.seam.faces.DataModels;
import org.richfaces.model.ExtendedTableDataModel;

/**
 * Exposes a List, array, Map or Set to the UI as a JSF DataModel
 * 
 * @author Gavin King
 */
public class MyDataModelBinder implements DataBinder<MyDataModel, Object, javax.faces.model.DataModel>
{

   public String getVariableName(MyDataModel out)
   {
      return out.value();
   }

   public ScopeType getVariableScope(MyDataModel out)
   {
      return out.scope();
   }

   public javax.faces.model.DataModel wrap(MyDataModel out, Object value)
   {
	  if(value instanceof ExtendedTableDataModel)
		  return (ExtendedTableDataModel) value;
      return DataModels.instance().getDataModel(value);
   }

   public Object getWrappedData(MyDataModel out, javax.faces.model.DataModel wrapper)
   {
      return wrapper.getWrappedData();
   }

   public Object getSelection(MyDataModel out, javax.faces.model.DataModel wrapper)
   {
      if ( wrapper.getRowCount()==0 || wrapper.getRowIndex()<0 || 
           wrapper.getRowIndex()>=wrapper.getRowCount())
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

   public boolean isDirty(MyDataModel out, javax.faces.model.DataModel wrapper, Object value)
   {
	  if(getWrappedData(out, wrapper) == null)
		  return false;
		  
      return !getWrappedData(out, wrapper).equals(value);
   }
   
}