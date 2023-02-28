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
package lucee.runtime.config

import java.io.IOException

class DebugEntry private constructor(private val id: String?, private val type: String?, ipRange: IPRange?, private val strIpRange: String?, label: String?, path: String?, fullname: String?, custom: Struct?, readOnly: Boolean) {
    private val ipRange: IPRange?
    private val label: String?
    private val custom: Struct?
    private val readOnly: Boolean
    private val path: String?
    private val fullname: String?

    constructor(id: String?, type: String?, ipRange: String?, label: String?, path: String?, fullname: String?, custom: Struct?) : this(id, type, IPRange.getInstance(ipRange), ipRange, label, path, fullname, custom, false) {}

    /**
     * @return the path
     */
    fun getPath(): String? {
        return path
    }

    /**
     * @return the fullname
     */
    fun getFullname(): String? {
        return fullname
    }

    /**
     * @return the readOnly
     */
    fun isReadOnly(): Boolean {
        return readOnly
    }

    /**
     * @return the id
     */
    fun getId(): String? {
        return id
    }

    /**
     * @return the type
     */
    fun getType(): String? {
        return type
    }

    /**
     * @return the ipRange
     */
    fun getIpRangeAsString(): String? {
        return strIpRange
    }

    fun getIpRange(): IPRange? {
        return ipRange
    }

    /**
     * @return the label
     */
    fun getLabel(): String? {
        return label
    }

    /**
     * @return the custom
     */
    fun getCustom(): Struct? {
        return custom.duplicate(false) as Struct
    }

    fun duplicate(readOnly: Boolean): DebugEntry? {
        return DebugEntry(id, type, ipRange, strIpRange, label, path, fullname, custom, readOnly)
    }

    companion object {
        fun organizeIPRange(ipRange: String?): String? {
            var arr: Array<String?> = ListUtil.trim(ListUtil.trimItems(ListUtil.listToStringArray(ipRange, ',')))
            val set: Set<String?> = HashSet<String?>()
            for (i in arr.indices) {
                set.add(arr[i])
            }
            arr = set.toArray(arrayOfNulls<String?>(set.size()))
            Arrays.sort(arr)
            return ListUtil.arrayToList(arr, ",")
        }

        fun ipRangeToId(ipRange: String?): String? {
            var ipRange = ipRange
            ipRange = organizeIPRange(ipRange)
            return try {
                MD5.getDigestAsString(ipRange)
            } catch (e: IOException) {
                ipRange
            }
        }
    }

    init {
        this.ipRange = ipRange
        this.label = label
        this.custom = custom
        this.readOnly = readOnly
        this.path = path
        this.fullname = fullname
    }
}