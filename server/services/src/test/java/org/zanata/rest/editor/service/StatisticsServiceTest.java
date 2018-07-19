package org.zanata.rest.editor.service;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.TranslationSourceType;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.test.EntityTestData.setId;

public class StatisticsServiceTest {
    private StatisticsService service;
    @Mock
    private DocumentDAO documentDAO;

    @Before
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
        setId(document, 1L);
        when(documentDAO.getByProjectIterationAndDocId("a", "1", "authors"))
                .thenReturn(document);

        int[] mtStats = new int[2];
        mtStats[0] = 1;
        mtStats[1] = 2;
        when(documentDAO.getStatisticsBySourceType(1L, LocaleId.DE,
                TranslationSourceType.MACHINE_TRANS)).thenReturn(mtStats);

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
    }
}
