package net.openl10n.flies.rest.dto.extensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.LocaleIdAdapter;
import net.openl10n.flies.rest.dto.po.HeaderEntry;
import net.openl10n.flies.rest.dto.resource.Extension;

@XmlType(name = "poTargetHeaderEntry", namespace = PoHeader.NAMESPACE, propOrder = { "comment", "entries" })
@XmlRootElement(name = "po-target-header", namespace = PoHeader.NAMESPACE)
public class PoTargetHeaderEntry extends Extension
{

   private LocaleId locale;

   private String comment;
   private List<HeaderEntry> entries;

   public PoTargetHeaderEntry()
   {
   }

   public PoTargetHeaderEntry(LocaleId locale, String comment, HeaderEntry... entries)
   {
      this.locale = locale;
      setComment(comment);
      for (int i = 0; i < entries.length; i++)
      {
         getEntries().add(entries[i]);
      }
   }

   @XmlAttribute(name = "locale", required = true)
   @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
   public LocaleId getLocale()
   {
      return locale;
   }

   public void setLocale(LocaleId locale)
   {
      this.locale = locale;
   }

   @XmlElement(name = "comment", namespace = PoHeader.NAMESPACE, required = true)
   public String getComment()
   {
      return comment;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   @XmlElementWrapper(name = "entries", namespace = PoHeader.NAMESPACE, required = true)
   @XmlElement(name = "entry", namespace = PoHeader.NAMESPACE)
   public List<HeaderEntry> getEntries()
   {
      if (entries == null)
         entries = new ArrayList<HeaderEntry>();
      return entries;
   }

}
