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
 * Implements the CFML Function expandpath
 */
package lucee.runtime.functions.system

import java.io.IOException

object ExpandPath : Function {
    private const val serialVersionUID = 6192659914120397912L
    @Throws(PageException::class)
    fun call(pc: PageContext?, relPath: String?): String? {
        var relPath = relPath
        val config: ConfigWeb = pc.getConfig()
        relPath = prettifyPath(pc, relPath)
        val contextPath: String = pc.getHttpServletRequest().getContextPath()
        if (!StringUtil.isEmpty(contextPath) && relPath.startsWith("$contextPath/")) {
            val sws: Boolean = StringUtil.startsWith(relPath, '/')
            relPath = relPath.substring(contextPath.length())
            if (sws && !StringUtil.startsWith(relPath, '/')) relPath = "/$relPath"
        }
        var res: Resource
        if (StringUtil.startsWith(relPath, '/')) {
            val pci: PageContextImpl? = pc as PageContextImpl?
            val cwi: ConfigWebPro = config as ConfigWebPro
            val sources: Array<PageSource?> = cwi.getPageSources(pci, mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()), relPath,
                    false, pci.useSpecialMappings(), true)
            if (!ArrayUtil.isEmpty(sources)) {
                // first check for existing
                for (i in sources.indices) {
                    if (sources[i].exists()) {
                        return toReturnValue(relPath, sources[i].getResource())
                    }
                }

                // no expand needed
                if (!SystemUtil.isWindows() && !sources[0].exists()) {
                    res = pc.getConfig().getResource(relPath)
                    if (res.exists()) {
                        return toReturnValue(relPath, res)
                    }
                }
                for (i in sources.indices) {
                    res = sources[i].getResource()
                    if (res != null) {
                        return toReturnValue(relPath, res)
                    }
                }
            } else if (!SystemUtil.isWindows()) {
                res = pc.getConfig().getResource(relPath)
                if (res.exists()) {
                    return toReturnValue(relPath, res)
                }
            }

            // Resource[] reses =
            // cwi.getPhysicalResources(pc,pc.getApplicationContext().getMappings(),realPath,false,pci.useSpecialMappings(),true);
        }
        relPath = ConfigWebUtil.replacePlaceholder(relPath, config)
        res = pc.getConfig().getResource(relPath)
        if (res.isAbsolute()) return toReturnValue(relPath, res)
        val ps: PageSource = pc.getBasePageSource()
        res = if (ps == null) ResourceUtil.getCanonicalResourceEL(ResourceUtil.toResourceExisting(pc.getConfig(), ReqRspUtil.getRootPath(pc.getServletContext()))) else ResourceUtil.getResource(pc, ps)
        if (!res.isDirectory()) res = res.getParentResource()
        res = res.getRealResource(relPath)
        return toReturnValue(relPath, res)
    }

    fun mergeMappings(l: Array<Mapping?>?, r: Array<Mapping?>?): Array<Mapping?>? {
        val arr: Array<Mapping?> = arrayOfNulls<Mapping?>((l?.size ?: 0) + (r?.size ?: 0))
        var index = 0
        if (l != null) {
            for (m in l) {
                arr[index++] = m
            }
        }
        if (r != null) {
            for (m in r) {
                arr[index++] = m
            }
        }
        return arr
    }

    private fun toReturnValue(realPath: String?, res: Resource?): String? {
        var path: String?
        var pathChar = '/'
        try {
            path = res.getCanonicalPath()
            pathChar = ResourceUtil.FILE_SEPERATOR
        } catch (e: IOException) {
            path = res.getAbsolutePath()
        }
        val pathEndsWithSep: Boolean = StringUtil.endsWith(path, pathChar)
        val realEndsWithSep: Boolean = StringUtil.endsWith(realPath, '/')
        if (realEndsWithSep) {
            if (!pathEndsWithSep) path = path + pathChar
        } else if (pathEndsWithSep) {
            path = path.substring(0, path!!.length() - 1)
        }
        return path
    }

    private fun prettifyPath(pc: PageContext?, path: String?): String? {
        var path: String? = path ?: return null

        // UNC Path
        if (path.startsWith("\\\\") && SystemUtil.isWindows()) {
            path = path.substring(2)
            path = path.replace('\\', '/')
            return "//" + StringUtil.replace(path, "//", "/", false)
        }
        path = path.replace('\\', '/')

        // virtual file system path
        val index: Int = path.indexOf("://")
        if (index != -1) {
            val factories: Array<ResourceProviderFactory?> = (pc.getConfig() as ConfigPro).getResourceProviderFactories()
            val scheme: String = path.substring(0, index).toLowerCase().trim()
            for (i in factories.indices) {
                if (scheme.equalsIgnoreCase(factories[i].getScheme())) return scheme + "://" + StringUtil.replace(path.substring(index + 3), "//", "/", false)
            }
        }
        return StringUtil.replace(path, "//", "/", false)
        // TODO /aaa/../bbb/
    }
}