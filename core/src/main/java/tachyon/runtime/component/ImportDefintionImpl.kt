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
package tachyon.runtime.component

import tachyon.commons.lang.StringUtil

class ImportDefintionImpl(private val pack: String?, private val name: String?) : ImportDefintion {
    private val wildcard: Boolean
    private var packAsPath: String? = null

    /**
     * @return the wildcard
     */
    @Override
    fun isWildcard(): Boolean {
        return wildcard
    }

    /**
     * @return the pack
     */
    @Override
    fun getPackage(): String? {
        return pack
    }

    /**
     * @return the name
     */
    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getPackageAsPath(): String? {
        if (packAsPath == null) {
            packAsPath = pack.replace('.', '/').toString() + "/"
        }
        return packAsPath
    }

    @Override
    override fun toString(): String {
        return pack.toString() + "." + name
    }

    companion object {
        fun getInstance(fullname: String?, defaultValue: ImportDefintion?): ImportDefintion? {
            val index: Int = fullname.lastIndexOf('.')
            if (index == -1) return defaultValue
            val p: String = fullname.substring(0, index).trim()
            val n: String = fullname.substring(index + 1, fullname!!.length()).trim()
            return if (StringUtil.isEmpty(p) || StringUtil.isEmpty(n)) defaultValue else ImportDefintionImpl(p, n)
        }
    }

    init {
        wildcard = name!!.equals("*")
    }
}