package org.zanata.util

import mu.KotlinLogging
import org.apache.commons.io.IOUtils.closeQuietly
import java.io.InputStream
import java.io.StringReader
import java.util.HashSet

/**
 * Returns process information about open server sockets.
 * Supports Linux only.
 */
object ProcessPortInfo {

    private val log = KotlinLogging.logger {}

    @JvmStatic
    fun getPortProcessInfo(portNum: Int): String = try {
        val pids = getProcessIdsForPort(portNum)
        if (pids.isEmpty()) {
            "<no processes found, or not accessible by current user>"
        } else {
            getProcessInfo(pids)
        }
    } catch (e: Exception) {
        log.warn("Unable to gather process/socket information", e)
        ""
    }

    private fun getProcessInfo(pids: Collection<Int>): String {
        val command = "/bin/ps -ww -o args -p " + pids.joinToString(",")
        val text = command.execute().text
        log.debug { "command: $command\noutput:\n$text" }
        return text
    }

    private fun getProcessIdsForPort(portNum: Int): Set<Int> {
        val pids = HashSet<Int>()
        // NB this may not work if the other process is owned by another user
        val command = "/sbin/ss --processes --listening --tcp --numeric sport = :" + portNum
        val text = command.execute().text
        log.info { "command: $command\noutput:\n$text" }

        val pidPattern = """.*pid=(\d+).*""".toPattern()
        text.forEachLine {
            val matcher = pidPattern.matcher(it)
            if (matcher.matches()) {
                pids.add(matcher.group(1).toInt())
            }
        }
        return pids
    }

    fun String.forEachLine(action: (String) -> Unit) =
            StringReader(this).forEachLine(action)

    fun String.execute(): Process =
            Runtime.getRuntime().exec(this)

    val Process.text: String
        get() {
            try {
                val stdout = inputStream.text
                // Process.exitValue() may hang if the process outputs too much
                // (32KB buffer?) to stdout OR STDERR before we read them.
                val code = waitFor()
                if (code == 0) {
                    return stdout
                } else {
                    throw Exception("execute failed with code: $code\nstdout: $stdout\nstderr: ${errorStream.text}")
                }
            } finally {
                closeStreams()
            }
        }

    private fun Process.closeStreams() {
        closeQuietly(errorStream)
        closeQuietly(inputStream)
        closeQuietly(outputStream)
    }

    val InputStream.text: String
        get() = bufferedReader().use { it.readText() }

    @JvmStatic
    fun main(args: Array<String>) {
        println(getPortProcessInfo(8180))
    }

}
