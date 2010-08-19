package net.openl10n.flies.rest.dto;

import junit.framework.TestCase;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.rest.dto.Account;
import net.openl10n.flies.rest.dto.Link;
import net.openl10n.flies.rest.dto.Links;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.Project;
import net.openl10n.flies.rest.dto.ProjectIteration;
import net.openl10n.flies.rest.dto.ProjectList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ToStringTests extends TestCase
{
   protected final Logger log = LoggerFactory.getLogger(getClass());

   public void testDTOs() throws Exception
   {
      // net.openl10n.flies.rest.dto
      test(new Account());
      test(new Link());
      test(new Links());
      test(new Person());
      test(new Project());
      test(new ProjectIteration());
      test(new ProjectList());
      // net.openl10n.flies.rest.dto.deprecated
      test(new net.openl10n.flies.rest.dto.deprecated.Document("full/path", ContentType.PO));
      test(new net.openl10n.flies.rest.dto.deprecated.Documents());
      test(new net.openl10n.flies.rest.dto.deprecated.SimpleComment());
      test(new net.openl10n.flies.rest.dto.deprecated.SimpleComments());
      test(new net.openl10n.flies.rest.dto.deprecated.TextFlow());
      test(new net.openl10n.flies.rest.dto.deprecated.TextFlowTarget());
      test(new net.openl10n.flies.rest.dto.deprecated.TextFlowTargets());
      // net.openl10n.flies.rest.dto.extensions
      test(new net.openl10n.flies.rest.dto.extensions.PoHeader());
      test(new net.openl10n.flies.rest.dto.extensions.PoTargetHeader());
      test(new net.openl10n.flies.rest.dto.extensions.PoTargetHeaderEntry());
      test(new net.openl10n.flies.rest.dto.extensions.PoTargetHeaders());
      test(new net.openl10n.flies.rest.dto.extensions.PotEntryHeader());
      test(new net.openl10n.flies.rest.dto.extensions.SimpleComment());
      // net.openl10n.flies.rest.dto.po
      test(new net.openl10n.flies.rest.dto.po.HeaderEntry());
      test(new net.openl10n.flies.rest.dto.po.PoHeader());
      test(new net.openl10n.flies.rest.dto.po.PoTargetHeader());
      test(new net.openl10n.flies.rest.dto.po.PoTargetHeaders());
      test(new net.openl10n.flies.rest.dto.po.PotEntryData());
      // net.openl10n.flies.rest.dto.resource
      test(new net.openl10n.flies.rest.dto.resource.ExtensionSet());
      test(new net.openl10n.flies.rest.dto.resource.Resource());
      test(new net.openl10n.flies.rest.dto.resource.ResourceMeta());
      test(new net.openl10n.flies.rest.dto.resource.ResourceMetaList());
      test(new net.openl10n.flies.rest.dto.resource.TextFlow());
      test(new net.openl10n.flies.rest.dto.resource.TextFlowTarget());
      test(new net.openl10n.flies.rest.dto.resource.TranslationsResource());
   }

   protected void test(Object obj)
   {
      String s = obj.toString();
      log.debug(s);
      assertTrue("expected xml but got: " + s, s.startsWith("<") && s.endsWith(">"));
   }
}
