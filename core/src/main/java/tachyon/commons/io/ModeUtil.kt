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
package tachyon.commons.io

import java.io.IOException

object ModeUtil {
    const val PERM_READ = 4
    const val PERM_WRITE = 2
    const val PERM_EXECUTE = 1
    const val ROLE_OWNER = 64
    const val ROLE_GROUP = 8
    const val ROLE_WORLD = 1

    /**
     * translate a string mode (777 or drwxrwxrwx to an octal value)
     *
     * @param strMode
     * @return
     */
    @Throws(IOException::class)
    fun toOctalMode(strMode: String): Int {
        var strMode = strMode
        strMode = strMode.trim().toLowerCase()
        if (strMode.length() === 9 || strMode.length() === 10) return _toOctalMode(strMode)
        if (strMode.length() <= 4 && strMode.length() > 0) return Integer.parseInt(strMode, 8)
        throw IOException("can't translate [$strMode] to a mode value")
    }

    private fun _toOctalMode(strMode: String): Int {
        var strMode = strMode
        var index: Int
        strMode = strMode.trim().toLowerCase()
        index = if (strMode.length() === 9) 0 else 1
        var mode = 0

        // owner
        if ("r".equals(strMode.substring(index++, index))) mode += 256
        if ("w".equals(strMode.substring(index++, index))) mode += 128
        if ("x".equals(strMode.substring(index++, index))) mode += 64
        // group
        if ("r".equals(strMode.substring(index++, index))) mode += 32
        if ("w".equals(strMode.substring(index++, index))) mode += 16
        if ("x".equals(strMode.substring(index++, index))) mode += 8
        // world
        if ("r".equals(strMode.substring(index++, index))) mode += 4
        if ("w".equals(strMode.substring(index++, index))) mode += 2
        if ("x".equals(strMode.substring(index++, index))) mode += 1
        return mode
    }

    /**
     * translate an octal mode value (73) to a string representation ("111")
     *
     * @param strMode
     * @return
     */
    fun toStringMode(octalMode: Int): String {
        var str: String = Integer.toString(octalMode, 8)
        while (str.length() < 3) str = "0$str"
        return str
    }

    /**
     * update a string mode with another (111+222=333 or 333+111=333 or 113+202=313)
     *
     * @param existing
     * @param update
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun updateMode(existing: String, update: String): String {
        return toStringMode(updateMode(toOctalMode(existing), toOctalMode(update)))
    }

    /**
     * update octal mode with another
     *
     * @param existingOctal
     * @param updateOctal
     * @return
     */
    fun updateMode(existingOctal: Int, updateOctal: Int): Int {
        val tmp = existingOctal and updateOctal
        return existingOctal - tmp + updateOctal
    }

    /**
     * check mode for a specific permission
     *
     * @param role
     * @param permission
     * @param mode
     * @return
     */
    fun hasPermission(role: Int, permission: Int, mode: Int): Boolean {
        return mode and role * permission > 0
    }

    /**
     * check if mode is readable for owner
     *
     * @param octalMode
     * @return
     */
    fun isReadable(octalMode: Int): Boolean {
        return hasPermission(ROLE_OWNER, PERM_READ, octalMode)
    }

    /**
     * check if mode is writeable for owner
     *
     * @param octalMode
     * @return
     */
    fun isWritable(octalMode: Int): Boolean {
        return hasPermission(ROLE_OWNER, PERM_WRITE, octalMode)
    }

    /**
     * check if mode is executable for owner
     *
     * @param octalMode
     * @return
     */
    fun isExecutable(octalMode: Int): Boolean {
        return hasPermission(ROLE_OWNER, PERM_EXECUTE, octalMode)
    }

    fun setReadable(octalMode: Int, value: Boolean): Int {
        val tmp = octalMode and 292
        return if (value) octalMode - tmp + 292 else octalMode - tmp
    }

    fun setWritable(octalMode: Int, value: Boolean): Int {
        val tmp = octalMode and 146
        return if (value) octalMode - tmp + 146 else octalMode - tmp
    }

    fun setExecutable(octalMode: Int, value: Boolean): Int {
        val tmp = octalMode and 73
        return if (value) octalMode - tmp + 73 else octalMode - tmp
    }
}