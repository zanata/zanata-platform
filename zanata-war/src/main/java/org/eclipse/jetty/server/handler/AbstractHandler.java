package org.eclipse.jetty.server.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * Workaround for unwanted logging caused by Liquibase.
 * http://stackoverflow.com/questions/26530677/why-failed-to-define-class-warn-from-server-startup.
 * https://liquibase.jira.com/browse/CORE-2024.
 *
 * @author cilap http://stackoverflow.com/a/29942016/14379
 */
@Slf4j
// TODO Remove when Liquibase CORE-2024 is fixed
public class AbstractHandler {
   public AbstractHandler() {
   }
}
