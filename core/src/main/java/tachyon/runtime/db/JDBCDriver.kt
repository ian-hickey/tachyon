/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.db

import java.io.BufferedReader

class JDBCDriver(val label: String, id: String, connStr: String, cd: ClassDefinition) {
    val id: String?
    var connStr: String?
    val cd: ClassDefinition

    companion object {
        @Throws(IOException::class)
        fun extractClassName(bundle: Bundle): String {
            val url: URL = bundle.getResource("/META-INF/services/java.sql.Driver")
            val br = BufferedReader(InputStreamReader(url.openConnection().getInputStream()))
            val content: String = IOUtil.toString(br)
            return ListUtil.first(content, " \n\t")
        }

        fun extractClassName(bundle: Bundle, defaultValue: String): String {
            return try {
                extractClassName(bundle)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    init {
        this.id = if (StringUtil.isEmpty(id)) null else id.trim()
        this.connStr = if (StringUtil.isEmpty(connStr)) null else connStr.trim()
        this.cd = cd
    }
}