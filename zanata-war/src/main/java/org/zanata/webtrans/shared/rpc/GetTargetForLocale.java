package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.model.TransUnitId;

/**
 *
 * @author Hannes Eskebaek
 */
public class GetTargetForLocale extends AbstractWorkspaceAction<GetTargetForLocaleResult>
{
   private static final long serialVersionUID = 1L;
   private TransUnitId sourceTransUnitId;
   private Locale locale;

   @SuppressWarnings("unused")
   private GetTargetForLocale()
   {
   }

   public GetTargetForLocale(TransUnitId sourceTransUnitId, Locale locale)
   {
      this.sourceTransUnitId = sourceTransUnitId;
      this.locale = locale;
   }

   public TransUnitId getSourceTransUnitId()
   {
      return sourceTransUnitId;
   }

   public void setSourceTransUnitId(TransUnitId sourceTransUnitId)
   {
      this.sourceTransUnitId = sourceTransUnitId;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public void setLocale(Locale locale)
   {
      this.locale = locale;
   }
}
