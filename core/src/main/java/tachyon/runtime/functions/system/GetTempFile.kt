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
/**
 * Implements the CFML Function gettempfile
 */
package tachyon.runtime.functions.system

import java.io.IOException

object GetTempFile : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, strDir: String?, prefix: String?): String? {
        return call(pc, strDir, prefix, ".tmp")
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strDir: String?, prefix: String?, extension: String?): String? {
        var extension = extension
        val dir: Resource = ResourceUtil.toResourceExisting(pc, strDir)
        pc.getConfig().getSecurityManager().checkFileLocation(dir)
        if (!dir.isDirectory()) throw ExpressionException("[$strDir] is not a directory")
        var count = 1
        var file: Resource?
        if (StringUtil.isEmpty(extension, true)) extension = ".tmp"
        if (extension.charAt(0) !== '.') extension = ".$extension"
        while (dir.getRealResource(prefix + pc.getId() + count + extension).also { file = it }.exists()) {
            count++
        }
        return try {
            file.createFile(false)
            // file.createNewFile();
            file.getCanonicalPath()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }
}