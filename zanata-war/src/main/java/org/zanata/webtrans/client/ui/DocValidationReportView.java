package org.zanata.webtrans.client.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DocValidationReportView extends DialogBox implements DocValidationReportDisplay
{
   @UiField
   FlexTable docsTable;

   @UiField
   Label reportGenerated;

   @UiField
   Styles style;

   private static DocValidationReportViewUiBinder uiBinder = GWT.create(DocValidationReportViewUiBinder.class);

   private final HashMap<DocumentId, Integer> rowsMap;
   private final Resources resources;

   interface DocValidationReportViewUiBinder extends UiBinder<HTMLPanel, DocValidationReportView>
   {
   }

   interface Styles extends CssResource
   {
   }

   @Inject
   public DocValidationReportView(final Resources resources)
   {
      super(true, true);
      setGlassEnabled(true);
      setStyleName("gwt-DialogBox-NoFixedSize");
      setText("Document validation report");

      this.resources = resources;

      docsTable = new FlexTable();
      rowsMap = new HashMap<DocumentId, Integer>();

      setWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public void init(Set<DocumentId> errorDocs)
   {
      docsTable.clear();
      rowsMap.clear();

      int i = 0;
      for (DocumentId documentId : errorDocs)
      {
         rowsMap.put(documentId, i);
         docsTable.setWidget(i, 0, new DocValidationReportTable(resources));
         i++;
      }
   }

   @Override
   public void updateRow(DocumentId documentId, LocaleId localeId, List<TransUnitValidationResult> result, Date endTime)
   {
      Integer row = rowsMap.get(documentId);
      if (row != null)
      {
         DocValidationReportTable docTable = (DocValidationReportTable) docsTable.getWidget(row, 0);
         docTable.setReportContent(result);
         reportGenerated.setText(DateUtil.formatShortDate(new Date()));
      }
   }
}
