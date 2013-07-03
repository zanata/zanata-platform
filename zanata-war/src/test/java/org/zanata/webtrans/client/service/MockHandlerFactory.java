package org.zanata.webtrans.client.service;

import java.util.List;

import org.hibernate.transform.ResultTransformer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.seam.SeamAutowire;
import org.zanata.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.rpc.GetTransUnitListHandler;
import org.zanata.webtrans.shared.model.DocumentId;

import lombok.extern.slf4j.Slf4j;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class MockHandlerFactory
{

   //used by GetTransUnitListHandler
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
   @Mock
   private ValidationService validationServiceImpl;

   public MockHandlerFactory()
   {
      MockitoAnnotations.initMocks(this);
   }

   public GetTransUnitListHandler createGetTransUnitListHandlerWithBehavior(DocumentId documentId, List<HTextFlow> hTextFlows, HLocale hLocale, int startIndex, int count)
   {
      // @formatter:off
      GetTransUnitListHandler handler = SeamAutowire.instance().reset()
            .use("identity", identity)
            .use("textFlowDAO", textFlowDAO)
            .use("textFlowSearchServiceImpl", textFlowSearchServiceImpl)
            .use("localeServiceImpl", localeServiceImpl)
            .use("resourceUtils", resourceUtils)
            .use("validationServiceImpl", validationServiceImpl)
            .autowire(GetTransUnitListHandler.class);
      // @formatter:on

      int maxSize = Math.min(startIndex + count, hTextFlows.size());
      when(textFlowDAO.getTextFlowsByDocumentId(documentId, hLocale, startIndex, count)).thenReturn(hTextFlows.subList(startIndex, maxSize));
      when(localeServiceImpl.validateLocaleByProjectIteration(any(LocaleId.class), anyString(), anyString())).thenReturn(hLocale);
      when(resourceUtils.getNumPlurals(any(HDocument.class), any(HLocale.class))).thenReturn(1);

      // trans unit navigation index handler
      when(textFlowDAO.getNavigationByDocumentId(eq(documentId.getId()), eq(hLocale), isA(ResultTransformer.class), isA(FilterConstraints.class))).thenReturn(hTextFlows);
      return handler;
   }
}
