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
package lucee.runtime.tag.util

import java.util.ArrayList

object FileUtil {
    const val NAMECONFLICT_UNDEFINED = 1 // can't start at 0 because we need to be able to do a bitmask test
    const val NAMECONFLICT_ERROR = 2
    const val NAMECONFLICT_SKIP = 4 // same as IGNORE
    const val NAMECONFLICT_OVERWRITE = 8 // same as MERGE
    const val NAMECONFLICT_MAKEUNIQUE = 16
    const val NAMECONFLICT_FORCEUNIQUE = 32

    // public static final int NAMECONFLICT_CLOSURE = 32; // FUTURE
    @Throws(ApplicationException::class)
    fun toNameConflict(nameConflict: String?): Int {
        var nameConflict = nameConflict
        if (StringUtil.isEmpty(nameConflict, true)) return NAMECONFLICT_UNDEFINED
        nameConflict = nameConflict.trim().toLowerCase()
        if ("error".equals(nameConflict)) return NAMECONFLICT_ERROR
        if ("skip".equals(nameConflict) || "ignore".equals(nameConflict)) return NAMECONFLICT_SKIP
        if ("merge".equals(nameConflict) || "overwrite".equals(nameConflict)) return NAMECONFLICT_OVERWRITE
        if ("makeunique".equals(nameConflict) || "unique".equals(nameConflict)) return NAMECONFLICT_MAKEUNIQUE
        if ("forceunique".equals(nameConflict)) return NAMECONFLICT_FORCEUNIQUE
        throw ApplicationException("Invalid value for attribute nameConflict [$nameConflict]", "valid values are [" + fromNameConflictBitMask(Integer.MAX_VALUE) + "]")
    }

    /**
     *
     * @param nameConflict
     * @param allowedValuesMask
     * @return
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun toNameConflict(nameConflict: String?, allowedValuesMask: Int): Int {
        val result = toNameConflict(nameConflict)
        if (allowedValuesMask and result == 0) {
            throw ApplicationException("Invalid value for attribute nameConflict [$nameConflict]",
                    "valid values are [" + fromNameConflictBitMask(allowedValuesMask) + "]")
        }
        return result
    }

    /**
     *
     * @param nameConflict
     * @param allowedValuesMask
     * @param defaultValue
     * @return
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun toNameConflict(nameConflict: String?, allowedValuesMask: Int, defaultValue: Int): Int {
        val result = toNameConflict(nameConflict, allowedValuesMask)
        return if (result == NAMECONFLICT_UNDEFINED) defaultValue else result
    }

    fun fromNameConflictBitMask(bitmask: Int): String? {
        val sb = StringBuilder()
        if (bitmask and NAMECONFLICT_ERROR > 0) sb.append("error").append(',')
        if (bitmask and NAMECONFLICT_MAKEUNIQUE > 0) sb.append("makeunique (unique)").append(',')
        if (bitmask and NAMECONFLICT_FORCEUNIQUE > 0) sb.append("forceunique").append(',')
        if (bitmask and NAMECONFLICT_OVERWRITE > 0) sb.append("overwrite (merge)").append(',')
        if (bitmask and NAMECONFLICT_SKIP > 0) sb.append("skip (ignore)").append(',')
        if (sb.length() > 0) sb.setLength(sb.length() - 1) // remove last ,
        return sb.toString()
    }

    @Throws(PageException::class)
    fun toExtensionFilter(obj: Object?): ExtensionResourceFilter? {
        val list: List<String?> = ArrayList()
        if (Decision.isArray(obj)) {
            var str: String?
            for (o in Caster.toNativeArray(obj)) {
                str = toExtensions(Caster.toString(o))
                if (!StringUtil.isEmpty(str)) list.add(str)
            }
        } else {
            for (str in ListUtil.listToList(Caster.toString(obj), ',', true)) {
                str = toExtensions(str)
                if (!StringUtil.isEmpty(str)) list.add(str)
            }
        }
        return ExtensionResourceFilter(list.toArray(arrayOfNulls<String?>(list.size())), false, true, false)
    }

    @Throws(PageException::class)
    fun toExtensions(str: String?): String? {
        var str = str
        if (StringUtil.isEmpty(str, true)) return null
        str = str.trim()
        if (str.startsWith("*.")) return str.substring(2).toLowerCase()
        return if (str.startsWith(".")) str.substring(1).toLowerCase() else str.toLowerCase()
    }
}