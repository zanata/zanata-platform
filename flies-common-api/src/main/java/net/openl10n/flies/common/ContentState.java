/**
 * 
 */
package net.openl10n.flies.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "contentStateType")
public enum ContentState
{
   New, NeedReview, Approved, FuzzyOrUntranslated,
}