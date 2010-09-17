package net.openl10n.flies.rest.service;


import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.comment.SimpleComment;
import net.openl10n.flies.rest.dto.extensions.gettext.HeaderEntry;
import net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

public class TranslationsResourceTestObjectFactory
{
   private static TranslationsResourceTestObjectFactory me = new TranslationsResourceTestObjectFactory();

   private TranslationsResourceTestObjectFactory()
   {

   }

   public static TranslationsResourceTestObjectFactory getInstance()
   {
      return me;
   }

   public TranslationsResource getTestObject()
   {
      TranslationsResource entity = new TranslationsResource();
      TextFlowTarget target = new TextFlowTarget("rest1");
      target.setContent("hello world");
      target.setState(ContentState.Approved);
      target.setTranslator(new Person("root@localhost", "Admin user"));
      // for the convenience of test
      target.getExtensions(true);
      entity.getTextFlowTargets(true).add(target);
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
      entity.getTextFlowTargets(true).add(target2);
      entity.getExtensions(true);
      return entity;
   }

   public TranslationsResource getTextFlowTargetCommentTest()
   {
      TranslationsResource sr = getTestObject();
      TextFlowTarget stf = sr.getTextFlowTargets(true).get(0);

      SimpleComment<TextFlowTarget> simpleComment = new SimpleComment<TextFlowTarget>("textflowtarget comment");

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
