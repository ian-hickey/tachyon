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
package lucee.runtime.engine

import lucee.runtime.CFMLFactory

internal object Surveillance {
    private val PAGE_POOL: Collection.Key? = KeyImpl.getInstance("pagePool")
    private val CLASS_LOADER: Collection.Key? = KeyImpl.getInstance("classLoader")
    private val QUERY_CACHE: Collection.Key? = KeyImpl.getInstance("queryCache")
    private val PAGE_CONTEXT_STACK: Collection.Key? = KeyImpl.getInstance("pageContextStack")
    @Throws(PageException::class)
    fun getInfo(config: Config?): Struct? {
        val sct: Struct = StructImpl()

        // memory
        val mem = DoubleStruct()
        sct.set(KeyConstants._memory, mem)
        getInfoMemory(mem, config)

        // count
        // ScopeContext sc = ((CFMLFactoryImpl)config.getFactory()).getScopeContext();
        // sc.getSessionCount(pc)
        return sct
    }

    @Throws(PageException::class)
    private fun getInfoMemory(parent: Struct?, config: Config?) {
        val server = DoubleStruct()
        val web = DoubleStruct()
        parent.set(KeyConstants._server, server)
        parent.set(KeyConstants._web, web)
        val isConfigWeb = config is ConfigWeb

        // server
        /*
		 * ConfigServer cs=isConfigWeb? config.getConfigServerImpl(): ((ConfigServer)config);
		 */

        // infoResources(server,cs);
        // web
        if (isConfigWeb) {
            _getInfoMemory(web, server, config as ConfigWeb?)
        } else {
            val configs: Array<ConfigWeb?> = (config as ConfigServer?).getConfigWebs()
            for (i in configs.indices) {
                _getInfoMemory(web, server, configs[i])
            }
        }
    }

    @Throws(PageException::class)
    private fun _getInfoMemory(web: Struct?, server: Struct?, config: ConfigWeb?) {
        val sct = DoubleStruct()
        infoMapping(sct, config)
        // infoResources(sct,config);
        infoScopes(sct, server, config)
        infoPageContextStack(sct, config.getFactory())
        // infoQueryCache(sct,config.getFactory());
        // size+=infoResources(sct,cs);
        web.set(config.getConfigDir().getPath(), sct)
    }

    @Throws(PageException::class)
    private fun infoMapping(parent: Struct?, config: Config?) {
        val map = DoubleStruct()
        infoMapping(map, config.getMappings(), false)
        infoMapping(map, config.getCustomTagMappings(), true)
        parent.set(KeyConstants._mappings, map)
    }

    @Throws(PageException::class)
    private fun infoMapping(map: Struct?, mappings: Array<Mapping?>?, isCustomTagMapping: Boolean) {
        if (mappings == null) return
        val sct = DoubleStruct()
        var size: Long
        var mapping: MappingImpl?
        for (i in mappings.indices) {
            mapping = mappings[i] as MappingImpl?

            // archive classloader
            size = if (mapping.getArchive() != null) mapping.getArchive().length() else 0
            sct.set("archiveClassLoader", Caster.toDouble(size))

            // physical classloader
            size = if (mapping.getPhysical() != null) mapping.getPhysical().length() else 0
            sct.set("physicalClassLoader", Caster.toDouble(size))

            // pagepool
            // size = SizeOf.size(mapping.getPageSourcePool());
            // sct.set(PAGE_POOL, Caster.toDouble(size));
            map.set(if (!isCustomTagMapping) mapping.getVirtual() else mapping.getStrPhysical(), sct)
        }
    }

    @Throws(PageException::class)
    private fun infoScopes(web: Struct?, server: Struct?, config: ConfigWeb?) {
        val sc: ScopeContext = (config.getFactory() as CFMLFactoryImpl).getScopeContext()
        val webScopes = DoubleStruct()
        val srvScopes = DoubleStruct()
        var s: Long
        s = sc.getScopesSize(Scope.SCOPE_SESSION)
        webScopes.set("session", Caster.toDouble(s))
        s = sc.getScopesSize(Scope.SCOPE_APPLICATION)
        webScopes.set("application", Caster.toDouble(s))
        s = sc.getScopesSize(Scope.SCOPE_CLUSTER)
        srvScopes.set("cluster", Caster.toDouble(s))
        s = sc.getScopesSize(Scope.SCOPE_SERVER)
        srvScopes.set("server", Caster.toDouble(s))
        s = sc.getScopesSize(Scope.SCOPE_CLIENT)
        webScopes.set("client", Caster.toDouble(s))
        web.set(KeyConstants._scopes, webScopes)
        server.set(KeyConstants._scopes, srvScopes)
    }

    @Throws(PageException::class)
    private fun infoPageContextStack(parent: Struct?, factory: CFMLFactory?) {
        val size: Long = (factory as CFMLFactoryImpl?).getPageContextsSize()
        parent.set(PAGE_CONTEXT_STACK, Caster.toDouble(size))
    }
}