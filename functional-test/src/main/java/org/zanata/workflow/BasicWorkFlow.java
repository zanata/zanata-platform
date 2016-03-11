/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.workflow;

import org.openqa.selenium.support.PageFactory;
import org.zanata.page.AbstractPage;
import org.zanata.page.WebDriverFactory;
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
        return WebDriverFactory.INSTANCE.ignoringDswid(() -> {
            driver.get(toUrl(url));
            return PageFactory.initElements(driver, pageClass);
        });
    }

    public <P extends AbstractPage> P goToUrl(String url, Class<P> pageClass) {
        return WebDriverFactory.INSTANCE.ignoringDswid(() -> {
            driver.navigate().to(url);
            return PageFactory.initElements(driver, pageClass);
        });
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
