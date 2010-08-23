package net.openl10n.flies.model;

import java.io.Serializable;

import com.ibm.icu.util.ULocale;

public class LocaleId implements Serializable{
    private static final long serialVersionUID = 1L;
    private String id;

	public LocaleId(String language) {
		this.id=language;
	}
	
	public LocaleId(ULocale locale){
      StringBuilder builder = new StringBuilder();
      builder.append(locale.getLanguage());
      if (!locale.getCountry().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getCountry());
      }
      if (!locale.getScript().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getScript());
      }
      if (!locale.getVariant().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getVariant());
      }

      this.id= builder.toString();
	}

	public String getId() {
		return id;
	}
}
