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
package tachyon.runtime.functions.component

import java.io.File

object ComponentListPackage : Function {
    private const val serialVersionUID = 6502632300879457687L
    private val FILTER_CFC: ExtensionResourceFilter? = ExtensionResourceFilter(Constants.getComponentExtensions())
    private val FILTER_CLASS: ExtensionResourceFilter? = ExtensionResourceFilter(".class")
    private val EMPTY: Array<String?>? = arrayOfNulls<String?>(0)
    @Throws(PageException::class)
    fun call(pc: PageContext?, packageName: String?): Array? {
        val names: Set<String?>?
        names = try {
            _call(pc, packageName)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        val arr: Array = ArrayImpl()
        var name: String
        val it = names!!.iterator()
        while (it.hasNext()) {
            name = it.next()
            if (Constants.isComponentExtension(ResourceUtil.getExtension(name, ""))) {
                name = ResourceUtil.removeExtension(name, name)
            }
            arr.appendEL(name)
        }
        return arr
    }

    @Throws(IOException::class, ApplicationException::class)
    private fun _call(pc: PageContext?, packageName: String?): Set<String?>? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        var rtn: Set<String?>? = null
        // var SEP=server.separator.file;

        // get environment configuration
        val searchLocal = if (packageName.indexOf('.') === -1) true else config.getComponentLocalSearch()
        val searchRoot: Boolean = config.getComponentRootSearch()
        val path: String = StringUtil.replace(packageName, ".", File.separator, false)

        // search local
        if (searchLocal) {
            val ps: PageSource = pci.getRelativePageSourceExisting(path)
            if (ps != null) {
                val mapping: Mapping = ps.getMapping()
                var _path: String = ps.getRealpath()
                _path = ListUtil.trim(_path, "\\/")
                val list = _listMapping(pc, mapping, _path)
                if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list)
            }
        }

        // check mappings (this includes the webroot)
        if (searchRoot) {
            val virtual = "/" + StringUtil.replace(packageName, ".", "/", false)
            val mappings: Array<Mapping?> = config.getMappings()
            var mapping: Mapping?
            var _path: String
            var list: Array<String?>?
            for (i in mappings.indices) {
                mapping = mappings[i]
                if (StringUtil.startsWithIgnoreCase(virtual, mapping.getVirtual())) {
                    _path = ListUtil.trim(virtual.substring(mapping.getVirtual().length()), "\\/").trim()
                    _path = StringUtil.replace(_path, "/", File.separator, false)
                    list = _listMapping(pc, mapping, _path)
                    if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list)
                }
            }
        }

        // check component mappings
        var mappings: Array<Mapping?> = config.getComponentMappings()
        var mapping: Mapping?
        var list: Array<String?>?
        if (mappings != null) {
            for (i in mappings.indices) {
                mapping = mappings[i]
                list = _listMapping(pc, mapping, path)
                if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list)
            }
        }

        // check application component mappings
        val ac: ApplicationContext = pc.getApplicationContext()
        if (ac != null) {
            mappings = ac.getComponentMappings()
            if (mappings != null) {
                for (i in mappings.indices) {
                    mapping = mappings[i]
                    list = _listMapping(pc, mapping, path)
                    if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list)
                }
            }
        }
        if (rtn == null) throw ApplicationException("no package with name [$packageName] found")
        return rtn
    }

    private fun add(set: Set<String?>?, arr: Array<String?>?): Set<String?>? {
        var set = set
        if (set == null) set = HashSet<String?>()
        for (i in arr.indices) {
            set.add(arr!![i])
        }
        return set
    }

    @Throws(IOException::class)
    private fun _listMapping(pc: PageContext?, mapping: Mapping?, path: String?): Array<String?>? {
        if (mapping.isPhysicalFirst()) {
            // check physical
            var list = _listPhysical(path, mapping)
            if (!ArrayUtil.isEmpty(list)) return list

            // check archive
            list = _listArchive(pc, path, mapping)
            if (!ArrayUtil.isEmpty(list)) return list
        } else {
            // check archive
            var list = _listArchive(pc, path, mapping)
            if (!ArrayUtil.isEmpty(list)) return list
            // check physical
            list = _listPhysical(path, mapping)
            if (!ArrayUtil.isEmpty(list)) return list
        }
        return null
    }

    private fun _listPhysical(path: String?, mapping: Mapping?): Array<String?>? {
        val physical: Resource = mapping.getPhysical()
        if (physical != null) {
            val dir: Resource = physical.getRealResource(path)
            if (dir.isDirectory()) {
                return dir.list(FILTER_CFC)
            }
        }
        return EMPTY
    }

    @Throws(IOException::class)
    private fun _listArchive(pc: PageContext?, path: String?, mapping: Mapping?): Array<String?>? {
        val packageName: String = StringUtil.replace(path, File.separator, ".", false)
        val archive: Resource = mapping.getArchive()
        if (archive != null) {
            // TODO nor working with pathes with none ascci characters, eith none ascci characters, the java
            // class path is renamed, so make sure you rename the path as well
            val strDir = "zip://" + archive + "!" + File.separator + path
            val dir: Resource = ResourceUtil.toResourceNotExisting(pc, strDir, true, false)
            if (dir.isDirectory()) {
                val list: MutableList<String?> = ArrayList<String?>()
                // we use the class files here to get the info, the source files are optional and perhaps not
                // present.
                val children: Array<Resource?> = dir.listResources(FILTER_CLASS)
                var className: String?
                var c: String?
                var sourceName: String? = null
                for (i in children.indices) {
                    className = children[i].getName()
                    className = className.substring(0, className.length() - 6)
                    className = "$packageName.$className"
                    try {
                        val clazz: Class<*> = mapping.getArchiveClass(className)
                        sourceName = ASMUtil.getSourceInfo(pc.getConfig(), clazz, true).name
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                    }
                    if (StringUtil.isEmpty(sourceName)) {
                        c = IOUtil.toString(children[i], null as Charset?)
                        val loc: Int = c.indexOf("<clinit>")
                        if (loc != -1) {
                            c = c.substring(0, loc)
                            c = ListUtil.last(c, "/\\", true).trim()
                            if (Constants.isComponentExtension(ResourceUtil.getExtension(c, ""))) list.add(c)
                        }
                    } else list.add(sourceName)
                }
                if (list.size() > 0) return list.toArray(arrayOfNulls<String?>(list.size()))
            }
        }
        return null
    }
}