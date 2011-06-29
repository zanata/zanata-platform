package org.zanata.client.commands.pull;

import java.io.IOException;

//import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

public interface PullStrategy
{
   void setPullOptions(PullOptions opts);
   StringSet getExtensions();

   boolean needsDocToWriteTrans();

   /**
    * @param doc
    * @throws IOException
    */
   void writeSrcFile(Resource doc) throws IOException;

   /**
    * @param doc
    * @param localeMapping
    * @param targetDoc
    * @throws IOException
    */
   void writeTransFile(Resource doc, LocaleMapping localeMapping, TranslationsResource targetDoc) throws IOException;
}
