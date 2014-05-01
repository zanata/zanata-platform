package org.zanata.workflow;

import org.openqa.selenium.support.PageFactory;
import org.zanata.page.AbstractPage;
import org.zanata.page.webtrans.EditorPage;
import com.google.common.base.Preconditions;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class BasicWorkFlow extends AbstractWebWorkFlow {
    public static final String EDITOR_TEMPLATE = "webtrans/translate?project=%s&iteration=%s&localeId=%s&locale=en#view:doc;doc:%s";
    public static final String PROJECT_VERSION_TEMPLATE = "iteration/view/%s/%s";

    public <P extends AbstractPage> P goToPage(String url, Class<P> pageClass) {
        driver.get(toUrl(url));
        return PageFactory.initElements(driver, pageClass);
    }

    public <P extends AbstractPage> P goToUrl(String url, Class<P> pageClass) {
        driver.navigate().to(url);
        return PageFactory.initElements(driver, pageClass);
    }

    private String toUrl(String relativeUrl) {
        return hostUrl + removeLeadingSlash(relativeUrl);
    }

    private static String removeLeadingSlash(String relativeUrl) {
        if (relativeUrl.startsWith("/")) {
            return relativeUrl.substring(1, relativeUrl.length());
        }
        return relativeUrl;
    }
    public EditorPage goToEditor(String project, String version, String locale, String doc) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(version);
        Preconditions.checkNotNull(locale);
        Preconditions.checkNotNull(doc);
        return goToPage(String.format(
                BasicWorkFlow.EDITOR_TEMPLATE, project, version,
                locale, doc), EditorPage.class);
    }

}
