package org.zanata.client.commands.pull;

import java.io.IOException;

import org.zanata.client.config.LocaleMapping;
import org.zanata.common.io.FileDetails;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

public interface PullStrategy
{
   void setPullOptions(PullOptions opts);
   StringSet getExtensions();

   boolean needsDocToWriteTrans();

   /**
    * @param docWithLocalName
    * @throws IOException
    */
   void writeSrcFile(Resource docWithLocalName) throws IOException;

   /**
    * pre: docWithLocalName.getName() must match docName if docWithLocalName is not null
    * @param docWithLocalName may be null if needsDocToWriteTrans() returns false
    * @param docName may be null if needsDocToWriteTrans() returns false
    * @param localeMapping
    * @param targetDoc
    * @return Details of the file that was written. May be null if the Strategy cannot provide details.
    * @throws IOException
    */
   FileDetails writeTransFile(
         Resource docWithLocalName,
         String docName,
         LocaleMapping localeMapping,
         TranslationsResource targetDoc) throws IOException;
}
