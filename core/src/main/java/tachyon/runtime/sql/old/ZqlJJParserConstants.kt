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
package tachyon.runtime.sql.old

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

interface ZqlJJParserConstants {
    companion object {
        const val EOF = 0
        const val K_ALL = 5
        const val K_AND = 6
        const val K_ANY = 7
        const val K_AS = 8
        const val K_ASC = 9
        const val K_AVG = 10
        const val K_BETWEEN = 11
        const val K_BINARY_INTEGER = 12
        const val K_BOOLEAN = 13
        const val K_BY = 14
        const val K_CHAR = 15
        const val K_COMMENT = 16
        const val K_COMMIT = 17
        const val K_CONNECT = 18
        const val K_COUNT = 19
        const val K_DATE = 20
        const val K_DELETE = 21
        const val K_DESC = 22
        const val K_DISTINCT = 23
        const val K_EXCLUSIVE = 24
        const val K_EXISTS = 25
        const val K_EXIT = 26
        const val K_FLOAT = 27
        const val K_FOR = 28
        const val K_FROM = 29
        const val K_GROUP = 30
        const val K_HAVING = 31
        const val K_IN = 32
        const val K_INSERT = 33
        const val K_INTEGER = 34
        const val K_INTERSECT = 35
        const val K_INTO = 36
        const val K_IS = 37
        const val K_LIKE = 38
        const val K_LOCK = 39
        const val K_MAX = 40
        const val K_MIN = 41
        const val K_MINUS = 42
        const val K_MODE = 43
        const val K_NATURAL = 44
        const val K_NOT = 45
        const val K_NOWAIT = 46
        const val K_NULL = 47
        const val K_NUMBER = 48
        const val K_OF = 49
        const val K_ONLY = 50
        const val K_OR = 51
        const val K_ORDER = 52
        const val K_PRIOR = 53
        const val K_QUIT = 54
        const val K_READ = 55
        const val K_REAL = 56
        const val K_ROLLBACK = 57
        const val K_ROW = 58
        const val K_SELECT = 59
        const val K_SET = 60
        const val K_SHARE = 61
        const val K_SMALLINT = 62
        const val K_START = 63
        const val K_SUM = 64
        const val K_TABLE = 65
        const val K_TRANSACTION = 66
        const val K_UNION = 67
        const val K_UPDATE = 68
        const val K_VALUES = 69
        const val K_VARCHAR2 = 70
        const val K_VARCHAR = 71
        const val K_WHERE = 72
        const val K_WITH = 73
        const val K_WORK = 74
        const val K_WRITE = 75
        const val S_NUMBER = 76
        const val FLOAT = 77
        const val INTEGER = 78
        const val DIGIT = 79
        const val LINE_COMMENT = 80
        const val MULTI_LINE_COMMENT = 81
        const val S_IDENTIFIER = 82
        const val LETTER = 83
        const val SPECIAL_CHARS = 84
        const val S_BIND = 85
        const val S_CHAR_LITERAL = 86
        const val S_QUOTED_IDENTIFIER = 87
        const val DEFAULT = 0

        /*
	 * public static final String tokenImage[] = { "<EOF>", "\" \"", "\"\\t\"", "\"\\r\"", "\"\\n\"",
	 * "\"NEVER_USE_AVG\"", "\"NEVER_USE_BETWEEN\"", "\"NEVER_USE_BINARY_INTEGER\"",
	 * "\"NEVER_USE_BOOLEAN\"", "\"NEVER_USE_BY\"", "\"NEVER_USE_CHAR\"", "\"NEVER_USE_COMMENT\"",
	 * "\"NEVER_USE_COMMIT\"", "\"NEVER_USE_CONNECT\"", "\"NEVER_USE_COUNT\"", "\"NEVER_USE_DATE\"",
	 * "\"NEVER_USE_DELETE\"", "\"NEVER_USE_DESC\"", "\"NEVER_USE_DISTINCT\"",
	 * "\"NEVER_USE_EXCLUSIVE\"", "\"NEVER_USE_EXISTS\"", "\"NEVER_USE_EXIT\"", "\"NEVER_USE_FLOAT\"",
	 * "\"NEVER_USE_FOR\"", "\"NEVER_USE_FROM\"", "\"NEVER_USE_GROUP\"", "\"NEVER_USE_HAVING\"",
	 * "\"NEVER_USE_IN\"", "\"NEVER_USE_INSERT\"", "\"NEVER_USE_INTEGER\"", "\"NEVER_USE_INTERSECT\"",
	 * "\"NEVER_USE_INTO\"", "\"NEVER_USE_IS\"", "\"NEVER_USE_LIKE\"", "\"NEVER_USE_LOCK\"",
	 * "\"NEVER_USE_MAX\"", "\"NEVER_USE_MIN\"", "\"NEVER_USE_MINUS\"", "\"NEVER_USE_MODE\"",
	 * "\"NEVER_USE_NATURAL\"", "\"NEVER_USE_NOT\"", "\"NEVER_USE_NOWAIT\"", "\"NEVER_USE_NULL\"",
	 * "\"NEVER_USE_NUMBER\"", "\"NEVER_USE_OF\"", "\"NEVER_USE_ONLY\"", "\"NEVER_USE_OR\"",
	 * "\"NEVER_USE_ORDER\"", "\"NEVER_USE_PRIOR\"", "\"NEVER_USE_QUIT\"", "\"NEVER_USE_READ\"",
	 * "\"NEVER_USE_REAL\"", "\"NEVER_USE_ROLLBACK\"", "\"NEVER_USE_ROW\"", "\"NEVER_USE_SELECT\"",
	 * "\"NEVER_USE_SET\"", "\"NEVER_USE_SHARE\"", "\"NEVER_USE_SMALLINT\"", "\"NEVER_USE_START\"",
	 * "\"NEVER_USE_SUM\"", "\"NEVER_USE_TABLE\"", "\"NEVER_USE_TRANSACTION\"", "\"NEVER_USE_UNION\"",
	 * "\"NEVER_USE_UPDATE\"", "\"NEVER_USE_VALUES\"", "\"NEVER_USE_VARCHAR2\"",
	 * "\"NEVER_USE_VARCHAR\"", "\"NEVER_USE_WHERE\"", "\"NEVER_USE_WITH\"", "\"NEVER_USE_WORK\"",
	 * "\"NEVER_USE_WRITE\"", "<S_NUMBER>", "<FLOAT>", "<INTEGER>", "<DIGIT>", "<LINE_COMMENT>",
	 * "<MULTI_LINE_COMMENT>", "<S_IDENTIFIER>", "<LETTER>", "<SPECIAL_CHARS>", "<S_BIND>",
	 * "<S_CHAR_LITERAL>", "<S_QUOTED_IDENTIFIER>", "\"(\"", "\",\"", "\")\"", "\";\"", "\"=\"",
	 * "\".\"", "\"!=\"", "\"#\"", "\"<>\"", "\">\"", "\">=\"", "\"<\"", "\"<=\"", "\"+\"", "\"-\"",
	 * "\"*\"", "\".*\"", "\"?\"", "\"||\"", "\"/\"", "\"**\"" };
	 */
        val tokenImage: Array<String?>? = arrayOf("<EOF>", "\" \"", "\"\\t\"", "\"\\r\"", "\"\\n\"", "\"ALL\"", "\"AND\"", "\"ANY\"", "\"AS\"", "\"ASC\"", "\"AVG\"", "\"BETWEEN\"",
                "\"BINARY_INTEGER\"", "\"BOOLEAN\"", "\"BY\"", "\"CHAR\"", "\"COMMENT\"", "\"COMMIT\"", "\"CONNECT\"", "\"COUNT\"", "\"DATE\"", "\"DELETE\"", "\"DESC\"",
                "\"DISTINCT\"", "\"EXCLUSIVE\"", "\"EXISTS\"", "\"EXIT\"", "\"FLOAT\"", "\"FOR\"", "\"FROM\"", "\"GROUP\"", "\"HAVING\"", "\"IN\"", "\"INSERT\"", "\"INTEGER\"",
                "\"INTERSECT\"", "\"INTO\"", "\"IS\"", "\"LIKE\"", "\"LOCK\"", "\"MAX\"", "\"MIN\"", "\"MINUS\"", "\"MODE\"", "\"NATURAL\"", "\"NOT\"", "\"NOWAIT\"", "\"NULL\"",
                "\"NUMBER\"", "\"OF\"", "\"ONLY\"", "\"OR\"", "\"ORDER\"", "\"PRIOR\"", "\"QUIT\"", "\"READ\"", "\"REAL\"", "\"ROLLBACK\"", "\"ROW\"", "\"SELECT\"", "\"SET\"",
                "\"SHARE\"", "\"SMALLINT\"", "\"START\"", "\"SUM\"", "\"TABLE\"", "\"TRANSACTION\"", "\"UNION\"", "\"UPDATE\"", "\"VALUES\"", "\"VARCHAR2\"", "\"VARCHAR\"",
                "\"WHERE\"", "\"WITH\"", "\"WORK\"", "\"WRITE\"", "<S_NUMBER>", "<FLOAT>", "<INTEGER>", "<DIGIT>", "<LINE_COMMENT>", "<MULTI_LINE_COMMENT>", "<S_IDENTIFIER>",
                "<LETTER>", "<SPECIAL_CHARS>", "<S_BIND>", "<S_CHAR_LITERAL>", "<S_QUOTED_IDENTIFIER>", "\"(\"", "\",\"", "\")\"", "\";\"", "\"=\"", "\".\"", "\"!=\"", "\"#\"",
                "\"<>\"", "\">\"", "\">=\"", "\"<\"", "\"<=\"", "\"+\"", "\"-\"", "\"*\"", "\".*\"", "\"?\"", "\"||\"", "\"/\"", "\"**\"")
    }
}