package org.zanata.webtrans.shared.rpc;

import java.util.List;
import org.zanata.webtrans.shared.model.Locale;

/**
 *
 * @author Hannes Eskebaek
 */
public class GetLocaleListResult implements DispatchResult
{
   private static final long serialVersionUID = 1L;
   private List<Locale> locales;

   @SuppressWarnings("unused")
   private GetLocaleListResult()
   {
   }

   public GetLocaleListResult(List<Locale> locales)
   {
      this.locales = locales;
   }

   public List<Locale> getLocales()
   {
      return locales;
   }
}
