package org.zanata.webtrans.shared.model;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnitValidationResult implements IsSerializable
{
   private TransUnit transUnit;
   private List<String> errorMessages;

   // for GWT
   @SuppressWarnings("unused")
   private TransUnitValidationResult()
   {
   }
   
   public TransUnitValidationResult(TransUnit transUnit, List<String> errorMessages)
   {
      this.transUnit = transUnit;
      this.errorMessages = errorMessages;
   }

   public TransUnit getTransUnit()
   {
      return transUnit;
   }

   public List<String> getErrorMessages()
   {
      return errorMessages;
   }
}
