package org.zanata.client.commands.pull;

import java.io.IOException;

import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * Strategy for converting documents from Zanata to a local file type.
 * Every implementation must have a public constructor which accepts PullOptions.
 */
public interface PullStrategy
{
   /**
    * Which extensions (eg gettext, comment) does this strategy need to fetch from the server?
    * @return
    */
   StringSet getExtensions();

   /**
    * Does this strategy need the source document (Resource) when writing translations?
    * @return
    */
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
    * @throws IOException
    */
   void writeTransFile(
         Resource docWithLocalName,
         String docName,
         LocaleMapping localeMapping,
         TranslationsResource targetDoc) throws IOException;
}
