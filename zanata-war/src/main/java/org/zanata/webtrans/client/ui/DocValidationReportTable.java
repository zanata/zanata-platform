package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class DocValidationReportTable extends Composite
{
   interface DocValidationReportTableUiBinder extends UiBinder<Widget, DocValidationReportTable>
   {
   }

   private static DocValidationReportTableUiBinder uiBinder = GWT.create(DocValidationReportTableUiBinder.class);
   
   @UiField
   Label docNameLabel;

   @UiField
   FlowPanel content;

   private final CellTable<TransUnitValidationResult> transUnitTable;
   private final Image loading;
   private final ListDataProvider<TransUnitValidationResult> dataProvider;

   private TextColumn<TransUnitValidationResult> stringsColumn = new TextColumn<TransUnitValidationResult>()
   {
      @Override
      public String getValue(TransUnitValidationResult result)
      {
         return result.getTransUnit().getId().toString();
      }
   };

   private TextColumn<TransUnitValidationResult> errorStringColumn = new TextColumn<TransUnitValidationResult>()
   {
      @Override
      public String getValue(TransUnitValidationResult result)
      {
         return result.getErrorMessages().toString();
      }
   };

   public DocValidationReportTable(final Resources resources)
   {
      initWidget(uiBinder.createAndBindUi(this));
      
      loading = new Image(resources.spinnerLarge());
      transUnitTable = new CellTable<TransUnitValidationResult>();
      dataProvider = new ListDataProvider<TransUnitValidationResult>();

      transUnitTable.addColumn(stringsColumn, "id");
      transUnitTable.addColumn(errorStringColumn, "Validation message");
      
      transUnitTable.setEmptyTableWidget(new Label("No content"));

      dataProvider.addDataDisplay(transUnitTable);
      
      setLoading(true);
   }

   public void setReportContent(List<TransUnitValidationResult> validationResult)
   {
      dataProvider.getList().clear();

      for (TransUnitValidationResult result : validationResult)
      {
         dataProvider.getList().add(result);
      }
      setLoading(false);
   }
   
   public void setDocName(String docName)
   {
      docNameLabel.setText(docName);
   }

   private void setLoading(boolean isLoading)
   {
      content.clear();
      if (isLoading)
      {
         content.add(loading);
      }
      else
      {
         content.add(transUnitTable);
      }
   }

}
