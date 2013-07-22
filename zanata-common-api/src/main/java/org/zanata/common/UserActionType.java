package org.zanata.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "userActionType")
public enum UserActionType
{
   UPDATE_TRANSLATION, REVIEWED_TRANSLATION, UPLOAD_SOURCE_DOCUMENT, UPLOAD_TRANSLATION_DOCUMENT;
}