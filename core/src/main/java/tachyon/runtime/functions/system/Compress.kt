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

import java.io.IOException

/**
 * Implements the CFML Function compress
 */
object Compress : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, strFormat: String?, strSource: String?, srcTarget: String?): Boolean {
        return call(pc, strFormat, strSource, srcTarget, true, "777")
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strFormat: String?, strSource: String?, srcTarget: String?, includeBaseFolder: Boolean): Boolean {
        return call(pc, strFormat, strSource, srcTarget, includeBaseFolder, "777")
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strFormat: String?, strSource: String?, srcTarget: String?, includeBaseFolder: Boolean, strMode: String?): Boolean {
        var strFormat = strFormat
        val mode: Int
        mode = try {
            ModeUtil.toOctalMode(strMode)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        strFormat = strFormat.trim().toLowerCase()
        var format: Int = CompressUtil.FORMAT_ZIP
        format = if (strFormat.equals("bzip")) CompressUtil.FORMAT_BZIP else if (strFormat.equals("bzip2")) CompressUtil.FORMAT_BZIP2 else if (strFormat.equals("gzip")) CompressUtil.FORMAT_GZIP else if (strFormat.equals("tar")) CompressUtil.FORMAT_TAR else if (strFormat.equals("tbz")) CompressUtil.FORMAT_TBZ else if (strFormat.startsWith("tar.bz")) CompressUtil.FORMAT_TBZ else if (strFormat.equals("tbz2")) CompressUtil.FORMAT_TBZ2 else if (strFormat.startsWith("tar.gz")) CompressUtil.FORMAT_TGZ else if (strFormat.equals("tgz")) CompressUtil.FORMAT_TGZ else if (strFormat.equals("zip")) CompressUtil.FORMAT_ZIP else throw FunctionException(pc, "compress", 1, "format",
                "invalid format definition [$strFormat], valid formats are [bzip,gzip,tar,tbz (tar bzip),tgz (tar gzip) and zip]")
        val arrSources: Array<String?> = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(strSource, ","))
        val sources: Array<Resource?> = arrayOfNulls<Resource?>(arrSources.size)
        for (i in sources.indices) {
            sources[i] = ResourceUtil.toResourceExisting(pc, arrSources[i])
            pc.getConfig().getSecurityManager().checkFileLocation(sources[i])
        }
        val target: Resource = ResourceUtil.toResourceExistingParent(pc, srcTarget)
        pc.getConfig().getSecurityManager().checkFileLocation(target)
        try {
            if (sources.size == 1) CompressUtil.compress(format, sources[0], target, includeBaseFolder, mode) else CompressUtil.compress(format, sources, target, mode)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return true
    }
}