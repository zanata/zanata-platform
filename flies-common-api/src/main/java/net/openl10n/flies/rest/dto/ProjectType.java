package net.openl10n.flies.rest.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.Namespaces;

@XmlType(name = "projectTypeType", namespace = Namespaces.FLIES)
@XmlEnum(String.class)
public enum ProjectType
{
   IterationProject, ProjectCollection;
}