package net.openl10n.api.rest.document;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.LocaleId;

/**
 * Represents a list of target-language translations for a single TextFlow
 * 
 * @author asgeirf
 *
 */
@XmlType(name="textFlowTargetsType", namespace=TextFlowTargets.NAMESPACE)
@XmlRootElement(name="targets", namespace=TextFlowTargets.NAMESPACE)
public class TextFlowTargets {
	
	public static final String NAMESPACE = "urn:extensions:target";
	
	private Set<TextFlowTarget> targets;
	
	@XmlElement(name="target", namespace=NAMESPACE)
	public Set<TextFlowTarget> getTargets() {
		if(targets == null)
			targets = new HashSet<TextFlowTarget>();
		return targets;
	}
	
	public TextFlowTarget getByLocale(LocaleId locale){
		for(TextFlowTarget target : targets){
			if(locale.equals(target.getLang())){
				return target;
			}
		}
		return null;
	}
}
