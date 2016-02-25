/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.page;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DswidParamChecker provides a WebDriverEventListener which tracks dswid
 * parameters (used by DeltaSpike's window/conversation management). It logs
 * a warning when navigation loses the dswid parameter, and throws an
 * AssertionError if the dswid changes to a different value.
 * </p>
 * <p>
 * DswidParamChecker currently only supports a single browser tab, since
 * it assumes a single dswid value.
 * </p>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class DswidParamChecker {
    private static final Logger log = LoggerFactory.getLogger(DswidParamChecker.class);
    private final EventFiringWebDriver driver;
    private final WebDriverEventListener urlListener;
    private @Nullable String oldUrl;
    private @Nullable String oldDswid;

    /**
     * Creates a listener for the specified driver, but does not register it. See getEventListener().
     * @param driver
     */
    public DswidParamChecker(EventFiringWebDriver driver) {
        this.driver = driver;
        this.urlListener =
                (WebDriverEventListener) Proxy.newProxyInstance(
                        DswidParamChecker.class.getClassLoader(),
                        new Class<?>[]{ WebDriverEventListener.class },
                        this::invoke
                );
    }

    /**
     * Returns a WebDriverEventListener which will track dswid parameters. It can be registered like this:
     * driver.register(new DswidParamChecker(driver).getEventListener());
     */
    public WebDriverEventListener getEventListener() {
        return urlListener;
    }

    private Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String url = driver.getCurrentUrl();
        String query = new URL(url).getQuery();
        Optional<String> dswid = URLEncodedUtils.parse(query, UTF_8)
                .stream()
                .filter(p -> p.getName().equals("dswid"))
                .map(NameValuePair::getValue)
                .findFirst();
        if (oldDswid != null) {
            assert oldUrl != null;
            if (!dswid.isPresent()) {
                log.warn("missing dswid on transition from {} to {}",
                        oldUrl, url);
            } else {
                if (!oldDswid.equals(dswid.get())) {
                    throw new AssertionError(
                            "changed dswid on transition from " +
                                    oldUrl + " to " + url);
                }
            }
        }
        oldDswid = dswid.orElse(null);
        oldUrl = url;
        return null;
    }
}
