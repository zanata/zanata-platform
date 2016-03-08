package org.zanata.util

import groovy.util.logging.Slf4j

/**
 * Returns process information about open server sockets.
 * Supports Linux only.
 */
@Slf4j
class ProcessPortInfo {

    static String getPortProcessInfo(int portNum) {
        try {
            def pids = getProcessIdsForPort(portNum)
            if (pids.isEmpty()) {
                return "<no processes found, or not accessible by current user>"
            }
            return getProcessInfo(pids)
        } catch (Exception e) {
            log.warn("Unable to gather process/socket information", e)
        }
    }

    static String getProcessInfo(Collection pids) {
        def command = '/bin/ps -ww -o args -p ' + pids.join(',')
        def text = command.execute().text
        log.debug("command: ${command}\noutput:\n${text}")
        return text
    }

    static Set<Integer> getProcessIdsForPort(int portNum) {
        def pids = new HashSet()
        // NB this may not work if the other process is owned by another user
        def command = '/sbin/ss --processes --listening --tcp --numeric sport = :' + portNum
        def text = command.execute().text
        log.info("command: ${command}\noutput:\n${text}")

        def pidPattern = ~/.*pid=(\d+).*/
        text.eachLine {
            def matcher = it =~ pidPattern
            if (matcher.matches()) {
                pids.add(matcher.group(1).toInteger())
            }
        }
        return pids;
    }

    static void main(def args) {
        println(getPortProcessInfo(8180))
    }

}
