package org.zanata.adapter.xliff;

import java.util.ArrayList;
import java.util.List;

public abstract class XliffCommon
{
   protected static final String DELIMITER = "::";

   protected static final String ELE_FILE = "file";
   protected static final String ELE_TRANS_UNIT = "trans-unit";
   protected static final String ELE_SOURCE = "source";
   protected static final String ELE_CONTEXT_GROUP = "context-group";
   protected static final String ELE_CONTEXT = "context";
   protected static final String ELE_TARGET = "target";
   protected static final String ELE_BODY = "body";

   private static List<String> contentEle = new ArrayList<String>();

   protected static final String ATTRI_SOURCE_LANGUAGE = "source-language";
   protected static final String ATTRI_TARGET_LANGUAGE = "target-language";
   protected static final String ATTRI_ID = "id";
   protected static final String ATTRI_CONTEXT_TYPE = "context-type";
   protected static final String ATTRI_NAME = "name";
   protected static final String ATTRI_DATATYPE = "datatype";
   protected static final String ATTRI_ORIGINAL = "original";

   protected List<String> getContentElementList()
   {
      if (contentEle.isEmpty())
      {
         contentEle.add("<g>");
         contentEle.add("</g>");
         contentEle.add("<g/>");
         contentEle.add("<x>");
         contentEle.add("</x>");
         contentEle.add("<x/>");
         contentEle.add("<bx>");
         contentEle.add("</bx>");
         contentEle.add("<bx/>");
         contentEle.add("<ex>");
         contentEle.add("</ex>");
         contentEle.add("<ex/>");
         contentEle.add("<bpt>");
         contentEle.add("</bpt>");
         contentEle.add("<bpt/>");
         contentEle.add("<ept>");
         contentEle.add("</ept>");
         contentEle.add("<ept/>");
         contentEle.add("<ph>");
         contentEle.add("</ph>");
         contentEle.add("<ph/>");
         contentEle.add("<it>");
         contentEle.add("</it>");
         contentEle.add("<it/>");
         contentEle.add("<mrk>");
         contentEle.add("</mrk>");
         contentEle.add("<mrk/>");
      }
      return contentEle;

   }

   protected enum CHECK
   {
      Validate, Quick, None;
   }

}
