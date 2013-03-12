package org.zanata.webtrans.shared.model;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnitValidationResult implements IsSerializable
{
   private TransUnitId transUnitId;
   private List<String> errorMessages;

   // for GWT
   @SuppressWarnings("unused")
   private TransUnitValidationResult()
   {
   }
   
   public TransUnitValidationResult(TransUnitId transUnitId, List<String> errorMessages)
   {
      this.transUnitId = transUnitId;
      this.errorMessages = errorMessages;
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public List<String> getErrorMessages()
   {
      return errorMessages;
   }
}
