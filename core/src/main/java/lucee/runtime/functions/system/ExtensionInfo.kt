package lucee.runtime.functions.system

import lucee.commons.lang.StringUtil

class ExtensionInfo : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toString(args[0])) else throw FunctionException(pc, "ExtensionExists", 1, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 2627423175121799118L
        private val BUNDLES: Key? = KeyImpl.getInstance("bundles")
        private val TLDS: Key? = KeyImpl.getInstance("tlds")
        private val FLDS: Key? = KeyImpl.getInstance("flds")
        private val EVENT_GATEWAYS: Key? = KeyImpl.getInstance("eventGateways")
        private val TAGS: Key? = KeyImpl.getInstance("tags")
        private val FUNCTIONS: Key? = KeyConstants._functions
        private val ARCHIVES: Key? = KeyImpl.getInstance("archives")
        private val CONTEXTS: Key? = KeyImpl.getInstance("contexts")
        private val WEBCONTEXTS: Key? = KeyImpl.getInstance("webcontexts")
        private val CONFIG: Key? = KeyConstants._config
        private val COMPONENTS: Key? = KeyImpl.getInstance("components")
        private val APPLICATIONS: Key? = KeyImpl.getInstance("applications")
        private val CATEGORIES: Key? = KeyImpl.getInstance("categories")
        private val PLUGINS: Key? = KeyImpl.getInstance("plugins")
        private val START_BUNDLES: Key? = KeyImpl.getInstance("startBundles")
        private val TRIAL: Key? = KeyImpl.getInstance("trial")
        private val RELEASE_TYPE: Key? = KeyImpl.getInstance("releaseType")
        private val SYMBOLIC_NAME: Key? = KeyImpl.getInstance("symbolicName")
        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?): Struct? {
            if (StringUtil.isEmpty(id, true)) return StructImpl()
            val info: Struct? = getInfo(id.trim(), (pc.getConfig() as ConfigWebPro).getRHExtensions())
            return if (info.size() > 0) info else getInfo(id.trim(), (pc.getConfig() as ConfigWebPro).getServerRHExtensions())
        }

        @Throws(PageException::class)
        private fun getInfo(id: String?, extensions: Array<RHExtension?>?): Struct? {
            val sct: Struct = StructImpl()
            for (ext in extensions!!) {
                if (ext.getId().equalsIgnoreCase(id) || ext.getSymbolicName().equalsIgnoreCase(id)) {
                    val ver: String = ext.getVersion().toString()
                    val sName: String = ext.getSymbolicName()
                    sct.set(KeyConstants._id, ext.getId())
                    sct.set(SYMBOLIC_NAME, sName)
                    sct.set(KeyConstants._name, ext.getName())
                    sct.set(KeyConstants._image, ext.getImage())
                    sct.set(KeyConstants._description, ext.getDescription())
                    sct.set(KeyConstants._version, if (ver == null) null else ver)
                    sct.set(TRIAL, ext.isTrial())
                    sct.set(RELEASE_TYPE, RHExtension.toReleaseType(ext.getReleaseType(), "all"))
                    try {
                        sct.set(FLDS, Caster.toArray(ext.getFlds()))
                        sct.set(TLDS, Caster.toArray(ext.getTlds()))
                        sct.set(FUNCTIONS, Caster.toArray(ext.getFunctions()))
                        sct.set(ARCHIVES, Caster.toArray(ext.getArchives()))
                        sct.set(TAGS, Caster.toArray(ext.getTags()))
                        sct.set(CONTEXTS, Caster.toArray(ext.getContexts()))
                        sct.set(WEBCONTEXTS, Caster.toArray(ext.getWebContexts()))
                        sct.set(CONFIG, Caster.toArray(ext.getConfigs()))
                        sct.set(EVENT_GATEWAYS, Caster.toArray(ext.getEventGateways()))
                        sct.set(CATEGORIES, Caster.toArray(ext.getCategories()))
                        sct.set(APPLICATIONS, Caster.toArray(ext.getApplications()))
                        sct.set(COMPONENTS, Caster.toArray(ext.getComponents()))
                        sct.set(PLUGINS, Caster.toArray(ext.getPlugins()))
                        sct.set(START_BUNDLES, Caster.toBoolean(ext.getStartBundles()))
                        val bfs: Array<BundleInfo?> = ext.getBundles()
                        val qryBundles: Query = QueryImpl(arrayOf<Key?>(KeyConstants._name, KeyConstants._version), bfs?.size
                                ?: 0, "bundles")
                        if (bfs != null) {
                            for (i in bfs.indices) {
                                qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName())
                                if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString())
                            }
                        }
                        sct.set("Bundles", qryBundles)
                    } catch (e: Exception) {
                        throw Caster.toPageException(e)
                    }
                }
            }
            return sct
        }
    }
}