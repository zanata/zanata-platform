package net.openl10n.flies.rest.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.comment.SimpleComment;
import net.openl10n.flies.rest.dto.extensions.gettext.HeaderEntry;
import net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

public class TranslationsResourceTestObjectFactory
{
   private final Logger log = LoggerFactory.getLogger(TranslationsResourceTestObjectFactory.class);

   public TranslationsResource getTestObject()
   {
      TranslationsResource entity = new TranslationsResource();
      TextFlowTarget target = new TextFlowTarget("rest1");
      target.setContent("hello world");
      target.setState(ContentState.Approved);
      target.setTranslator(new Person("root@localhost", "Admin user"));
      // for the convenience of test
      target.getExtensions(true);
      entity.getTextFlowTargets().add(target);
      entity.getExtensions(true);
      return entity;
   }

   public TranslationsResource getTestObject2()
   {
      TranslationsResource entity = new TranslationsResource();
      TextFlowTarget target = new TextFlowTarget("rest1");
      target.setContent("hello world");
      target.setState(ContentState.Approved);
      target.setTranslator(new Person("root@localhost", "Admin user"));
      target.getExtensions(true);
      TextFlowTarget target2 = new TextFlowTarget("rest2");
      target2.setContent("greeting world");
      target2.setState(ContentState.Approved);
      target2.setTranslator(new Person("root@localhost", "Admin user"));
      target2.getExtensions(true);
      entity.getTextFlowTargets().add(target2);
      entity.getExtensions(true);
      log.debug(entity.toString());
      return entity;
   }

   public TranslationsResource getTextFlowTargetCommentTest()
   {
      TranslationsResource sr = getTestObject();
      TextFlowTarget stf = sr.getTextFlowTargets().get(0);

      SimpleComment simpleComment = new SimpleComment("textflowtarget comment");

      stf.getExtensions(true).add(simpleComment);
      return sr;
   }

   public TranslationsResource getPoTargetHeaderTextFlowTargetTest()
   {
      TranslationsResource sr = getTestObject();
      PoTargetHeader poTargetHeader = new PoTargetHeader("target header comment", new HeaderEntry("ht", "vt1"), new HeaderEntry("th2", "tv2"));

      sr.getExtensions(true).add(poTargetHeader);
      return sr;
   }

}
