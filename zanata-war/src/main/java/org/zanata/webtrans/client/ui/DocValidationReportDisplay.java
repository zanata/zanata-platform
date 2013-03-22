package org.zanata.webtrans.client.ui;

import java.util.Date;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;

import com.google.inject.ImplementedBy;

@ImplementedBy(DocValidationReportView.class)
public interface DocValidationReportDisplay extends WidgetDisplay
{
   void center();

   void hide();

   void init(Set<DocumentId> errorDocs);

   void updateRow(DocumentId documentId, LocaleId localeId, List<TransUnitValidationResult> result, Date endTime);

}
