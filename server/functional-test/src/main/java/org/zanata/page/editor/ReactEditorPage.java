/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.CorePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ReactEditorPage extends CorePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ReactEditorPage.class);

    private By headerElement = new By.ById("editor-header");
    private By transUnitText = new By.ByClassName("TransUnit-text");

    public ReactEditorPage(WebDriver driver) {
        super(driver);
    }

    // TODO: find out why there's a background request, and either amend or override waitForPageSilence
    @Override
    protected int getExpectedBackgroundRequests() {
        return 1;
    }

    public ReactEditorPage switchToEditorWindow() {
        log.info("Switching to new window (from {})", getDriver().getWindowHandle());
        String mainHandle = getDriver().getWindowHandle();
        waitForAMoment().withMessage("second window to be present")
                .until(it -> getAllWindowHandles().size() > 1);
        Set<String> allWindowHandles = new HashSet<>(getAllWindowHandles());
        log.info("main window handle: {}", mainHandle);
        for (Iterator<String> iter = allWindowHandles.iterator(); iter.hasNext(); ) {
            String handle = iter.next();
            if (handle.equals(mainHandle)) {
                log.info("stripping main handle: {}", mainHandle);
                iter.remove();
            }
        }
        String handle = allWindowHandles.stream()
                .filter(it -> !it.equals(mainHandle))
                .findAny()
                .get();
        log.info("found target window: {}", handle);
        waitForAMoment().withMessage("waiting for window")
                .until(it -> {
                    getDriver().switchTo().window(handle);
                    return getDriver().getWindowHandle().equals(handle);
                });
        waitForPageSilence();
        waitForAMoment().withMessage("waiting for editor")
                .until(it -> {
                    if (isReactEditor()) {
                        log.info("React Editor Window: {}",
                                getDriver().getWindowHandle());
                        return true;
                    }
                    return false;
                });
        return new ReactEditorPage(getDriver());
    }

    public boolean isReactEditor() {
        log.info("Query is React editor visible");
        return getDriver().findElements(headerElement).size() > 0;
    }

    public List<WebElement> getTransunitTargets() {
        log.info("Query Transunit targets");
        List<WebElement> targets = new ArrayList<>();
        for (WebElement element : getTextUnits()) {
            if (element.getTagName().trim().equals("textarea")) {
                targets.add(element);
            }
        }
        return targets;
    }

    private List<WebElement> getTextUnits() {
        return getDriver().findElements(transUnitText);
    }

    public void expectNumberOfTargets(int expected) {
        log.info("Expect number of translation target is {}", expected);
        waitForAMoment().withMessage("Expected number of targets is shown")
                .until(it -> getTransunitTargets().size() == expected);
    }
}
