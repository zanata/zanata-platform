package org.zanata.rest.service;

import java.util.HashSet;
import java.util.Set;


import org.testng.annotations.Test;
import org.zanata.ZanataJpaTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ResourceType;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.service.ResourceUtils;

public class ResourceUtilsJpaTest extends ZanataJpaTest
{
   private static ResourceUtils resourceUtils = new ResourceUtils();

   @Test
   void transferFromResourceMetadata()
   {
      ResourceMeta from = new ResourceMeta("resId");
      from.setContentType(ContentType.TextPlain);
      PoHeader poHeader = new PoHeader();
      poHeader.setComment("comment");
      poHeader.getEntries().add(new HeaderEntry("key", "value"));
      from.getExtensions(true).add(poHeader);
      from.setLang(LocaleId.ES);
      from.setName("name");
      from.setType(ResourceType.FILE);

      HLocale hLocale = new HLocale(LocaleId.EN_US);

      HDocument to = new HDocument("fullPath", ContentType.PO, hLocale);

      Set<String> commentExt = new HashSet<String>();
      commentExt.add(SimpleComment.ID);
      resourceUtils.transferFromResourceMetadata(from, to, commentExt, hLocale, 1);
      // TODO check the results in 'to'
   }
}
