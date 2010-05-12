package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum(String.class)
public enum ProjectType {
	IterationProject,
	ProjectCollection;
}