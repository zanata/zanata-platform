package org.zanata.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException


/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@JvmOverloads
fun toJson(json: String, pretty: Boolean = false): String {
    return toJson(pretty) { mapper -> mapper.readValue(json, Any::class.java) }
}
@JvmOverloads
fun toJson(jsonMap: Map<String, Any>, pretty: Boolean = false) = toJson(pretty) { jsonMap }

private fun toJson(pretty: Boolean, valueProducer: (ObjectMapper) -> Any): String {
    try {
        val mapper = ObjectMapper()
        val value = valueProducer.invoke(mapper)
        return if (pretty)
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)
        else
            mapper.writeValueAsString(value)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
}

fun toMap(json: String): Map<String, Any> =
        ObjectMapper().readValue(json, object : TypeReference<Map<String, Any>>() {})
