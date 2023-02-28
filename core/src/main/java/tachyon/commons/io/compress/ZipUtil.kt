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
package tachyon.commons.io.compress

import java.io.IOException

object ZipUtil {
    @Throws(IOException::class)
    fun unzip(zip: Resource, dir: Resource) {
        if (zip.length() > 0 && (dir.exists() || dir.mkdirs())) {
            if ("Mac OS X".equalsIgnoreCase(System.getProperty("os.name"))) {
                try {
                    // Command.execute("unzip "+zip+" -d "+dir);
                    Command.execute("unzip", arrayOf("-o", zip.getAbsolutePath(), "-d", dir.getAbsolutePath()))
                } catch (e: InterruptedException) {
                }
                return
            }
            CompressUtil.extract(CompressUtil.FORMAT_ZIP, zip, dir)
        }
    }

    fun close(zos: ZipOutputStream?) {
        if (zos == null) return
        try {
            zos.close()
        } catch (e: IOException) {
        }
    }

    fun close(file: ZipFile?) {
        if (file == null) return
        try {
            file.close()
        } catch (e: IOException) {
        }
    }

    @Throws(IOException::class)
    fun toResource(targetDir: Resource, entry: ZipEntry): Resource {
        var target: Resource = targetDir.getRealResource(entry.getName())

        // in case a file is outside the target directory, we copy it to the target directory
        if (!target.getCanonicalPath().startsWith(targetDir.getCanonicalPath())) {
            target = targetDir.getRealResource(ListUtil.last(entry.getName(), "\\/", true))
        }
        return target
    }
}