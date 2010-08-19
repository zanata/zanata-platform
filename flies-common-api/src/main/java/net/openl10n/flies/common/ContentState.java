/**
 * 
 */
package net.openl10n.flies.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "contentStateType", namespace = Namespaces.FLIES)
public enum ContentState
{
   New, NeedReview, Approved
}