package net.openl10n.flies.action;

import java.io.Serializable;
import java.util.List;

import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;


@Name("languageSearchAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
public class LanguageSearchAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @In
   LocaleService localeServiceImpl;
   @DataModel
   List<FliesLocalePair> allLanguages;
   @DataModelSelection
   FliesLocalePair selectedLanguage;

   public void loadSupportedLanguage()
   {
      allLanguages = localeServiceImpl.getAllLocales();
   }

   public FliesLocalePair getSelectedLanguage()
   {
      return selectedLanguage;
   }

   public void disable(FliesLocalePair fliesLocalePair)
   {
      localeServiceImpl.disable(fliesLocalePair.getLocaleId());
      Events.instance().raiseEvent("disableLanguage");
   }

   public void enable(FliesLocalePair fliesLocalePair)
   {
      localeServiceImpl.enable(fliesLocalePair.getLocaleId());
      Events.instance().raiseEvent("enableLanguage");
   }

}
