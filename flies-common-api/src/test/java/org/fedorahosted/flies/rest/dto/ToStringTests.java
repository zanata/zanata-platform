package org.fedorahosted.flies.rest.dto;

import junit.framework.TestCase;

import org.fedorahosted.flies.common.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ToStringTests extends TestCase
{
   protected final Logger log = LoggerFactory.getLogger(getClass());

   public void testDTOs() throws Exception
   {
      // org.fedorahosted.flies.rest.dto
      test(new Account());
      test(new Link());
      test(new Links());
      test(new Person());
      test(new Project());
      test(new ProjectIteration());
      test(new ProjectList());
      // org.fedorahosted.flies.rest.dto.deprecated
      test(new org.fedorahosted.flies.rest.dto.deprecated.Document("full/path", ContentType.PO));
      test(new org.fedorahosted.flies.rest.dto.deprecated.Documents());
      test(new org.fedorahosted.flies.rest.dto.deprecated.SimpleComment());
      test(new org.fedorahosted.flies.rest.dto.deprecated.SimpleComments());
      test(new org.fedorahosted.flies.rest.dto.deprecated.TextFlow());
      test(new org.fedorahosted.flies.rest.dto.deprecated.TextFlowTarget());
      test(new org.fedorahosted.flies.rest.dto.deprecated.TextFlowTargets());
      // org.fedorahosted.flies.rest.dto.extensions
      test(new org.fedorahosted.flies.rest.dto.extensions.PoHeader());
      test(new org.fedorahosted.flies.rest.dto.extensions.PoTargetHeader());
      test(new org.fedorahosted.flies.rest.dto.extensions.PoTargetHeaderEntry());
      test(new org.fedorahosted.flies.rest.dto.extensions.PoTargetHeaders());
      test(new org.fedorahosted.flies.rest.dto.extensions.PotEntryHeader());
      test(new org.fedorahosted.flies.rest.dto.extensions.SimpleComment());
      // org.fedorahosted.flies.rest.dto.po
      test(new org.fedorahosted.flies.rest.dto.po.HeaderEntry());
      test(new org.fedorahosted.flies.rest.dto.po.PoHeader());
      test(new org.fedorahosted.flies.rest.dto.po.PoTargetHeader());
      test(new org.fedorahosted.flies.rest.dto.po.PoTargetHeaders());
      test(new org.fedorahosted.flies.rest.dto.po.PotEntryData());
      // org.fedorahosted.flies.rest.dto.resource
      test(new org.fedorahosted.flies.rest.dto.resource.ExtensionSet());
      test(new org.fedorahosted.flies.rest.dto.resource.Resource());
      test(new org.fedorahosted.flies.rest.dto.resource.ResourceMeta());
      test(new org.fedorahosted.flies.rest.dto.resource.ResourceMetaList());
      test(new org.fedorahosted.flies.rest.dto.resource.TextFlow());
      test(new org.fedorahosted.flies.rest.dto.resource.TextFlowTarget());
      test(new org.fedorahosted.flies.rest.dto.resource.TranslationsResource());
   }

   protected void test(Object obj)
   {
      String s = obj.toString();
      log.debug(s);
      assertTrue("expected xml but got: " + s, s.startsWith("<") && s.endsWith(">"));
   }
}
