package org.zanata.rest.editor.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.server.rpc.GetTranslationHistoryHandler;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TransUnitHistoryServiceTest {
    private TransUnitHistoryService service;
    @Mock
    private GetTranslationHistoryHandler historyHandler;

    private String localeId = "ja";
    private Long transUnitId = 1L;
    private String projectSlug = "project88";
    private String versionSlug = "version99";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new TransUnitHistoryService(historyHandler);
    }

    @Test
    public void nullOrEmptyLocaleWillReturnBadRequest() {
        Response response = service.get(
                null, transUnitId, projectSlug, versionSlug);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void nullTransUnitIdWillReturnBadRequest() {
        Response response = service.get(
                localeId, null, projectSlug, versionSlug);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void nullProjectSlugWillReturnBadRequest() {
        Response response = service.get(
                localeId, transUnitId, null, versionSlug);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void nullVersionSlugWillReturnBadRequest() {
        Response response = service.get(
                localeId, transUnitId, projectSlug, null);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void validParamsReturnSuccess() {
        GetTranslationHistoryResult historyResult =
                new GetTranslationHistoryResult(null, null, null);
        when(historyHandler
                .getTranslationHistory(localeId, transUnitId, projectSlug,
                        versionSlug)).thenReturn(historyResult);
        Response response = service.get(
                localeId, transUnitId, projectSlug, versionSlug);
        assertThat(response.getStatus()).isEqualTo(200);
    }

}
