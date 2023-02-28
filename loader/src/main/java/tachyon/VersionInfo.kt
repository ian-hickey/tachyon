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
package tachyon

import java.io.BufferedReader

/**
 * returns th current built in version
 */
object VersionInfo {
    private var version: Version? = null
    private var created: Long = -1

    /**
     * @return returns the current version
     */
    fun getIntVersion(): Version? {
        init()
        return version
    }

    /**
     * return creation time of this version
     *
     * @return creation time
     */
    fun getCreateTime(): Long {
        init()
        return created
    }

    private fun init() {
        if (version != null) return
        var content = "9000000:" + System.currentTimeMillis()
        try {
            val `is`: InputStream = TP().getClass().getClassLoader().getResourceAsStream("tachyon/version")
            if (`is` != null) {
                content = getContentAsString(`is`, "UTF-8")
            } else {
                System.err.println("tachyon/version not found")
            }
        } catch (e: IOException) {
        }
        val index: Int = content.indexOf(':')
        version = CFMLEngineFactorySupport.toVersion(content.substring(0, index), CFMLEngineFactory.VERSION_ZERO)
        val d: String = content.substring(index + 1)
        try {
            created = Long.parseLong(d)
        } catch (nfe: NumberFormatException) {
            try {
                created = SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").parse(d).getTime()
            } catch (pe: ParseException) {
                pe.printStackTrace()
                created = 0
            }
        }
    }

    @Throws(IOException::class)
    private fun getContentAsString(`is`: InputStream?, charset: String?): String {
        val br: BufferedReader = if (charset == null) BufferedReader(InputStreamReader(`is`)) else BufferedReader(InputStreamReader(`is`, charset))
        val content = StringBuffer()
        var line: String? = br.readLine()
        if (line != null) {
            content.append(line)
            while (br.readLine().also { line = it } != null) content.append("""
    
    $line
    """.trimIndent())
        }
        br.close()
        return content.toString()
    }
}