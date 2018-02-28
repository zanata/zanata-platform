package org.zanata.rest.editor.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.server.rpc.GetTranslationHistoryHandler;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class TransUnitHistoryServiceTest {
    private TransUnitHistoryService service;
    @Mock
    private GetTranslationHistoryHandler historyHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        historyHandler = new GetTranslationHistoryHandler();
        service = new TransUnitHistoryService(historyHandler);
    }

    @Test
    public void nullOrEmptyLocaleWillReturnBadRequest() {
        Long transUnitID = 1L;
        Response response = service.get(null, transUnitID);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void nullTransUnitIdWillReturnBadRequest() {
        String localeId = "ja";
        Response response = service.get(localeId, null);
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
