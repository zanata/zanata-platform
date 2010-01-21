package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;


@XmlRootElement(name="document-target", namespace=Namespaces.FLIES)
public class DocumentTarget {
        
        private LocaleId targetLanguage;

        private List<TextFlowTarget> textFlowTargets;

        @XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
        @XmlAttribute(name="target-language", required=true)
        public LocaleId getTargetLanguage() {
                return targetLanguage;
        }
        
        public void setTargetLanguage(LocaleId targetLanguage) {
                this.targetLanguage = targetLanguage;
        }
        
        @XmlElement(name="text-flow-target", namespace=Namespaces.FLIES)
        public List<TextFlowTarget> getTextFlowTargets() {
                if(textFlowTargets == null)
                        textFlowTargets = new ArrayList<TextFlowTarget>();
                return textFlowTargets;
        }

        @Override
    	public String toString() {
    		return Utility.toXML(this);
    	}
    	
}