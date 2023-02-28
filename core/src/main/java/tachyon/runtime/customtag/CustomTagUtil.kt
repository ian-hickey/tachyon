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
package tachyon.runtime.customtag

import java.util.ArrayList

object CustomTagUtil {
    @Throws(PageException::class)
    fun loadInitFile(pc: PageContext?, name: String?): InitFile? {
        val initFile: InitFile? = loadInitFile(pc, name, null)
        if (initFile != null) {
            return initFile
        }
        // EXCEPTION
        val config: ConfigWeb = pc.getConfig()
        // message
        val msg: StringBuilder = StringBuilder("Custom tag \"").append(getDisplayName(config, name)).append("\" was not found.")
        val dirs: List<String?> = ArrayList()
        if (config.doLocalCustomTag()) {
            dirs.add(ResourceUtil.getResource(pc, pc.getCurrentPageSource()).getParent())
        }
        val actms: Array<Mapping?> = pc.getApplicationContext().getCustomTagMappings()
        val cctms: Array<Mapping?> = config.getCustomTagMappings()
        var r: Resource
        if (actms != null) {
            for (m in actms) {
                r = m.getPhysical()
                if (r != null) dirs.add(r.toString())
            }
        }
        if (cctms != null) {
            for (m in cctms) {
                r = m.getPhysical()
                if (r != null) dirs.add(r.toString())
            }
        }
        if (!dirs.isEmpty()) {
            msg.append(" Directories searched: ")
            val it = dirs.iterator()
            while (it.hasNext()) {
                msg.append('"').append(it.next()).append('"')
                if (it.hasNext()) msg.append(", ")
            }
        }
        throw ExpressionException(msg.toString())
    }

    @Throws(PageException::class)
    fun loadInitFile(pc: PageContext?, name: String?, defaultValue: InitFile?): InitFile? {
        val config: ConfigPro = pc.getConfig() as ConfigPro
        val filenames = getFileNames(config, name)
        val doCache: Boolean = config.useCTPathCache()
        val doCustomTagDeepSearch: Boolean = config.doCustomTagDeepSearch()
        var ps: PageSource? = null
        var initFile: InitFile?

        // CACHE
        // check local
        var localCacheName: String? = null
        val actms: Array<Mapping?> = pc.getApplicationContext().getCustomTagMappings()
        val cctms: Array<Mapping?> = config.getCustomTagMappings()
        if (doCache) {
            if (pc.getConfig().doLocalCustomTag()) {
                localCacheName = pc.getCurrentPageSource().getDisplayPath().replace('\\', '/')
                localCacheName = "local:" + localCacheName.substring(0, localCacheName.lastIndexOf('/') + 1).concat(name)
                initFile = config.getCTInitFile(pc, localCacheName)
                if (initFile != null) return initFile
            }

            // cache application mapping
            if (actms != null) for (i in actms.indices) {
                initFile = config.getCTInitFile(pc, "application:" + actms[i].hashCode().toString() + "/" + name)
                if (initFile != null) return initFile
            }

            // cache config mapping
            if (cctms != null) for (i in cctms.indices) {
                initFile = config.getCTInitFile(pc, "config:" + cctms[i].hashCode().toString() + "/" + name)
                if (initFile != null) return initFile
            }
        }

        // SEARCH
        // search local
        if (pc.getConfig().doLocalCustomTag()) {
            for (i in filenames.indices) {
                val arr: Array<PageSource?> = (pc as PageContextImpl?).getRelativePageSources(filenames!![i])
                // ps=pc.getRelativePageSource(filenames[i]);
                ps = MappingImpl.isOK(arr)
                if (ps != null) {
                    initFile = InitFile(pc, ps, filenames[i])
                    if (doCache) config.putCTInitFile(localCacheName, initFile)
                    return initFile
                }
            }
        }

        // search application custom tag mapping
        if (actms != null) {
            for (i in filenames.indices) {
                ps = getMapping(actms, filenames!![i], doCustomTagDeepSearch)
                if (ps != null) {
                    initFile = InitFile(pc, ps, filenames[i])
                    if (doCache) config.putCTInitFile("application:" + ps.getMapping().hashCode().toString() + "/" + name, initFile)
                    return initFile
                }
            }
        }

        // search custom tag mappings
        for (i in filenames.indices) {
            ps = getMapping(cctms, filenames!![i], doCustomTagDeepSearch)
            if (ps != null) {
                initFile = InitFile(pc, ps, filenames[i])
                if (doCache) config.putCTInitFile("config:" + ps.getMapping().hashCode().toString() + "/" + name, initFile)
                return initFile
            }
        }
        return defaultValue
    }

    fun getComponentExtension(pc: PageContext?, ps: PageSource?): String? {
        return if (ps.getDialect() === CFMLEngine.DIALECT_CFML) Constants.getCFMLComponentExtension() else Constants.getTachyonComponentExtension()
    }

    @Throws(ExpressionException::class)
    fun getFileNames(config: Config?, name: String?): Array<String?>? {
        val extensions: Array<String?> = config.getCustomTagExtensions()
        if (extensions.size == 0) throw ExpressionException("Custom Tags are disabled")
        val fileNames = arrayOfNulls<String?>(extensions.size)
        for (i in fileNames.indices) {
            fileNames[i] = name + '.' + extensions[i]
        }
        return fileNames
    }

    private fun getMapping(ctms: Array<Mapping?>?, filename: String?, doCustomTagDeepSearch: Boolean): PageSource? {
        var ps: PageSource
        for (i in ctms.indices) {
            ps = (ctms!![i] as MappingImpl?).getCustomTagPath(filename, doCustomTagDeepSearch)
            if (ps != null) return ps
        }
        return null
    }

    fun getDisplayName(config: Config?, name: String?): String? {
        val extensions: Array<String?> = config.getCustomTagExtensions()
        return if (extensions.size == 0) name else name.toString() + ".[" + ListUtil.arrayToList(extensions, "|") + "]"
    }

    fun toString(ctms: Array<Mapping?>?): String? {
        if (ctms == null) return ""
        val sb = StringBuilder()
        var p: Resource
        for (i in ctms.indices) {
            if (sb.length() !== 0) sb.append(", ")
            p = ctms[i].getPhysical()
            if (p != null) sb.append(p.toString())
        }
        return sb.toString()
    }
}