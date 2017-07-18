package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;
import org.zanata.webtrans.shared.rpc.PreviewReplaceText;
import org.zanata.webtrans.shared.rpc.PreviewReplaceTextResult;
import org.zanata.webtrans.shared.rpc.ReplaceText;

import net.customware.gwt.dispatch.shared.ActionException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class PreviewReplaceTextHandlerTest extends ZanataTest {
    @Inject @Any
    private PreviewReplaceTextHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        TransUnit transUnit =
                TestFixture.makeTransUnit(1, ContentState.NeedReview, "target");
        PreviewReplaceText action =
                new PreviewReplaceText(new ReplaceText(transUnit, "target",
                        "replace", true));

        PreviewReplaceTextResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getPreviews(), Matchers.hasSize(1));
        TransUnitUpdatePreview preview = result.getPreviews().get(0);
        assertThat(preview.getId(), Matchers.equalTo(transUnit.getId()));
        assertThat(preview.getState(),
                Matchers.equalTo(ContentState.NeedReview));
        assertThat(preview.getContents(), Matchers.contains("replace"));
    }

    @Test(expected = ActionException.class)
    @InRequestScope
    public void cannotRollback() throws ActionException {
        handler.rollback(null, null, null);
    }
}
