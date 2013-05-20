/**
 * 
 */
package org.zanata.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "contentStateType")
public enum ContentState
{
   // translation life cycle order:
   // New -> NeedReview -> Saved -> Approved (Saved is only used in require review project)
   New, NeedReview, Approved, Saved
}