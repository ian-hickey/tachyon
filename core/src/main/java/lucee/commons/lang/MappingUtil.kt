/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.lang

import java.io.IOException

object MappingUtil {
    fun searchMappingRecursive(mapping: Mapping, name: String, onlyCFC: Boolean): PageSource? {
        if (name.indexOf('/') === -1) { // TODO handle this as well?
            val config: Config = mapping.getConfig()
            var ext: ExtensionResourceFilter? = null
            if (onlyCFC) ext = ExtensionResourceFilter(Constants.getComponentExtensions(), true, true) else {
                ext = ExtensionResourceFilter(Constants.getExtensions(), true, true)
                // ext.addExtension(config.getComponentExtension());
            }
            if (mapping.isPhysicalFirst()) {
                var ps: PageSource? = searchPhysical(mapping, name, ext)
                if (ps != null) return ps
                ps = searchArchive(mapping, name, onlyCFC)
                if (ps != null) return ps
            } else {
                var ps: PageSource? = searchArchive(mapping, name, onlyCFC)
                if (ps != null) return ps
                ps = searchPhysical(mapping, name, ext)
                if (ps != null) return ps
            }
        }
        return null
    }

    private fun searchArchive(mapping: Mapping, name: String, onlyCFC: Boolean): PageSource? {
        val archive: Resource = mapping.getArchive()
        if (archive != null && archive.isFile()) {
            var zis: ZipInputStream? = null
            try {
                zis = ZipInputStream(archive.getInputStream())
                var entry: ZipEntry
                var clazz: Class<*>
                while (zis.getNextEntry().also { entry = it } != null) {
                    if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue
                    clazz = mapping.getArchiveClass(toClassName(entry.getName()), null)
                    if (clazz == null) continue
                    val srcInf: SourceInfo = ASMUtil.getSourceInfo(mapping.getConfig(), clazz, onlyCFC)
                    if (name.equalsIgnoreCase(srcInf.name)) {
                        return mapping.getPageSource(srcInf.relativePath)
                    }
                }
            } catch (ioe: IOException) {
                LogUtil.log(mapping.getConfig(), "mapping", ioe)
            } finally {
                try {
                    IOUtil.close(zis)
                } catch (ioe: IOException) {
                    LogUtil.log(mapping.getConfig(), "mapping", ioe)
                }
            }
        }
        // TODO Auto-generated method stub
        return null
    }

    private fun toClassName(name: String): String {
        return name.replace('/', '.').substring(0, name.length() - 6)
    }

    private fun searchPhysical(mapping: Mapping, name: String, filter: ResourceFilter?): PageSource? {
        val physical: Resource = mapping.getPhysical()
        if (physical != null) {
            val _path = searchPhysical(mapping.getPhysical(), null, name, filter, true)
            if (_path != null) {
                return mapping.getPageSource(_path)
            }
        }
        return null
    }

    private fun searchPhysical(res: Resource, dir: String?, name: String, filter: ResourceFilter, top: Boolean): String? {
        var dir = dir
        if (res.isFile()) {
            if (res.getName().equalsIgnoreCase(name)) {
                return dir + res.getName()
            }
        } else if (res.isDirectory()) {
            val _dir: Array<Resource> = res.listResources(if (top) DirectoryResourceFilter.FILTER else filter)
            if (_dir != null) {
                dir = if (dir == null) "/" else dir + res.getName().toString() + "/"
                var path: String?
                for (i in _dir.indices) {
                    path = searchPhysical(_dir[i], dir, name, filter, false)
                    if (path != null) return path
                }
            }
        }
        return null
    }

    fun getMatch(pc: PageContext?, trace: StackTraceElement): SourceInfo? {
        return getMatch(pc, null, trace)
    }

    fun getMatch(config: Config?, trace: StackTraceElement): SourceInfo? {
        return getMatch(null, config, trace)
    }

    fun getMatch(pc: PageContext?, config: Config?, trace: StackTraceElement): SourceInfo? {
        var config: Config? = config
        if (trace.getFileName() == null) return null
        if (pc == null && config == null) config = ThreadLocalPageContext.getConfig()

        // PageContext pc = ThreadLocalPageContext.get();
        val mappings: Array<Mapping> = if (pc != null) ConfigWebUtil.getAllMappings(pc) else ConfigWebUtil.getAllMappings(config)
        if (pc != null) config = pc.getConfig()
        var mapping: Mapping
        var clazz: Class
        for (i in mappings.indices) {
            mapping = mappings[i]
            // print.e("virtual:"+mapping.getVirtual()+"+"+trace.getClassName());
            // look for the class in that mapping
            clazz = (mapping as MappingImpl).loadClass(trace.getClassName())
            if (clazz == null) continue

            // classname is not distinct, because of that we must check class content
            try {
                val si: SourceInfo = ASMUtil.getSourceInfo(config, clazz, false)
                if (si != null && trace.getFileName() != null && trace.getFileName().equals(si.absolutePath(pc))) return si
            } catch (e: IOException) {
            }
        }
        return null
    }
}