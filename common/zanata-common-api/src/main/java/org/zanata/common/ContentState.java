/**
 * 
 */
package org.zanata.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "contentStateType")
public enum ContentState
{
   New, NeedReview, Approved
}