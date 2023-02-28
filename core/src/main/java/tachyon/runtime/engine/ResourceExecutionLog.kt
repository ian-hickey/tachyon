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
package tachyon.runtime.engine

import java.io.IOException

class ResourceExecutionLog : ExecutionLogSupport() {
    private var file: Resource? = null
    private var content: StringBuffer? = null
    private var pc: PageContext? = null
    private var header: StringBuffer? = null
    private val pathes: ArrayList<String?>? = ArrayList<String?>()
    private var start: Long = 0
    private var dir: Resource? = null
    @Override
    protected override fun _init(pc: PageContext?, arguments: Map<String?, String?>?) {
        this.pc = pc

        // header
        val req: HttpServletRequest = pc.getHttpServletRequest()
        header = StringBuffer()
        createHeader(header, "context-path", req.getContextPath())
        createHeader(header, "remote-user", req.getRemoteUser())
        createHeader(header, "remote-addr", req.getRemoteAddr())
        createHeader(header, "remote-host", req.getRemoteHost())
        createHeader(header, "script-name", StringUtil.emptyIfNull(req.getContextPath()) + StringUtil.emptyIfNull(req.getServletPath()))
        createHeader(header, "server-name", req.getServerName())
        createHeader(header, "protocol", req.getProtocol())
        createHeader(header, "server-port", Caster.toString(req.getServerPort()))
        createHeader(header, "path-info", StringUtil.replace(StringUtil.emptyIfNull(req.getRequestURI()), StringUtil.emptyIfNull(req.getServletPath()), "", true))
        // createHeader(header,"path-translated",pc.getBasePageSource().getDisplayPath());
        createHeader(header, "query-string", req.getQueryString())
        createHeader(header, "unit", unitShortToString(unit))
        createHeader(header, "min-time-nano", min.toString() + "")
        content = StringBuffer()

        // directory
        val strDirectory = arguments!!["directory"]
        if (dir == null) {
            if (StringUtil.isEmpty(strDirectory)) {
                dir = getTemp(pc)
            } else {
                try {
                    dir = ResourceUtil.toResourceNotExisting(pc, strDirectory, false, false)
                    if (!dir.exists()) {
                        dir.createDirectory(true)
                    } else if (dir.isFile()) {
                        err(pc, "can not create directory [$dir], there is already a file with same name.")
                    }
                } catch (t: Exception) {
                    err(pc, t)
                    dir = getTemp(pc)
                }
            }
        }
        file = dir.getRealResource(pc.getId().toString() + "-" + CreateUUID.call(pc) + ".exl")
        file.createNewFile()
        start = System.currentTimeMillis()
    }

    @Override
    protected override fun _release() {

        // execution time
        createHeader(header, "execution-time", Caster.toString(System.currentTimeMillis() - start))
        header.append("\n")

        // path
        val sb = StringBuilder()
        val it: Iterator<String?> = pathes.iterator()
        var count = 0
        while (it.hasNext()) {
            sb.append(count++)
            sb.append(":")
            sb.append(it.next())
            sb.append("\n")
        }
        sb.append("\n")
        try {
            IOUtil.write(file, header + sb.toString() + content.toString(), null as Charset?, false)
        } catch (ioe: IOException) {
            err(pc, ioe)
        }
    }

    private fun createHeader(sb: StringBuffer?, name: String?, value: String?) {
        sb.append(name)
        sb.append(":")
        sb.append(StringUtil.emptyIfNull(value))
        sb.append("\n")
    }

    @Override
    protected override fun _log(startPos: Int, endPos: Int, startTime: Long, endTime: Long) {
        var diff = endTime - startTime
        if (unit === UNIT_MICRO) diff /= 1000 else if (unit === UNIT_MILLI) diff /= 1000000
        content.append(path(pc.getCurrentPageSource().getDisplayPath()))
        content.append("\t")
        content.append(startPos)
        content.append("\t")
        content.append(endPos)
        content.append("\t")
        content.append(diff)
        content.append("\n")
    }

    private fun path(path: String?): Int {
        val index: Int = pathes.indexOf(path)
        if (index == -1) {
            pathes.add(path)
            return pathes.size() - 1
        }
        return index
    }

    private fun err(pc: PageContext?, msg: String?) {
        LogUtil.log(pc, Log.LEVEL_ERROR, ResourceExecutionLog::class.java.getName(), msg)
    }

    private fun err(pc: PageContext?, e: Exception?) {
        LogUtil.log(pc, ResourceExecutionLog::class.java.getName(), e)
    }

    companion object {
        private const val count = 1
        private fun getTemp(pc: PageContext?): Resource? {
            val tmp: Resource = pc.getConfig().getConfigDir()
            val dir: Resource = tmp.getRealResource("execution-log")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }
    }
}