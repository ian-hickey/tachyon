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
package lucee.runtime.functions.system

import java.io.IOException

/**
 * Implements the CFML Function compress
 */
object Extract : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, strFormat: String?, strSource: String?, srcTarget: String?): Boolean {
        var strFormat = strFormat
        var singleFileFormat = false
        strFormat = strFormat.trim().toLowerCase()
        var format: Int = CompressUtil.FORMAT_ZIP
        if (strFormat.equals("bzip")) {
            format = CompressUtil.FORMAT_BZIP
            singleFileFormat = true
        } else if (strFormat.equals("bzip2")) {
            format = CompressUtil.FORMAT_BZIP2
            singleFileFormat = true
        } else if (strFormat.equals("gzip")) {
            format = CompressUtil.FORMAT_GZIP
            singleFileFormat = true
        } else if (strFormat.equals("tar")) format = CompressUtil.FORMAT_TAR else if (strFormat.equals("tbz")) format = CompressUtil.FORMAT_TBZ else if (strFormat.startsWith("tar.bz")) format = CompressUtil.FORMAT_TBZ else if (strFormat.equals("tbz2")) format = CompressUtil.FORMAT_TBZ2 else if (strFormat.startsWith("tar.gz")) format = CompressUtil.FORMAT_TGZ else if (strFormat.equals("tgz")) format = CompressUtil.FORMAT_TGZ else if (strFormat.equals("zip")) format = CompressUtil.FORMAT_ZIP else throw FunctionException(pc, "extract", 1, "format",
                "invalid format definition [$strFormat], valid formats are [bzip,gzip,tar,tbz (tar bzip),tgz (tar gzip) and zip]")
        val arrSources: Array<String?> = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(strSource, ","))
        val sources: Array<Resource?> = arrayOfNulls<Resource?>(arrSources.size)
        for (i in sources.indices) {
            sources[i] = ResourceUtil.toResourceExisting(pc, arrSources[i])
            // FileUtil.toFileExisting(pc,arrSources[i]);
            pc.getConfig().getSecurityManager().checkFileLocation(sources[i])
        }
        val target: Resource = if (singleFileFormat) ResourceUtil.toResourceNotExisting(pc, srcTarget) else ResourceUtil.toResourceExisting(pc, srcTarget)
        pc.getConfig().getSecurityManager().checkFileLocation(target)
        try {
            CompressUtil.extract(format, sources, target)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return true
    }
}