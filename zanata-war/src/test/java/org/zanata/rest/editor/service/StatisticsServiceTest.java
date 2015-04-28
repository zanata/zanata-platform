package org.zanata.rest.editor.service;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@Test(groups = "unit-tests")
public class StatisticsServiceTest {
    private StatisticsService service;
    @Mock
    private DocumentDAO documentDAO;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new StatisticsService(documentDAO);
    }

    @Test
    public void getDocumentStatisticsWillReturnNotFoundIfDocumentNotFound() {
        when(documentDAO.getByProjectIterationAndDocId("a", "1", "authors"))
                .thenReturn(null);

        Response response =
                service.getDocumentStatistics("a", "1", "authors", "de");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getDocumentStatisticsWillReturnResult() {
        HDocument document = new HDocument();
        document.setId(1L);
        when(documentDAO.getByProjectIterationAndDocId("a", "1", "authors"))
                .thenReturn(document);
        ContainerTranslationStatistics statistics =
                new ContainerTranslationStatistics();
        statistics.addStats(new TranslationStatistics(new TransUnitWords(10, 0,
                0, 10, 0), "de"));
        statistics.addStats(new TranslationStatistics(new TransUnitCount(5, 0,
                0, 10, 0), "de"));
        when(documentDAO.getStatistics(1L, LocaleId.DE)).thenReturn(statistics);

        Response response =
                service.getDocumentStatistics("a", "1", "authors", "de");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(GenericEntity.class);
    }
}
