/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.component

import java.util.concurrent.ConcurrentHashMap

object ComponentLoader {
    private const val RETURN_TYPE_PAGE: Short = 1
    private const val RETURN_TYPE_INTERFACE: Short = 2
    private const val RETURN_TYPE_COMPONENT: Short = 3
    private val tokens: ConcurrentHashMap<String?, String?>? = ConcurrentHashMap<String?, String?>()
    private val DIR_OR_EXT: ResourceFilter? = OrResourceFilter(arrayOf<ResourceFilter?>(DirectoryResourceFilter.FILTER, ExtensionResourceFilter(Constants.getComponentExtensions())))
    private val EMPTY_ID: Array<ImportDefintion?>? = arrayOfNulls<ImportDefintion?>(0)

    /**
     *
     * @param pc
     * @param loadingLocation
     * @param rawPath
     * @param searchLocal
     * @param searchRoot
     * @param isExtendedComponent if set to true this is a base component loaded because another
     * component has defined this component via extends
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun searchComponent(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?, isExtendedComponent: Boolean): ComponentImpl? {
        return _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, true, RETURN_TYPE_COMPONENT, isExtendedComponent, true) as ComponentImpl?
    }

    @Throws(PageException::class)
    fun searchComponent(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?,
                        isExtendedComponent: Boolean, executeConstr: Boolean): ComponentImpl? {
        return _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, executeConstr, RETURN_TYPE_COMPONENT, isExtendedComponent, true) as ComponentImpl?
    }

    @Throws(PageException::class)
    fun searchComponent(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?,
                        isExtendedComponent: Boolean, executeConstr: Boolean, validate: Boolean): ComponentImpl? {
        return _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, executeConstr, RETURN_TYPE_COMPONENT, isExtendedComponent, validate) as ComponentImpl?
    }

    @Throws(PageException::class)
    fun getStaticScope(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?): StaticScope? {
        val cp: ComponentPageImpl? = searchComponentPage(pc, loadingLocation, rawPath, searchLocal, searchRoot)
        var ss: StaticScope = cp.getStaticScope()

        // if there is no static scope stored yet, we need to load it
        if (ss == null) {
            synchronized(cp.getPageSource().getDisplayPath().toString() + ":" + getToken(cp.getHash().toString() + "")) {
                ss = cp.getStaticScope()
                if (ss == null) {
                    ss = searchComponent(pc, loadingLocation, rawPath, searchLocal, searchRoot, false, false).staticScope()
                    cp.setStaticScope(ss)
                    return ss
                }
            }
        }

        // check if one of the base components did change
        val index: Long = cp.getIndex()
        var reload = false
        var bc: ComponentImpl?
        var c: Component? = ss.getComponent()
        while (c.getBaseComponent() as ComponentImpl?. also { bc = it } != null) {
            val bcp: ComponentPageImpl = (bc._getPageSource() as PageSourceImpl).loadPage(pc, false, null) as ComponentPageImpl
            if (bcp.getStaticStruct() != null) {
                val idx: Long = bcp.getStaticStruct().index()
                if (idx == 0L || idx > index) {
                    reload = true
                    break
                }
            }
            c = bc
        }

        // if we had changes we need to reload
        if (reload) {
            ss = searchComponent(pc, loadingLocation, rawPath, searchLocal, searchRoot, false, false).staticScope()
            cp.setStaticScope(ss)
        }
        return ss
    }

    @Throws(PageException::class)
    fun searchComponentPage(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?): ComponentPageImpl? {
        return searchComponentPage(pc, loadingLocation, rawPath, searchLocal, searchRoot, true)
    }

    @Throws(PageException::class)
    fun searchComponentPage(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?, validate: Boolean): ComponentPageImpl? {
        val obj: Object? = _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, false, RETURN_TYPE_PAGE, false, validate)
        if (obj is ComponentPageImpl) return obj as ComponentPageImpl?
        val dialect: Int = pc.getCurrentTemplateDialect()
        throw ExpressionException(
                "invalid " + toStringType(RETURN_TYPE_PAGE, dialect) + " definition, can't find " + toStringType(RETURN_TYPE_PAGE, dialect) + " [" + rawPath + "]")
    }

    @Throws(PageException::class)
    fun searchInterface(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?): InterfaceImpl? {
        return _search(pc, loadingLocation, rawPath, Boolean.TRUE, Boolean.TRUE, true, RETURN_TYPE_INTERFACE, false, true) as InterfaceImpl?
    }

    @Throws(PageException::class)
    fun searchInterface(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, executeConstr: Boolean): InterfaceImpl? {
        return _search(pc, loadingLocation, rawPath, Boolean.TRUE, Boolean.TRUE, executeConstr, RETURN_TYPE_INTERFACE, false, true) as InterfaceImpl?
    }

    @Throws(PageException::class)
    fun searchInterface(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, executeConstr: Boolean, validate: Boolean): InterfaceImpl? {
        return _search(pc, loadingLocation, rawPath, Boolean.TRUE, Boolean.TRUE, executeConstr, RETURN_TYPE_INTERFACE, false, validate) as InterfaceImpl?
    }

    @Throws(PageException::class)
    fun searchPage(pc: PageContext?, child: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?): Page? {
        return _search(pc, child, rawPath, searchLocal, searchRoot, false, RETURN_TYPE_PAGE, false, true) as Page?
    }

    @Throws(PageException::class)
    fun searchPage(pc: PageContext?, child: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?, validate: Boolean): Page? {
        return _search(pc, child, rawPath, searchLocal, searchRoot, false, RETURN_TYPE_PAGE, false, validate) as Page?
    }

    @Throws(PageException::class)
    private fun _search(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?, executeConstr: Boolean, returnType: Short,
                        isExtendedComponent: Boolean, validate: Boolean): Object? {
        val currPS: PageSource = pc.getCurrentPageSource(null)
        var importDefintions: Array<ImportDefintion?>? = null
        if (currPS != null) {
            var currP: Page?
            val cfc: Component = pc.getActiveComponent()
            if (cfc is ComponentImpl && currPS.equals(cfc.getPageSource())) {
                importDefintions = (cfc as ComponentImpl)._getImportDefintions()
            } else if (currPS.loadPage(pc, false, null).also { currP = it } != null) {
                importDefintions = currP.getImportDefintions()
            }
        }
        val dialect: Int = if (currPS == null) pc.getCurrentTemplateDialect() else currPS.getDialect()
        // first try for the current dialect
        var obj: Object? = _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, executeConstr, returnType, currPS, importDefintions, dialect, isExtendedComponent, validate)
        // then we try the opposite dialect
        if (obj == null && (pc.getConfig() as ConfigPro).allowTachyonDialect()) { // only when the tachyon dialect is enabled we have to check the opposite
            obj = _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, executeConstr, returnType, currPS, importDefintions,
                    if (dialect == CFMLEngine.DIALECT_CFML) CFMLEngine.DIALECT_LUCEE else CFMLEngine.DIALECT_CFML, isExtendedComponent, validate)
        }
        if (obj == null) throw ExpressionException("invalid " + toStringType(returnType, dialect) + " definition, can't find " + toStringType(returnType, dialect) + " [" + rawPath + "]")
        return obj
    }

    @Throws(PageException::class)
    private fun _search(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?, executeConstr: Boolean, returnType: Short,
                        currPS: PageSource?, importDefintions: Array<ImportDefintion?>?, dialect: Int, isExtendedComponent: Boolean, validate: Boolean): Object? {
        var loadingLocation: PageSource? = loadingLocation
        var rawPath = rawPath
        var searchLocal = searchLocal
        var searchRoot = searchRoot
        val config: ConfigPro = pc.getConfig() as ConfigPro
        if (dialect == CFMLEngine.DIALECT_LUCEE && !config.allowTachyonDialect()) PageContextImpl.notSupported()
        var doCache: Boolean = config.useComponentPathCache()
        var sub: String? = null
        if (returnType != RETURN_TYPE_PAGE && rawPath.indexOf('$') !== -1) {
            val d: Int = rawPath.lastIndexOf('$')
            val s: Int = rawPath.lastIndexOf('.')
            if (d > s) {
                sub = rawPath.substring(d + 1)
                rawPath = rawPath.substring(0, d)
            }
        }

        // app-String appName=pc.getApplicationContext().getName();
        rawPath = rawPath.trim().replace('\\', '/')
        val path = if (rawPath.indexOf("./") === -1) rawPath.replace('.', '/') else rawPath
        val isRealPath: Boolean = !StringUtil.startsWith(path, '/')
        // PageSource currPS = pc.getCurrentPageSource();
        // Page currP=currPS.loadPage(pc,false);
        var ps: PageSource? = null
        var page: CIPage? = null

        // MUSTMUST improve to handle different extensions
        val pathWithCFC: String = path.concat("." + if (dialect == CFMLEngine.DIALECT_CFML) Constants.getCFMLComponentExtension() else Constants.getTachyonComponentExtension())

        // no cache for per application pathes
        val acm: Array<Mapping?> = pc.getApplicationContext().getComponentMappings()
        if (!ArrayUtil.isEmpty(acm)) {
            var m: Mapping?
            for (y in acm.indices) {
                m = acm[y]
                ps = m.getPageSource(pathWithCFC)
                page = toCIPage(ps.loadPageThrowTemplateException(pc, false, null as Page?))
                if (page != null) {
                    return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
                }
            }
        }
        if (searchLocal == null) searchLocal = Caster.toBoolean(if (rawPath.indexOf('.') === -1) true else config.getComponentLocalSearch())
        if (searchRoot == null) searchRoot = Caster.toBoolean(config.getComponentRootSearch())

        // CACHE
        // check local in cache
        var localCacheName: String? = null
        if (searchLocal && isRealPath && currPS != null) {
            localCacheName = currPS.getDisplayPath().replace('\\', '/')
            localCacheName = localCacheName.substring(0, localCacheName.lastIndexOf('/') + 1).concat(pathWithCFC)
            if (doCache) {
                page = config.getCachedPage(pc, localCacheName)
                if (page != null) return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
            }
        }

        // check import cache
        if (doCache && isRealPath) {
            var impDef: ImportDefintion? = config.getComponentDefaultImport()
            val impDefs: Array<ImportDefintion?>? = importDefintions ?: EMPTY_ID
            var i = -1
            do {
                if (impDef.isWildcard() || impDef.getName().equalsIgnoreCase(path)) {
                    page = config.getCachedPage(pc, "import:" + impDef.getPackageAsPath() + pathWithCFC)
                    if (page != null) return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
                }
                impDef = if (++i < impDefs!!.size) impDefs[i] else null
            } while (impDef != null)
        }
        if (doCache) {
            // check global in cache
            page = config.getCachedPage(pc, pathWithCFC)
            if (page != null) return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
        }

        // SEARCH
        // search from local
        if (searchLocal && isRealPath) {
            // check realpath
            val arr: Array<PageSource?> = (pc as PageContextImpl?).getRelativePageSources(pathWithCFC)
            page = toCIPage(PageSourceImpl.loadPage(pc, arr, null))
            if (page != null) {
                if (doCache) config.putCachedPageSource(localCacheName, page.getPageSource())
                return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
            }
        }

        // search with imports
        val ac: ApplicationContext = pc.getApplicationContext()
        // Mapping[] ccMappings = config.getComponentMappings();
        // Mapping[] acMappings = ac != null ? ac.getComponentMappings() : null;
        val compMappings: Array<Array<Mapping?>?> = arrayOf<Array<Mapping?>?>(if (ac != null) ac.getComponentMappings() else null, config.getComponentMappings())
        if (isRealPath) {
            var impDef: ImportDefintion? = config.getComponentDefaultImport()
            val impDefs: Array<ImportDefintion?>? = importDefintions ?: EMPTY_ID
            var arr: Array<PageSource?>
            var i = -1
            do {
                if (impDef.isWildcard() || impDef.getName().equalsIgnoreCase(path)) {

                    // search from local first
                    if (searchLocal) {
                        arr = (pc as PageContextImpl?).getRelativePageSources(impDef.getPackageAsPath() + pathWithCFC)
                        page = toCIPage(PageSourceImpl.loadPage(pc, arr, null))
                        if (page != null) {
                            if (doCache) config.putCachedPageSource("import:" + impDef.getPackageAsPath() + pathWithCFC, page.getPageSource())
                            return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
                        }
                    }

                    // search mappings and webroot
                    page = toCIPage(PageSourceImpl.loadPage(pc, (pc as PageContextImpl?).getPageSources("/" + impDef.getPackageAsPath() + pathWithCFC), null))
                    if (page != null) {
                        val key: String = impDef.getPackageAsPath() + pathWithCFC
                        if (doCache && !(page.getPageSource().getMapping() as MappingImpl).isAppMapping()) config.putCachedPageSource("import:$key", page.getPageSource())
                        return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
                    }

                    // search application component mappings
                    for (z in compMappings.indices) {
                        val mappings: Array<Mapping?>? = compMappings[z]
                        if (mappings != null) {
                            var m: Mapping?
                            for (y in mappings.indices) {
                                m = mappings[y]
                                ps = m.getPageSource(impDef.getPackageAsPath() + pathWithCFC)
                                page = toCIPage(ps.loadPageThrowTemplateException(pc, false, null as Page?))
                                if (page != null) {
                                    if (doCache && z > 0) config.putCachedPageSource("import:" + impDef.getPackageAsPath() + pathWithCFC, page.getPageSource())
                                    return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
                                }
                            }
                        }
                    }
                }
                impDef = if (++i < impDefs!!.size) impDefs[i] else null
            } while (impDef != null)
        }
        val p: String
        if (isRealPath) p = '/' + pathWithCFC else p = pathWithCFC

        // search mappings and webroot
        page = toCIPage(PageSourceImpl.loadPage(pc, (pc as PageContextImpl?).getPageSources(p), null))
        if (page != null) {
            if (doCache && !(page.getPageSource().getMapping() as MappingImpl).isAppMapping()) config.putCachedPageSource(pathWithCFC, page.getPageSource())
            return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
        }

        // search component mappings
        for (y in compMappings.indices) {
            val mappings: Array<Mapping?>? = compMappings[y]
            if (mappings != null) {
                var m: Mapping?
                for (i in mappings.indices) {
                    m = mappings[i]
                    ps = m.getPageSource(p)
                    page = toCIPage(ps.loadPageThrowTemplateException(pc, false, null as Page?))

                    // recursive search
                    if (page == null && config.doComponentDeepSearch() && path.indexOf('/') === -1) {
                        ps = MappingUtil.searchMappingRecursive(m, pathWithCFC, true)
                        if (ps != null) {
                            page = toCIPage(ps.loadPageThrowTemplateException(pc, false, null as Page?))
                            if (page != null) doCache = false // do not cache this, it could be ambigous
                        }
                    }
                    if (page != null) {
                        if (doCache && y > 0) config.putCachedPageSource(pathWithCFC, page.getPageSource())
                        return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
                    }
                }
            }
        }

        // search relative to active component (this get not cached because the cache get ambigous if we do)
        if (searchLocal && isRealPath) {
            if (loadingLocation == null) {
                val c: Component = pc.getActiveComponent()
                if (c != null) loadingLocation = c.getPageSource()
            }
            if (loadingLocation != null) {
                ps = loadingLocation.getRealPage(pathWithCFC)
                if (ps != null) {
                    page = toCIPage(ps.loadPageThrowTemplateException(pc, false, null as Page?))
                    if (page != null) {
                        return if (returnType == RETURN_TYPE_PAGE) page else load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr, validate)
                    }
                }
            }
        }
        // translate cfide. to org.tachyon.cfml
        if (StringUtil.startsWithIgnoreCase(rawPath, "cfide.")) {
            val rpm: String = Constants.DEFAULT_PACKAGE.toString() + "." + rawPath.substring(6)
            return try {
                _search(pc, loadingLocation, rpm, searchLocal, searchRoot, executeConstr, returnType, currPS, importDefintions, dialect, false, validate)
            } catch (ee: ExpressionException) {
                null
                // throw new ExpressionException("invalid "+toStringType(returnType)+" definition, can't find
                // "+rawPath+" or "+rpm);
            }
        }
        return null
        // throw new ExpressionException("invalid "+toStringType(returnType)+" definition, can't find
        // "+toStringType(returnType)+" ["+rawPath+"]");
    }

    private fun toStringType(returnType: Short, dialect: Int): String? {
        if (RETURN_TYPE_COMPONENT == returnType) return if (dialect == CFMLEngine.DIALECT_LUCEE) "class" else "component"
        return if (RETURN_TYPE_INTERFACE == returnType) "interface" else "component/interface"
    }

    private fun trim(str: String?): String? {
        var str = str
        if (StringUtil.startsWith(str, '.')) str = str.substring(1)
        return str
    }

    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, ps: PageSource?, callPath: String?, isRealPath: Boolean, silent: Boolean): ComponentImpl? {
        return _loadComponent(pc, toCIPage(ps.loadPage(pc, false), callPath), callPath, isRealPath, false, true, true)
    }

    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, ps: PageSource?, callPath: String?, isRealPath: Boolean, silent: Boolean, executeConstr: Boolean): ComponentImpl? {
        return _loadComponent(pc, toCIPage(ps.loadPage(pc, false), callPath), callPath, isRealPath, false, executeConstr, true)
    }

    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, ps: PageSource?, callPath: String?, isRealPath: Boolean, silent: Boolean, executeConstr: Boolean, validate: Boolean): ComponentImpl? {
        return _loadComponent(pc, toCIPage(ps.loadPage(pc, false), callPath), callPath, isRealPath, false, executeConstr, validate)
    }

    // do not change, method is used in flex extension
    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, page: Page?, callPath: String?, isRealPath: Boolean, silent: Boolean, isExtendedComponent: Boolean, executeConstr: Boolean): ComponentImpl? {
        return loadComponent(pc, page, callPath, isRealPath, silent, isExtendedComponent, executeConstr, true)
    }

    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, page: Page?, callPath: String?, isRealPath: Boolean, silent: Boolean, isExtendedComponent: Boolean, executeConstr: Boolean,
                      validate: Boolean): ComponentImpl? {
        val cip: CIPage? = toCIPage(page, callPath)
        if (silent) {
            // TODO is there a more direct way
            val bc: BodyContent = pc.pushBody()
            return try {
                _loadComponent(pc, cip, callPath, isRealPath, isExtendedComponent, executeConstr, validate)
            } finally {
                BodyContentUtil.clearAndPop(pc, bc)
            }
        }
        return _loadComponent(pc, cip, callPath, isRealPath, isExtendedComponent, executeConstr, validate)
    }

    @Throws(PageException::class)
    private fun load(pc: PageContext?, page: Page?, callPath: String?, sub: String?, isRealPath: Boolean, returnType: Short, isExtendedComponent: Boolean,
                     executeConstr: Boolean, validate: Boolean): CIObject? {
        var cip: CIPage? = toCIPage(page, callPath)
        // String subName = null;
        if (sub != null) {
            cip = loadSub(cip, sub)
        }
        if (cip is ComponentPageImpl) {
            if (returnType != RETURN_TYPE_COMPONENT) throw ApplicationException("the component [" + cip.getPageSource().getComponentName().toString() + "] cannot be used as an interface.")
            return _loadComponent(pc, cip, callPath, isRealPath, isExtendedComponent, executeConstr, validate)
        }
        if (returnType != RETURN_TYPE_INTERFACE) throw ApplicationException("the interface [" + cip.getPageSource().getComponentName().toString() + "] cannot be used as a component.")
        return loadInterface(pc, cip, cip.getPageSource(), callPath, isRealPath)
    }

    @Throws(ApplicationException::class)
    private fun loadSub(page: CIPage?, sub: String?): CIPage? {
        // TODO find a better way to create that class name
        val subClassName: String = tachyon.transformer.bytecode.Page.createSubClass(page.getPageSource().getClassName(), sub, page.getPageSource().getDialect())

        // subClassName:sub.test_cfc$cf$1$sub1
        // - sub.test_cfc$sub1$cf
        val subs: Array<CIPage?> = page.getSubPages()
        for (i in subs.indices) {
            if (subs[i].getClass().getName().equals(subClassName)) {
                return subs[i]
            }
        }
        val detail = StringBuilder()
        for (i in subs.indices) {
            if (subs[i] is SubPage) {
                if (detail.length() > 0) detail.append(",")
                detail.append((subs[i] as SubPage?).getSubname())
            }
        }
        val msg: StringBuilder = StringBuilder("There is no Sub component [").append(sub).append("] in [").append(page.getPageSource().getDisplayPath()).append("]")
        if (detail.length() > 0) throw ApplicationException(msg.toString(), "The following Sub Components are availble [" + detail + "] in [" + page.getPageSource().getDisplayPath() + "]") else throw ApplicationException(msg.toString(), "There are no Sub Components in [" + page.getPageSource().getDisplayPath().toString() + "]")
    }

    @Throws(PageException::class)
    fun loadPage(pc: PageContext?, ps: PageSource?, forceReload: Boolean): Page? {
        if (pc.getConfig().debug() && (pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
            val debugEntry: DebugEntryTemplate = pc.getDebugger().getEntry(pc, ps)
            pc.addPageSource(ps, true)
            val currTime: Long = pc.getExecutionTime()
            var exeTime: Long = 0
            val time: Long = System.currentTimeMillis()
            return try {
                debugEntry.updateFileLoadTime((System.currentTimeMillis() - time) as Int)
                exeTime = System.currentTimeMillis()
                ps.loadPage(pc, forceReload)
            } finally {
                val diff: Long = System.currentTimeMillis() - exeTime - (pc.getExecutionTime() - currTime)
                pc.setExecutionTime(pc.getExecutionTime() + (System.currentTimeMillis() - time))
                debugEntry.updateExeTime(diff)
                pc.removeLastPageSource(true)
            }
        }
        // no debug
        pc.addPageSource(ps, true)
        return try {
            ps.loadPage(pc, forceReload)
        } finally {
            pc.removeLastPageSource(true)
        }
    }

    @Throws(PageException::class)
    fun loadInline(page: CIPage?, pc: PageContext?): ComponentImpl? {
        return _loadComponent(pc, page, null, true, true, true, true).setInline()
    }

    @Throws(PageException::class)
    private fun _loadComponent(pc: PageContext?, page: CIPage?, callPath: String?, isRealPath: Boolean, isExtendedComponent: Boolean, executeConstr: Boolean,
                               validate: Boolean): ComponentImpl? {
        var rtn: ComponentImpl? = null
        if (pc.getConfig().debug() && (pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
            val debugEntry: DebugEntryTemplate = pc.getDebugger().getEntry(pc, page.getPageSource())
            pc.addPageSource(page.getPageSource(), true)
            val currTime: Long = pc.getExecutionTime()
            var exeTime: Long = 0
            val time: Long = System.nanoTime()
            try {
                debugEntry.updateFileLoadTime((System.nanoTime() - time) as Int)
                exeTime = System.nanoTime()
                rtn = initComponent(pc, page, callPath, isRealPath, isExtendedComponent, executeConstr, validate)
            } finally {
                if (rtn != null) rtn.setLoaded(true)
                val diff: Long = System.nanoTime() - exeTime - (pc.getExecutionTime() - currTime)
                pc.setExecutionTime(pc.getExecutionTime() + (System.nanoTime() - time))
                debugEntry.updateExeTime(diff)
                pc.removeLastPageSource(true)
            }
        } else {
            pc.addPageSource(page.getPageSource(), true)
            rtn = try {
                initComponent(pc, page, callPath, isRealPath, isExtendedComponent, executeConstr, validate)
            } finally {
                if (rtn != null) rtn.setLoaded(true)
                pc.removeLastPageSource(true)
            }
        }
        return rtn
    }

    @Throws(PageException::class)
    fun loadInterface(pc: PageContext?, page: Page?, ps: PageSource?, callPath: String?, isRealPath: Boolean): InterfaceImpl? {
        var page: Page? = page
        var rtn: InterfaceImpl? = null
        if (pc.getConfig().debug() && (pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
            val debugEntry: DebugEntryTemplate = pc.getDebugger().getEntry(pc, ps)
            pc.addPageSource(ps, true)
            val currTime: Long = pc.getExecutionTime()
            var exeTime: Long = 0
            val time: Long = System.nanoTime()
            try {
                debugEntry.updateFileLoadTime((System.nanoTime() - time) as Int)
                exeTime = System.nanoTime()
                if (page == null) page = ps.loadPage(pc, false)
                rtn = initInterface(pc, page, callPath, isRealPath)
            } finally {
                val diff: Long = System.nanoTime() - exeTime - (pc.getExecutionTime() - currTime)
                pc.setExecutionTime(pc.getExecutionTime() + (System.nanoTime() - time))
                debugEntry.updateExeTime(diff)
                pc.removeLastPageSource(true)
            }
        } else {
            pc.addPageSource(ps, true)
            try {
                if (page == null) page = ps.loadPage(pc, false)
                rtn = initInterface(pc, page, callPath, isRealPath)
            } finally {
                pc.removeLastPageSource(true)
            }
        }
        return rtn
    }

    @Throws(PageException::class)
    private fun initInterface(pc: PageContext?, page: Page?, callPath: String?, isRealPath: Boolean): InterfaceImpl? {
        if (page !is InterfacePageImpl) throw ApplicationException("invalid interface definition [$callPath]")
        val ip: InterfacePageImpl? = page as InterfacePageImpl?
        return ip.newInstance(pc, callPath, isRealPath)
    }

    @Throws(PageException::class)
    private fun initComponent(pc: PageContext?, page: CIPage?, callPath: String?, isRealPath: Boolean, isExtendedComponent: Boolean, executeConstr: Boolean,
                              validate: Boolean): ComponentImpl? {
        // is not a component, then it has to be an interface
        if (validate && page !is ComponentPageImpl) throw ApplicationException("you cannot instantiate the interface [" + page.getPageSource().getDisplayPath()
                .toString() + "] as a component (" + page.getClass().getName().toString() + "" + (page is InterfacePageImpl).toString() + ")")
        val cp: ComponentPageImpl? = page as ComponentPageImpl?
        val c: ComponentImpl = cp.newInstance(pc, callPath, isRealPath, isExtendedComponent, executeConstr)
        // abstract/final check
        if (validate) {
            if (!isExtendedComponent) {
                if (c.getModifier() === Component.MODIFIER_ABSTRACT) throw ApplicationException("you cannot instantiate an abstract component [" + page.getPageSource().getDisplayPath().toString() + "], this component can only be extended by other components")
            } else if (c.getModifier() === Component.MODIFIER_FINAL) throw ApplicationException("you cannot extend a final component [" + page.getPageSource().getDisplayPath().toString() + "]")
        }
        c.setInitalized(true)
        return c
    }

    @Throws(PageException::class)
    private fun toCIPage(p: Page?, callPath: String?): CIPage? {
        if (p is CIPage) return p as CIPage?
        if (p != null) throw ApplicationException("invalid component definition [" + callPath + "] in template [" + p.getPageSource().getDisplayPath() + "]")
        throw ApplicationException("invalid component definition [$callPath] ")
    }

    private fun toCIPage(p: Page?): CIPage? {
        return if (p is CIPage) p as CIPage? else null
    }

    fun getToken(key: String?): String? {
        var lock: String? = tokens.putIfAbsent(key, key)
        if (lock == null) {
            lock = key
        }
        return lock
    }
}