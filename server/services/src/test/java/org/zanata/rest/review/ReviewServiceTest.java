package org.zanata.rest.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataJpaTest;
import org.zanata.common.IssuePriority;
import org.zanata.dao.ReviewCriteriaDAO;
import org.zanata.model.ReviewCriteria;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.rest.dto.TransReviewCriteria;

public class ReviewServiceTest extends ZanataJpaTest {

    private static final String DESCRIPTION = "bad grammar";
    private static final String SERVER_PATH = "https://localhost/zanata/";
    private ReviewService reviewService;
    @Mock
    private UriInfo uriInfo;
    private UrlUtil urlUtil = new UrlUtil(SERVER_PATH, "/zanata", null, null, null);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        reviewService = new ReviewService(new ReviewCriteriaDAO(getSession()), uriInfo, urlUtil);
    }

    @Test
    public void canGetAllCriteria() {
        ReviewCriteria criteria =
                new ReviewCriteria(IssuePriority.Critical, false, DESCRIPTION);
        getEm().persist(criteria);

        Response response = reviewService.getAllCriteria();

        @SuppressWarnings("unchecked")
        List<TransReviewCriteria> entity =
                (List<TransReviewCriteria>) response.getEntity();
        assertThat(entity).hasSize(1);
        assertThat(entity.get(0).getDescription()).isEqualTo(DESCRIPTION);
    }

    @Test
    public void canAddNewEntry() throws Exception {
        when(uriInfo.getPath())
                .thenReturn("criteria");
        URI baseUri = new URI(SERVER_PATH +"rest/");
        when(uriInfo.getBaseUri())
                .thenReturn(baseUri);
        TransReviewCriteria dto = new TransReviewCriteria(null,
                IssuePriority.Critical, DESCRIPTION, false);
        Response response = reviewService.addCriteria(dto);
        TransReviewCriteria entity = (TransReviewCriteria) response.getEntity();
        assertThat(entity.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(entity.getId()).isNotNull();
        assertThat(response.getLocation().toString())
                .isEqualTo(SERVER_PATH + "rest/criteria/" + entity.getId());
    }

    @Test
    public void canEditEntry() {
        ReviewCriteria criteria =
                new ReviewCriteria(IssuePriority.Critical, false, DESCRIPTION);
        getEm().persist(criteria);

        TransReviewCriteria dto =
                new TransReviewCriteria(criteria.getId(), IssuePriority.Major,
                        "bad", false);
        Response response = reviewService.editCriteria(criteria.getId(),
                dto);
        TransReviewCriteria entity = (TransReviewCriteria) response.getEntity();
        assertThat(entity.getDescription()).isEqualTo("bad");
        assertThat(entity.getId()).isEqualTo(criteria.getId());

        ReviewCriteria updated =
                getEm().find(ReviewCriteria.class, criteria.getId());
        assertThat(updated.getPriority()).isEqualTo(IssuePriority.Major);
    }

    @Test
    public void canDeleteEntry() {
        ReviewCriteria criteria =
                new ReviewCriteria(IssuePriority.Critical, false, DESCRIPTION);
        getEm().persist(criteria);

        Response response = reviewService.deleteCriteria(criteria.getId());

        TransReviewCriteria entity = (TransReviewCriteria) response.getEntity();
        assertThat(entity.getId()).isEqualTo(criteria.getId());

        ReviewCriteria updated =
                getEm().find(ReviewCriteria.class, criteria.getId());
        assertThat(updated).isNull();
    }


}
