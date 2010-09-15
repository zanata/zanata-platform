package net.openl10n.flies.rest.dto.deprecated;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;

/**
 * Represents a list of target-language translations for a single TextFlow
 * 
 * @author asgeirf
 * 
 */
@XmlType(name = "textFlowTargetsType", namespace = Namespaces.FLIES)
@XmlRootElement(name = "targets", namespace = Namespaces.FLIES)
@Deprecated
public class TextFlowTargets
{

   private Set<TextFlowTarget> targets;

   @XmlElement(name = "target", namespace = Namespaces.FLIES)
   public Set<TextFlowTarget> getTargets()
   {
      if (targets == null)
         targets = new HashSet<TextFlowTarget>();
      return targets;
   }

   public TextFlowTarget getByLocale(LocaleId locale)
   {
      for (TextFlowTarget target : targets)
      {
         if (locale.equals(target.getLang()))
         {
            return target;
         }
      }
      return null;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
