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
package org.zanata.workflow

import org.openqa.selenium.support.PageFactory
import org.zanata.page.AbstractPage
import org.zanata.page.WebDriverFactory
import org.zanata.page.webtrans.EditorPage
import com.google.common.base.Preconditions
import java.util.function.Supplier

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class BasicWorkFlow : AbstractWebWorkFlow() {

    fun <P : AbstractPage> goToPage(url: String, pageClass: Class<P>): P {
        return WebDriverFactory.INSTANCE.ignoringDswid(Supplier {
            driver.get(toUrl(url))
            return@Supplier PageFactory.initElements(driver, pageClass)
        })
    }

    fun <P : AbstractPage> goToUrl(url: String, pageClass: Class<P>): P {
        log.info("Navigating directly to page {}", url)
        return WebDriverFactory.INSTANCE.ignoringDswid(Supplier {
            driver.navigate().to(url)
            return@Supplier PageFactory.initElements(driver, pageClass)
        })
    }

    private fun toUrl(relativeUrl: String): String {
        return hostUrl + removeLeadingSlash(relativeUrl)
    }

    fun goToEditor(project: String, version: String, locale: String, doc: String): EditorPage {
        Preconditions.checkNotNull(project)
        Preconditions.checkNotNull(version)
        Preconditions.checkNotNull(locale)
        Preconditions.checkNotNull(doc)
        return goToPage(String.format(
                BasicWorkFlow.EDITOR_TEMPLATE, project, version,
                locale, doc), EditorPage::class.java)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(BasicWorkFlow::class.java)

        const val EDITOR_TEMPLATE = "webtrans/translate?project=%s&iteration=%s&localeId=%s&locale=en#view:doc;doc:%s"
        const val PROJECT_VERSION_TEMPLATE = "iteration/view/%s/%s"

        private fun removeLeadingSlash(relativeUrl: String): String {
            return if (relativeUrl.startsWith("/")) {
                relativeUrl.substring(1, relativeUrl.length)
            } else relativeUrl
        }
    }

}
