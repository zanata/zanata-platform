package org.zanata.page.webtrans;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.google.common.base.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequiredArgsConstructor
public class LoadingPanel implements Predicate<WebDriver> {
    private final boolean appear;

    public static LoadingPanel appear() {
        return new LoadingPanel(true);
    }

    public static LoadingPanel disappear() {
        return new LoadingPanel(false);
    }

    @Override
    public boolean apply(WebDriver input) {
        return appear == input.findElement(By.id("loading-panel"))
                .isDisplayed();
    }
}
