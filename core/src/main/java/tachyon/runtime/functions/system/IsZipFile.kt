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
package tachyon.runtime.functions.system

import java.io.File

object IsZipFile {
    fun call(pc: PageContext?, path: String?): Boolean {
        return try {
            invoke(ResourceUtil.toResourceExisting(pc, path))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            false
        }
    }

    operator fun invoke(res: Resource?): Boolean {
        var `is`: InputStream? = null
        var hasEntries = false
        try {
            // ZipEntry ze;
            val zis = ZipInputStream(res.getInputStream().also { `is` = it })
            while (zis.getNextEntry() != null) {
                zis.closeEntry()
                hasEntries = true
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        } finally {
            IOUtil.closeEL(`is`)
        }
        return hasEntries
    }

    operator fun invoke(file: File?): Boolean {
        var `is`: InputStream? = null
        var hasEntries = false
        try {
            // ZipEntry ze;
            val zis = ZipInputStream(FileInputStream(file).also { `is` = it })
            while (zis.getNextEntry() != null) {
                zis.closeEntry()
                hasEntries = true
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        } finally {
            IOUtil.closeEL(`is`)
        }
        return hasEntries
    }
}