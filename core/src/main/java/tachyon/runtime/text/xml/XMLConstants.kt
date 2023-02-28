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
package tachyon.runtime.text.xml

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

interface XMLConstants {
    companion object {
        val NON_VALIDATING_DTD_GRAMMAR: String? = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar"
        val NON_VALIDATING_DTD_EXTERNAL: String? = "http://apache.org/xml/features/nonvalidating/load-external-dtd"
        val VALIDATION_SCHEMA: String? = "http://apache.org/xml/features/validation/schema"
        val VALIDATION_SCHEMA_FULL_CHECKING: String? = "http://apache.org/xml/features/validation/schema-full-checking"
        val FEATURE_DISALLOW_DOCTYPE_DECL: String? = "http://apache.org/xml/features/disallow-doctype-decl"
        val FEATURE_EXTERNAL_GENERAL_ENTITIES: String? = "http://xml.org/sax/features/external-general-entities"
        val FEATURE_EXTERNAL_PARAMETER_ENTITIES: String? = "http://xml.org/sax/features/external-parameter-entities"
        val FEATURE_NONVALIDATING_LOAD_EXTERNAL_DTD: String? = "http://apache.org/xml/features/nonvalidating/load-external-dtd"

        // public static final String ACCESS_EXTERNAL_DTD = javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
        // public static final String ACCESS_EXTERNAL_SCHEMA =
        // javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
        val ACCESS_EXTERNAL_DTD: String? = "http://javax.xml.XMLConstants/property/accessExternalDTD"
        val ACCESS_EXTERNAL_SCHEMA: String? = "http://javax.xml.XMLConstants/property/accessExternalSchema"
    }
}