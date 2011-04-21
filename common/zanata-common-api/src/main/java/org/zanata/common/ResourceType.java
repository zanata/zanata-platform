package org.zanata.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "resourceEnumType")
public enum ResourceType
{
   FILE, DOCUMENT, PAGE;
}
