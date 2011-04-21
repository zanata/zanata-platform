package org.zanata.rest.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ResourceType;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;

public class ResourceTestObjectFactory
{
   private final Logger log = LoggerFactory.getLogger(ResourceTestObjectFactory.class);

   public Resource getTextFlowTest()
   {
      Resource sr = new Resource("test1");
      sr.setContentType(ContentType.TextPlain);
      sr.setLang(LocaleId.EN_US);
      sr.setType(ResourceType.FILE);
      // for the convenience of test only
      sr.getExtensions(true);

      TextFlow stf = new TextFlow("rest1", LocaleId.EN_US, "tf1");
      stf.getExtensions(true);
      sr.getTextFlows().add(stf);
      log.debug(sr.toString());
      return sr;
   }

   public Resource getTextFlowTest2()
   {
      Resource sr = new Resource("test2");
      sr.setContentType(ContentType.TextPlain);
      sr.setLang(LocaleId.EN_US);
      sr.setType(ResourceType.FILE);
      sr.getExtensions(true);

      TextFlow stf = new TextFlow("tf1", LocaleId.EN_US, "tf1");
      stf.getExtensions(true);
      TextFlow stf2 = new TextFlow("tf2", LocaleId.EN_US, "testtf2");
      // stf2.getExtensions(true);
      sr.getTextFlows().add(stf);
      sr.getTextFlows().add(stf2);
      return sr;
   }

   public Resource getPoHeaderTest()
   {
      Resource sr = getTextFlowTest();

      PoHeader poHeaderExt = new PoHeader("comment", new HeaderEntry("h1", "v1"), new HeaderEntry("h2", "v2"));
      sr.getExtensions(true).add(poHeaderExt);
      return sr;
   }

   public Resource getPotEntryHeaderTest()
   {
      Resource sr = getTextFlowTest();
      TextFlow stf = sr.getTextFlows().get(0);

      PotEntryHeader potEntryHeader = new PotEntryHeader();
      potEntryHeader.setContext("potentrycontext");
      // for the convenience of test only
      potEntryHeader.getFlags().add("");
      potEntryHeader.getReferences().add("");
      stf.getExtensions(true).add(potEntryHeader);
      return sr;
   }

   public Resource getTextFlowCommentTest()
   {
      Resource sr = getTextFlowTest();
      TextFlow stf = sr.getTextFlows().get(0);

      SimpleComment simpleComment = new SimpleComment("textflow comment");

      stf.getExtensions(true).add(simpleComment);
      return sr;
   }

   public Resource getPotEntryHeaderComment()
   {
      Resource sr = getTextFlowTest();
      TextFlow stf = sr.getTextFlows().get(0);
      SimpleComment simpleComment = new SimpleComment("textflow comment");

      PotEntryHeader potEntryHeader = new PotEntryHeader();
      potEntryHeader.setContext("potentrycontext");

      // /no place for flag and reference
      stf.getExtensions(true).add(simpleComment);
      stf.getExtensions(true).add(potEntryHeader);
      return sr;
   }

   public ResourceMeta getResourceMeta()
   {
      ResourceMeta entity = new ResourceMeta();
      entity.setContentType(ContentType.TextPlain);
      entity.setName("test1");
      entity.setLang(new LocaleId("en-US"));
      entity.setType(ResourceType.FILE);
      entity.getExtensions(true);
      log.debug("create a new resource meta:" + entity.toString());
      return entity;
   }

   public ResourceMeta getPoHeaderResourceMeta()
   {
      ResourceMeta entity = getResourceMeta();
      entity.getExtensions(true).add(new PoHeader("comment", new HeaderEntry("ref", "test ref")));
      log.debug("create a new resource meta:" + entity.toString());
      return entity;
   }

}
