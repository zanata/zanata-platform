package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name = "projectTypeType", namespace = Namespaces.FLIES)
@XmlEnum(String.class)
public enum ProjectType
{
   IterationProject, ProjectCollection;
}