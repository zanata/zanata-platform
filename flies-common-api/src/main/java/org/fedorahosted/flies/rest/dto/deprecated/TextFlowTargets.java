package org.fedorahosted.flies.rest.dto.deprecated;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;

/**
 * Represents a list of target-language translations for a single TextFlow
 * 
 * @author asgeirf
 * 
 */
@XmlType(name = "textFlowTargetsType", namespace = Namespaces.FLIES)
@XmlRootElement(name = "targets", namespace = Namespaces.FLIES)
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
      return Utility.toXML(this);
   }

}
