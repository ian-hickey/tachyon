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
package lucee.runtime.text.xml

import kotlin.Throws
import kotlin.jvm.Synchronized
import lucee.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import lucee.commons.collection.LongKeyList.Pair
import lucee.commons.collection.AbstractCollection
import lucee.runtime.type.Array
import java.sql.Array
import lucee.commons.lang.Pair
import lucee.runtime.exp.CatchBlockImpl.Pair
import lucee.runtime.type.util.ListIteratorImpl
import lucee.runtime.type.Lambda
import java.util.Random
import lucee.runtime.config.Constants
import lucee.runtime.engine.Request
import lucee.runtime.engine.ExecutionLogSupport.Pair
import lucee.runtime.functions.other.NullValue
import lucee.runtime.functions.string.Val
import lucee.runtime.reflection.Reflector.JavaAnnotation
import lucee.transformer.cfml.evaluator.impl.Output
import lucee.transformer.cfml.evaluator.impl.Property
import lucee.transformer.bytecode.statement.Condition.Pair

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