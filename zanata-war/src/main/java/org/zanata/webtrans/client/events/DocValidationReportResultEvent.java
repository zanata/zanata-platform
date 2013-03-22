package org.zanata.webtrans.client.events;

import java.util.Date;
import java.util.List;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;

import com.google.gwt.event.shared.GwtEvent;

public class DocValidationReportResultEvent extends GwtEvent<DocValidationReportResultHandler>
{
   private Date endTime;
   private List<TransUnitValidationResult> result;
   private LocaleId localeId;
   private DocumentId documentId;

   public DocValidationReportResultEvent(DocumentId documentId, Date endTime, List<TransUnitValidationResult> result, LocaleId localeId)
   {
      this.endTime = endTime;
      this.result = result;
      this.localeId = localeId;
      this.documentId = documentId;
   }

   /**
    * Handler type.
    */
   private static Type<DocValidationReportResultHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<DocValidationReportResultHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<DocValidationReportResultHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<DocValidationReportResultHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(DocValidationReportResultHandler handler)
   {
      handler.onCompleteRunDocReportValidation(this);
   }

   public Date getEndTime()
   {
      return endTime;
   }

   public List<TransUnitValidationResult> getResult()
   {
      return result;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }
}
