/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.commons.lang

import kotlin.Throws
import kotlin.jvm.Synchronized
import tachyon.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import tachyon.commons.collection.LongKeyList.Pair
import tachyon.commons.collection.AbstractCollection
import tachyon.runtime.type.Array
import java.sql.Array
import tachyon.commons.lang.Pair
import tachyon.runtime.exp.CatchBlockImpl.Pair
import tachyon.runtime.type.util.ListIteratorImpl
import tachyon.runtime.type.Lambda
import java.util.Random
import tachyon.runtime.config.Constants
import tachyon.runtime.engine.Request
import tachyon.runtime.engine.ExecutionLogSupport.Pair
import tachyon.runtime.functions.other.NullValue
import tachyon.runtime.functions.string.Val
import tachyon.runtime.reflection.Reflector.JavaAnnotation
import tachyon.transformer.cfml.evaluator.impl.Output
import tachyon.transformer.cfml.evaluator.impl.Property
import tachyon.transformer.bytecode.statement.Condition.Pair

object CFTypes {
    /**
     * Field `TYPE_ANY`
     */
    const val TYPE_ANY: Short = 0 // do not change used in extension

    /**
     * Field `TYPE_ARRAY`
     */
    const val TYPE_ARRAY: Short = 1

    /**
     * Field `TYPE_BOOLEAN`
     */
    const val TYPE_BASE64: Short = 20

    /**
     * Field `TYPE_BOOLEAN`
     */
    const val TYPE_BOOLEAN: Short = 2 // do not change used in extension

    /**
     * Field `TYPE_BINARY`
     */
    const val TYPE_BINARY: Short = 3

    /**
     * Field `TYPE_DATETIME`
     */
    const val TYPE_DATETIME: Short = 4

    /**
     * Field `TYPE_NUMERIC`
     */
    const val TYPE_NUMERIC: Short = 5

    /**
     * Field `TYPE_QUERY`
     */
    const val TYPE_QUERY: Short = 6

    /**
     * Field `TYPE_STRING`
     */
    const val TYPE_STRING: Short = 7 // do not change used in extension

    /**
     * Field `TYPE_STRUCT`
     */
    const val TYPE_STRUCT: Short = 8

    /**
     * Field `TYPE_TIMESPAN`
     */
    const val TYPE_TIMESPAN: Short = 9

    /**
     * Field `TYPE_UUID`
     */
    const val TYPE_UUID: Short = 10

    /**
     * Field `TYPE_VARIABLE_NAME`
     */
    const val TYPE_VARIABLE_NAME: Short = 11

    /**
     * Field `TYPE_VARIABLE_STRING`
     */
    const val TYPE_VARIABLE_STRING: Short = 12

    /**
     * Field `TYPE_UNKNOW`
     */
    const val TYPE_UNKNOW: Short = -1 // do not change used in extension

    /**
     * Field `TYPE_UNKNOW`
     */
    const val TYPE_UNDEFINED: Short = 14

    /**
     * Field `TYPE_VOID`
     */
    const val TYPE_VOID: Short = 15 // do never change this is hardcoded in flex extension

    /**
     * Field `TYPE_XML`
     */
    const val TYPE_XML: Short = 16

    // public static final short TYPE_SIZE = 21;
    const val TYPE_GUID: Short = 22
    const val TYPE_FUNCTION: Short = 23
    const val TYPE_QUERY_COLUMN: Short = 24
    const val TYPE_IMAGE: Short = 25 // used in extension image
    const val TYPE_LOCALE: Short = 26
    const val TYPE_TIMEZONE: Short = 27

    /**
     * Wandelt einen String Datentypen in ein CFML short Typ um.
     *
     * @param type
     * @param defaultValue
     * @return short Data Type
     */
    fun toString(type: Int, defaultValue: String): String {
        when (type) {
            TYPE_ANY -> return "any"
            TYPE_ARRAY -> return "array"
            TYPE_BASE64 -> return "base64"
            TYPE_BINARY -> return "binary"
            TYPE_BOOLEAN -> return "boolean"
            TYPE_DATETIME -> return "datetime"
            TYPE_GUID -> return "guid"
            TYPE_IMAGE -> return "image"
            TYPE_NUMERIC -> return "numeric"
            TYPE_QUERY -> return "query"
            TYPE_QUERY_COLUMN -> return "querycolumn"
            TYPE_STRING -> return "string"
            TYPE_STRUCT -> return "struct"
            TYPE_TIMESPAN -> return "timespan"
            TYPE_UNDEFINED -> return "any"
            TYPE_UNKNOW -> return "any"
            TYPE_UUID -> return "uuid"
            TYPE_VARIABLE_NAME -> return "variablename"
            TYPE_VARIABLE_STRING -> return "variablestring"
            TYPE_VOID -> return "void"
            TYPE_XML -> return "xml"
            TYPE_LOCALE -> return "locale"
            TYPE_TIMEZONE -> return "timezone"
            TYPE_FUNCTION -> return "function"
        }
        return defaultValue
    }

    fun toShortStrict(type: String, defaultValue: Short): Short {
        var type = type
        type = type.toLowerCase().trim()
        if (type.length() > 2) {
            val first: Char = type.charAt(0)
            when (first) {
                'a' -> {
                    if (type.equals("any")) return TYPE_ANY
                    if (type.equals("array")) return TYPE_ARRAY
                }
                'b' -> {
                    if (type.equals("boolean") || type.equals("bool")) return TYPE_BOOLEAN
                    if (type.equals("binary")) return TYPE_BINARY
                }
                'd' -> {
                    if (type.equals("date") || type.equals("datetime")) return TYPE_DATETIME
                    if (type.equals("function")) return TYPE_FUNCTION
                }
                'f' -> if (type.equals("function")) return TYPE_FUNCTION
                'g' -> if ("guid".equals(type)) return TYPE_GUID
                'i' -> if ("image".equals(type)) return TYPE_IMAGE
                'n' -> if (type.equals("numeric")) return TYPE_NUMERIC else if (type.equals("number")) return TYPE_NUMERIC
                'l' -> if (type.equals("locale")) return TYPE_LOCALE
                'o' -> if (type.equals("object")) return TYPE_ANY
                'q' -> {
                    if (type.equals("query")) return TYPE_QUERY
                    if (type.equals("querycolumn")) return TYPE_QUERY_COLUMN
                }
                's' -> if (type.equals("string")) return TYPE_STRING else if (type.equals("struct")) return TYPE_STRUCT
                't' -> {
                    if (type.equals("timespan")) return TYPE_TIMESPAN
                    if (type.equals("time")) return TYPE_DATETIME
                    if (type.equals("timestamp")) return TYPE_DATETIME
                    if (type.equals("timezone")) return TYPE_TIMEZONE
                }
                'u' -> if (type.equals("uuid")) return TYPE_UUID
                'v' -> {
                    if (type.equals("variablename")) return TYPE_VARIABLE_NAME
                    if (type.equals("variable_name")) return TYPE_VARIABLE_NAME
                    if (type.equals("variablestring")) return TYPE_VARIABLE_STRING
                    if (type.equals("variable_string")) return TYPE_VARIABLE_STRING
                    if (type.equals("void")) return TYPE_VOID
                }
                'x' -> if (type.equals("xml")) return TYPE_XML
            }
        }
        return defaultValue
    }

    fun toShort(type: String, alsoAlias: Boolean, defaultValue: Short): Short {
        var type = type
        type = type.toLowerCase().trim()
        if (type.length() > 2) {
            val first: Char = type.charAt(0)
            when (first) {
                'a' -> {
                    if (type.equals("any")) return TYPE_ANY
                    if (type.equals("array")) return TYPE_ARRAY
                }
                'b' -> {
                    if (type.equals("boolean") || alsoAlias && type.equals("bool")) return TYPE_BOOLEAN
                    if (type.equals("binary")) return TYPE_BINARY
                    if (alsoAlias && type.equals("bigint")) return TYPE_NUMERIC
                    if ("base64".equals(type)) return TYPE_STRING
                }
                'c' -> if (alsoAlias && "char".equals(type)) return TYPE_STRING
                'd' -> {
                    if (alsoAlias && "double".equals(type)) return TYPE_NUMERIC
                    if (alsoAlias && "decimal".equals(type)) return TYPE_STRING
                    if (type.equals("date") || type.equals("datetime")) return TYPE_DATETIME
                }
                'e' -> if ("eurodate".equals(type)) return TYPE_DATETIME
                'f' -> {
                    if (alsoAlias && "float".equals(type)) return TYPE_NUMERIC
                    if ("function".equals(type)) return TYPE_FUNCTION
                }
                'g' -> if ("guid".equals(type)) return TYPE_GUID
                'i' -> if (alsoAlias && ("int".equals(type) || "integer".equals(type))) return TYPE_NUMERIC
                'l' -> {
                    if (alsoAlias && "long".equals(type)) return TYPE_NUMERIC
                    if ("locale".equals(type)) return TYPE_LOCALE
                }
                'n' -> {
                    if (type.equals("numeric")) return TYPE_NUMERIC else if (type.equals("number")) return TYPE_NUMERIC
                    if (alsoAlias) {
                        if (type.equals("node")) return TYPE_XML else if (type.equals("nvarchar")) return TYPE_STRING else if (type.equals("nchar")) return TYPE_STRING
                    }
                }
                'o' -> {
                    if (type.equals("object")) return TYPE_ANY
                    if (alsoAlias && type.equals("other")) return TYPE_ANY
                }
                'q' -> {
                    if (type.equals("query")) return TYPE_QUERY
                    if (type.equals("querycolumn")) return TYPE_QUERY_COLUMN
                }
                's' -> {
                    if (type.equals("string")) return TYPE_STRING else if (type.equals("struct")) return TYPE_STRUCT
                    if (alsoAlias && "short".equals(type)) return TYPE_NUMERIC
                }
                't' -> {
                    if (type.equals("timespan")) return TYPE_TIMESPAN
                    if (type.equals("timezone")) return TYPE_TIMEZONE
                    if (type.equals("time")) return TYPE_DATETIME
                    if (alsoAlias && type.equals("timestamp")) return TYPE_DATETIME
                    if (alsoAlias && type.equals("text")) return TYPE_STRING
                }
                'u' -> {
                    if (type.equals("uuid")) return TYPE_UUID
                    if (alsoAlias && "usdate".equals(type)) return TYPE_DATETIME
                    if (alsoAlias && "udf".equals(type)) return TYPE_FUNCTION
                }
                'v' -> {
                    if (type.equals("variablename")) return TYPE_VARIABLE_NAME
                    if (alsoAlias && type.equals("variable_name")) return TYPE_VARIABLE_NAME
                    if (type.equals("variablestring")) return TYPE_VARIABLE_STRING
                    if (alsoAlias && type.equals("variable_string")) return TYPE_VARIABLE_STRING
                    if (type.equals("void")) return TYPE_VOID
                    if (alsoAlias && type.equals("varchar")) return TYPE_STRING
                }
                'x' -> if (type.equals("xml")) return TYPE_XML
            }
        }
        return defaultValue
    }

    fun isSimpleType(type: Short): Boolean {
        return type == TYPE_BOOLEAN || type == TYPE_DATETIME || type == TYPE_NUMERIC || type == TYPE_STRING
    }
}