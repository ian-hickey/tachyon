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
package lucee.runtime.rest

import java.util.ArrayList

class Mapping(config: Config?, virtual: String?, physical: String?, hidden: Boolean, readonly: Boolean, _default: Boolean) {
    /**
     * @return the virtual
     */
    val virtual: String? = null
    private val physical: Resource?

    /**
     * @return the strPhysical
     */
    val strPhysical: String?

    /**
     * @return the hidden
     */
    val isHidden: Boolean

    /**
     * @return the readonly
     */
    val isReadonly: Boolean
    var isDefault: Boolean
    private var baseSources: List<Source?>? = null
    private val customSources: Map<Resource?, List<Source?>?>? = HashMap<Resource?, List<Source?>?>()
    @Throws(PageException::class)
    private fun init(pc: PageContext?, reset: Boolean): List<Source?>? {
        if (reset) release()
        val locations: Array<Resource?> = pc.getApplicationContext().getRestCFCLocations()

        // base source
        if (ArrayUtil.isEmpty(locations)) {
            if (baseSources == null && physical != null && physical.isDirectory()) {
                baseSources = _init(pc, this, physical)
            }
            return baseSources
        }

        // custom sources
        val rtn: List<Source?> = ArrayList<Source?>()
        var list: List<Source?>?
        for (i in locations.indices) {
            list = customSources!![locations[i]]
            if (list == null && locations[i].isDirectory()) {
                list = _init(pc, this, locations[i])
                customSources.put(locations[i], list)
            }
            copy(list, rtn)
        }
        return rtn
    }

    private fun copy(src: List<Source?>?, trg: List<Source?>?) {
        if (src == null) return
        val it: Iterator<Source?> = src.iterator()
        while (it.hasNext()) {
            trg.add(it.next())
        }
    }

    fun duplicate(config: Config?, readOnly: Boolean?): Mapping? {
        return Mapping(config, virtual, strPhysical, isHidden, if (readOnly == null) isReadonly else readOnly.booleanValue(), isDefault)
    }

    /**
     * @return the physical
     */
    fun getPhysical(): Resource? {
        return physical
    }

    val virtualWithSlash: String?
        get() = virtual.toString() + "/"

    @Throws(PageException::class)
    fun getResult(pc: PageContext?, path: String?, matrix: Struct?, format: Int, hasFormatExtension: Boolean, accept: List<MimeType?>?, contentType: MimeType?, defaultValue: Result?): Result? {
        val sources: List<Source?>? = init(pc, false)
        val it: Iterator<Source?> = sources!!.iterator()
        var src: Source?
        var arrPath: Array<String?>
        val subPath: Array<String?>
        var index: Int
        while (it.hasNext()) {
            src = it.next()
            val variables: Struct = StructImpl()
            arrPath = RestUtil.splitPath(path)
            index = RestUtil.matchPath(variables, src!!.getPath(), arrPath)
            if (index != -1) {
                subPath = arrayOfNulls<String?>(arrPath.size - 1 - index)
                System.arraycopy(arrPath, index + 1, subPath, 0, subPath.size)
                return Result(src, variables, subPath, matrix, format, hasFormatExtension, accept, contentType)
            }
        }
        return defaultValue
    }

    @Throws(PageException::class)
    fun reset(pc: PageContext?) {
        init(pc, true)
    }

    @Synchronized
    fun release() {
        if (baseSources != null) {
            baseSources.clear()
            baseSources = null
        }
        customSources.clear()
    }

    companion object {
        private val _FILTER_CFML: ResourceFilter? = AndResourceFilter(arrayOf<ResourceFilter?>(ExtensionResourceFilter(Constants.getCFMLComponentExtension()), object : ResourceFilter() {
            @Override
            fun accept(res: Resource?): Boolean {
                return !Constants.CFML_APPLICATION_EVENT_HANDLER.equalsIgnoreCase(res.getName())
            }
        }))
        private val _FILTER_LUCEE: ResourceFilter? = AndResourceFilter(arrayOf<ResourceFilter?>(ExtensionResourceFilter(Constants.getLuceeComponentExtension()), object : ResourceFilter() {
            @Override
            fun accept(res: Resource?): Boolean {
                return !Constants.LUCEE_APPLICATION_EVENT_HANDLER.equalsIgnoreCase(res.getName())
            }
        }))
        private val FILTER: ResourceFilter? = OrResourceFilter(arrayOf<ResourceFilter?>(_FILTER_CFML, _FILTER_LUCEE))
        @Throws(PageException::class)
        private fun _init(pc: PageContext?, mapping: Mapping?, dir: Resource?): ArrayList<Source?>? {
            val children: Array<Resource?> = dir.listResources(FILTER)
            val settings: RestSettings = pc.getApplicationContext().getRestSettings()
            val sources: ArrayList<Source?> = ArrayList<Source?>()
            var ps: PageSource
            var cfc: Component
            var meta: Struct
            var path: String
            for (i in children.indices) {
                try {
                    ps = pc.toPageSource(children[i], null)
                    cfc = ComponentLoader.loadComponent(pc, ps, children[i].getName(), true, true)
                    meta = cfc.getMetaData(pc)
                    if (Caster.toBooleanValue(meta.get(KeyConstants._rest, null), false)) {
                        path = Caster.toString(meta.get(KeyConstants._restPath, null), null)
                        sources.add(Source(mapping, cfc.getPageSource(), path))
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (!settings.getSkipCFCWithError()) throw Caster.toPageException(t)
                }
            }
            return sources
        }
    }

    init {
        if (!virtual.startsWith("/")) this.virtual = "/$virtual"
        if (virtual.endsWith("/")) this.virtual = virtual.substring(0, virtual!!.length() - 1) else this.virtual = virtual
        strPhysical = physical
        isHidden = hidden
        isReadonly = readonly
        isDefault = _default
        if (config !is ConfigWeb) return
        val cw: ConfigWeb? = config as ConfigWeb?
        this.physical = ConfigWebUtil.getExistingResource(cw.getServletContext(), physical, null, cw.getConfigDir(), FileUtil.TYPE_DIR, cw, true)
    }
}