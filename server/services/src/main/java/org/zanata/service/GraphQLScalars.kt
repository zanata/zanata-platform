package org.zanata.service

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import org.zanata.common.ContentType
import org.zanata.common.LocaleId
import java.lang.reflect.Constructor

/**
 * T must have a public constructor which accepts a String, and a suitable toString() method.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

inline fun <reified T> coercingAsString() = coercingAsString(T::class.java)

fun <T> coercingAsString(tClass: Class<T>) = object :  Coercing<T, String> {
    private val ctor: Constructor<T> = tClass.getConstructor(java.lang.String::class.java)
            ?: throw IllegalArgumentException("Class must have a String constructor")

    override fun parseLiteral(input: Any): T? {
        return if (input is StringValue) {
            ctor.newInstance(input.value)
        } else null
    }

    override fun parseValue(input: Any): T {
        if (input is kotlin.String) {
            return ctor.newInstance(input)
        }
        throw CoercingParseValueException(
                "Expected type 'String' but was '" + input.javaClass.simpleName + "'.")
    }

    override fun serialize(input: Any): String {
        if (tClass.isInstance(input)) {
            return input.toString()
        }
        throw CoercingSerializeException(
                "Expected type '" + tClass.simpleName + "' but was '" + input.javaClass.simpleName + "'.")
    }

}

private val contentType = GraphQLScalarType("ContentType", "ContentType", coercingAsString<ContentType>())
private val localeId = GraphQLScalarType("LocaleId", "LocaleId", coercingAsString<LocaleId>())
internal val allScalarTypes: List<GraphQLType> = listOf(contentType, localeId)

