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
package lucee.runtime.listener

import java.util.ArrayList

class JavaSettingsImpl : JavaSettings {
    private val resources: Array<Resource?>?
    private var resourcesTranslated: Array<Resource?>?
    private val bundles: Array<Resource?>?
    private var bundlesTranslated: List<Resource?>? = null
    private val loadCFMLClassPath: Boolean
    private val reloadOnChange: Boolean
    private val watchInterval: Int
    private val watchedExtensions: Array<String?>?
    private var hasBundlesTranslated = false

    constructor() {
        resources = arrayOfNulls<Resource?>(0)
        bundles = arrayOfNulls<Resource?>(0)
        loadCFMLClassPath = false
        reloadOnChange = false
        watchInterval = 60
        watchedExtensions = arrayOf("jar", "class")
    }

    constructor(resources: Array<Resource?>?, bundles: Array<Resource?>?, loadCFMLClassPath: Boolean?, reloadOnChange: Boolean, watchInterval: Int, watchedExtensions: Array<String?>?) {
        this.resources = resources
        this.bundles = bundles
        this.loadCFMLClassPath = loadCFMLClassPath!!
        this.reloadOnChange = reloadOnChange
        this.watchInterval = watchInterval
        this.watchedExtensions = watchedExtensions
    }

    @Override
    fun getResources(): Array<Resource?>? {
        return resources
    }

    @Override
    fun getResourcesTranslated(): Array<Resource?>? {
        if (resourcesTranslated == null) {
            val list: List<Resource?> = ArrayList<Resource?>()
            _getResourcesTranslated(list, resources, true)
            resourcesTranslated = list.toArray(arrayOfNulls<Resource?>(list.size()))
        }
        return resourcesTranslated
    }

    // FUTURE interface
    fun getBundles(): Array<Resource?>? {
        return bundles
    }

    // FUTURE interface
    fun getBundlesTranslated(): List<Resource?>? {
        if (!hasBundlesTranslated) {
            val list: List<Resource?> = ArrayList<Resource?>()
            _getBundlesTranslated(list, bundles, true, true)
            bundlesTranslated = list
            if (bundlesTranslated != null && bundlesTranslated!!.isEmpty()) bundlesTranslated = null
            hasBundlesTranslated = true
        }
        return bundlesTranslated
    }

    @Override
    fun loadCFMLClassPath(): Boolean {
        return loadCFMLClassPath
    }

    @Override
    fun reloadOnChange(): Boolean {
        return reloadOnChange
    }

    @Override
    fun watchInterval(): Int {
        return watchInterval
    }

    @Override
    fun watchedExtensions(): Array<String?>? {
        return watchedExtensions
    }

    companion object {
        fun _getResourcesTranslated(list: List<Resource?>?, resources: Array<Resource?>?, deep: Boolean) {
            if (ArrayUtil.isEmpty(resources)) return
            for (resource in resources!!) {
                if (resource.isFile()) {
                    if (ResourceUtil.getExtension(resource, "").equalsIgnoreCase("jar")) list.add(resource)
                } else if (deep && resource.isDirectory()) {
                    list.add(resource) // add as possible classes dir
                    _getResourcesTranslated(list, resource.listResources(), false)
                }
            }
        }

        fun _getBundlesTranslated(list: List<Resource?>?, resources: Array<Resource?>?, deep: Boolean, checkFiles: Boolean) {
            if (ArrayUtil.isEmpty(resources)) return
            for (resource in resources!!) {
                if (resource.isDirectory()) {
                    list.add(ResourceUtil.getCanonicalResourceEL(resource))
                    if (deep) _getBundlesTranslated(list, resource.listResources(), false, false)
                } else if (checkFiles && resource.isFile()) {
                    val bf: BundleFile = BundleFile.getInstance(resource, null)
                    if (bf != null && bf.isBundle()) list.add(resource)
                }
            }
        }

        fun newInstance(base: JavaSettings?, sct: Struct?): JavaSettingsImpl? {
            // load paths
            var paths: List<Resource?>?
            run {
                val obj: Object = sct.get(KeyImpl.getInstance("loadPaths"), null)
                if (obj != null) {
                    paths = loadPaths(ThreadLocalPageContext.get(), obj)
                } else paths = ArrayList<Resource?>()
            }

            // bundles paths
            var bundles: List<Resource?>?
            run {
                var obj: Object = sct.get(KeyImpl.getInstance("bundlePaths"), null)
                if (obj == null) obj = sct.get(KeyImpl.getInstance("bundles"), null)
                if (obj == null) obj = sct.get(KeyImpl.getInstance("bundleDirectory"), null)
                if (obj == null) obj = sct.get(KeyImpl.getInstance("bundleDirectories"), null)
                if (obj != null) {
                    bundles = loadPaths(ThreadLocalPageContext.get(), obj)
                } else bundles = ArrayList<Resource?>()
            }
            // loadCFMLClassPath
            var loadCFMLClassPath: Boolean = Caster.toBoolean(sct.get(KeyImpl.getInstance("loadCFMLClassPath"), null), null)
            if (loadCFMLClassPath == null) loadCFMLClassPath = Caster.toBoolean(sct.get(KeyImpl.getInstance("loadColdFusionClassPath"), null), null)
            if (loadCFMLClassPath == null) loadCFMLClassPath = base.loadCFMLClassPath()

            // reloadOnChange
            val reloadOnChange: Boolean = Caster.toBooleanValue(sct.get(KeyImpl.getInstance("reloadOnChange"), null), base.reloadOnChange())

            // watchInterval
            val watchInterval: Int = Caster.toIntValue(sct.get(KeyImpl.getInstance("watchInterval"), null), base.watchInterval())

            // watchExtensions
            val obj: Object = sct.get(KeyImpl.getInstance("watchExtensions"), null)
            val extensions: List<String?> = ArrayList<String?>()
            if (obj != null) {
                var arr: Array?
                if (Decision.isArray(obj)) {
                    try {
                        arr = Caster.toArray(obj)
                    } catch (e: PageException) {
                        arr = ArrayImpl()
                    }
                } else {
                    arr = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(Caster.toString(obj, ""), ',')
                }
                val it: Iterator<Object?> = arr.valueIterator()
                var ext: String?
                while (it.hasNext()) {
                    ext = Caster.toString(it.next(), null)
                    if (StringUtil.isEmpty(ext)) continue
                    ext = ext.trim()
                    if (ext.startsWith(".")) ext = ext.substring(1)
                    if (ext.startsWith("*.")) ext = ext.substring(2)
                    extensions.add(ext)
                }
            }
            return JavaSettingsImpl(paths.toArray(arrayOfNulls<Resource?>(paths!!.size())), bundles.toArray(arrayOfNulls<Resource?>(bundles!!.size())), loadCFMLClassPath, reloadOnChange, watchInterval,
                    extensions.toArray(arrayOfNulls<String?>(extensions.size())))
        }

        private fun loadPaths(pc: PageContext?, obj: Object?): List<Resource?>? {
            var obj: Object? = obj
            var res: Resource
            if (!Decision.isArray(obj)) {
                val list: String = Caster.toString(obj, null)
                if (!StringUtil.isEmpty(list)) {
                    obj = ListUtil.listToArray(list, ',')
                }
            }
            if (Decision.isArray(obj)) {
                val arr: Array = Caster.toArray(obj, null)
                val list: MutableList<Resource?> = ArrayList<Resource?>()
                val it: Iterator<Object?> = arr.valueIterator()
                while (it.hasNext()) {
                    try {
                        val path: String = Caster.toString(it.next(), null) ?: continue
                        res = AppListenerUtil.toResourceExisting(pc.getConfig(), pc.getApplicationContext(), path, false)
                        if (res == null || !res.exists()) res = ResourceUtil.toResourceExisting(pc, path, true, null)
                        if (res != null) list.add(res)
                    } catch (e: Exception) {
                        LogUtil.log(pc, ModernApplicationContext::class.java.getName(), e)
                    }
                }
                return list
            }
            return null
        }

        fun getBundleDirectories(pc: PageContext?): List<Resource?>? {
            var pc: PageContext? = pc
            pc = ThreadLocalPageContext.get(pc)
            if (pc == null) return null
            val ac: ApplicationContext = pc.getApplicationContext() ?: return null
            val js = ac.getJavaSettings() as JavaSettingsImpl
                    ?: return null
            return js.getBundlesTranslated()
        }
    }
}