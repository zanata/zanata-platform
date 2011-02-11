package net.openl10n.flies.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "resourceEnumType")
public enum ResourceType
{
   FILE, DOCUMENT, PAGE;
}
