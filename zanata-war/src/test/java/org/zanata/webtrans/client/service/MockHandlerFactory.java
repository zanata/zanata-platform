package org.zanata.webtrans.client.service;

import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.server.rpc.GetTransUnitListHandler;
import org.zanata.webtrans.server.rpc.GetTransUnitsNavigationHandler;
import org.zanata.webtrans.shared.model.DocumentId;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockHandlerFactory
{

   //used by GetTransUnitListHandler and GetTransUnitsNavigationHandler
   @Mock
   private TextFlowDAO textFlowDAO;
   @Mock
   private LocaleService localeServiceImpl;
   @Mock
   private ResourceUtils resourceUtils;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private TextFlowSearchService textFlowSearchServiceImpl;

   public MockHandlerFactory()
   {
      MockitoAnnotations.initMocks(this);
   }

   public GetTransUnitListHandler createGetTransUnitListHandlerWithBehavior(DocumentId documentId, List<HTextFlow> hTextFlows, HLocale hLocale)
   {
      // @formatter:off
      GetTransUnitListHandler handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("textFlowDAO", textFlowDAO)
            .use("textFlowSearchServiceImpl", textFlowSearchServiceImpl)
            .use("localeServiceImpl", localeServiceImpl)
            .use("resourceUtils", resourceUtils)
            .autowire(GetTransUnitListHandler.class);
      // @formatter:on
      when(textFlowDAO.getTextFlows(documentId.getId())).thenReturn(hTextFlows);
      when(localeServiceImpl.validateLocaleByProjectIteration(any(LocaleId.class), anyString(), anyString())).thenReturn(hLocale);
      when(resourceUtils.getNumPlurals(any(HDocument.class), any(HLocale.class))).thenReturn(1);
      return handler;
   }


   public GetTransUnitsNavigationHandler createGetTransUnitsNavigationHandlerWithBehavior(DocumentId documentId, List<HTextFlow> hTextFlows, HLocale hLocale)
   {
      // @formatter:off
      GetTransUnitsNavigationHandler handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("textFlowDAO", textFlowDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .autowire(GetTransUnitsNavigationHandler.class);
      // @formatter:on
      when(textFlowDAO.getNavigationByDocumentId(documentId.getId())).thenReturn(hTextFlows);
      when(localeServiceImpl.validateLocaleByProjectIteration(any(LocaleId.class), anyString(), anyString())).thenReturn(hLocale);

      return handler;
   }
}
