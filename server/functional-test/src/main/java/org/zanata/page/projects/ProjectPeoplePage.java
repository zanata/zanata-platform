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
package org.zanata.page.projects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectPeoplePage extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectPeoplePage.class);
    private By peopleList = By.id("people");

    public ProjectPeoplePage(WebDriver driver) {
        super(driver);
    }

    public List<String> getPeople() {
        log.info("Query maintainers list");
        List<String> names = new ArrayList<>();
        for (WebElement row : readyElement(peopleList)
                .findElements(By.tagName("li"))) {
            String username = row.findElement(By.tagName("a")).getText().trim();
            String roles = "";
            for (WebElement role : row
                    .findElements(By.className("txt--understated"))) {
                roles = roles.concat(role.getText().trim() + ";");
            }
            names.add(username + "|" + roles);
        }
        return names;
    }
}
