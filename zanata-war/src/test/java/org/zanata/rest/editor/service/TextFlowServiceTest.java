package org.zanata.rest.editor.service;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.rest.editor.dto.TransUnits;
import org.zanata.rest.service.ResourceUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.when;
import static org.zanata.model.TestFixture.makeHTextFlow;
import static org.zanata.model.TestFixture.makeTransUnit;

public class TextFlowServiceTest {
    private TextFlowService service;
    @Mock
    private TextFlowDAO textFlowDAO;
    private TransUnitUtils transUnitUtils;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        transUnitUtils = new TransUnitUtils(new ResourceUtils());
        service = new TextFlowService(textFlowDAO, transUnitUtils);
    }

    @Test
    public void willReturnEmptyIfIdIsEmpty() {
        Response response = service.get("");
        assertThat(response.getStatus()).isEqualTo(200);

        TransUnits transUnits = (TransUnits) response.getEntity();
        assertThat(transUnits).isEmpty();
    }

    @Test
    public void willReturnForbiddenIfIdIsOverSizeLimit() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < TransUnitUtils.MAX_SIZE; i++) {
            builder.append(i).append(",");
        }
        builder.append("9999");
        Response response = service.get(builder.toString());
        assertThat(response.getStatus()).isEqualTo(
                Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void willReturnList() {
        HLocale targetLocale = new HLocale(LocaleId.DE);
        when(textFlowDAO.findByIdList(newArrayList(1L, 2L)))
                .thenReturn(
                        newArrayList(
                                makeHTextFlow(1, targetLocale,
                                        ContentState.NeedReview),
                                makeHTextFlow(2, targetLocale,
                                        ContentState.Translated)));
        Response response = service.get("1,2");

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
