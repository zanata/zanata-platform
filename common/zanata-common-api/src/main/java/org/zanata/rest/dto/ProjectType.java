package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "projectTypeType")
@XmlEnum(String.class)
public enum ProjectType
{
   IterationProject, ProjectCollection;
}