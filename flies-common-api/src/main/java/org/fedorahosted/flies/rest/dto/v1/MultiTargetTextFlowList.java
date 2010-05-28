package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="multiTargetTextFlowListType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resources", namespace=Namespaces.FLIES)
public class MultiTargetTextFlowList extends ArrayList<MultiTargetTextFlow> {
	
}
