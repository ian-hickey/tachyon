/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.op

import java.io.File

/**
 * implemention of the ctration object
 */
class CreationImpl private constructor(engine: CFMLEngine?) : Creation, Serializable {
    @Override
    fun createArray(): Array? {
        return ArrayImpl()
    }

    @Override
    fun createArray(list: String?, delimiter: String?, removeEmptyItem: Boolean, trim: Boolean): Array? {
        if (removeEmptyItem) return ListUtil.listToArrayRemoveEmpty(list, delimiter)
        return if (trim) ListUtil.listToArrayTrim(list, delimiter) else ListUtil.listToArray(list, delimiter)
    }

    @Override
    @Throws(PageException::class)
    fun createArray(dimension: Int): Array? {
        return ArrayUtil.getInstance(dimension)
    }

    @Override
    fun createStruct(): Struct? {
        return StructImpl()
    }

    @Override
    fun createStruct(type: Int): Struct? {
        return StructImpl(type)
    }

    @Override
    @Throws(ApplicationException::class)
    fun createStruct(type: String?): Struct? {
        val t: Int = StructNew.toType(type)
        return if (t == StructImpl.TYPE_LINKED_CASESENSITIVE || t == StructImpl.TYPE_CASESENSITIVE) {
            MapAsStruct.toStruct(if (t == StructImpl.TYPE_LINKED_CASESENSITIVE) Collections.synchronizedMap(LinkedHashMap()) else ConcurrentHashMap(), true)
        } else StructImpl(StructNew.toType(type))
    }

    @Override
    fun createQuery(columns: Array<String?>?, rows: Int, name: String?): Query? {
        return QueryImpl(columns, rows, name)
    }

    @Override
    @Throws(DatabaseException::class)
    fun createQuery(columns: Array<Collection.Key?>?, rows: Int, name: String?): Query? {
        return QueryImpl(columns, rows, name)
    }

    @Override
    @Throws(DatabaseException::class)
    fun createQuery(columns: Array<String?>?, types: Array<String?>?, rows: Int, name: String?): Query? {
        return QueryImpl(columns, types, rows, name)
    }

    @Override
    @Throws(DatabaseException::class)
    fun createQuery(columns: Array<Collection.Key?>?, types: Array<String?>?, rows: Int, name: String?): Query? {
        return QueryImpl(columns, types, rows, name)
    }

    @Override
    @Throws(PageException::class)
    fun createQuery(dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: Int, name: String?): Query? {
        return QueryImpl(ThreadLocalPageContext.get(), dc, sql, maxrow, fetchsize, TimeSpanImpl.fromMillis(timeout * 1000), name)
    }

    @Override
    fun createDateTime(time: Long): DateTime? {
        return DateTimeImpl(time, false)
    }

    @Override
    fun createTimeSpan(day: Int, hour: Int, minute: Int, second: Int): TimeSpan? {
        return TimeSpanImpl(day, hour, minute, second)
    }

    @Override
    fun createDate(time: Long): Date? {
        return DateImpl(time)
    }

    @Override
    fun createTime(time: Long): Time? {
        return TimeImpl(time, false)
    }

    @Override
    @Throws(ExpressionException::class)
    fun createDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, millis: Int): DateTime? {
        return DateTimeUtil.getInstance().toDateTime(ThreadLocalPageContext.getTimeZone(), year, month, day, hour, minute, second, millis)
    }

    @Override
    @Throws(ExpressionException::class)
    fun createDate(year: Int, month: Int, day: Int): Date? {
        return DateImpl(DateTimeUtil.getInstance().toDateTime(null, year, month, day, 0, 0, 0, 0))
    }

    @Override
    fun createTime(hour: Int, minute: Int, second: Int, millis: Int): Time? {
        return TimeImpl(DateTimeUtil.getInstance().toTime(null, 1899, 12, 30, hour, minute, second, millis, 0), false)
    }

    @Override
    fun createKey(key: String?): Key? {
        return KeyImpl.init(key)
    }

    @Override
    fun createRemoteClientTask(plans: Array<ExecutionPlan?>?, remoteClient: RemoteClient?, attrColl: Struct?, callerId: String?, type: String?): SpoolerTask? {
        return RemoteClientTask(plans, remoteClient, attrColl, callerId, type)
    }

    @Override
    fun createClusterEntry(key: Key?, value: Serializable?, offset: Int): ClusterEntry? {
        return ClusterEntryImpl(key, value, offset)
    }

    @Override
    @Throws(PageException::class)
    fun createResource(path: String?, existing: Boolean): Resource? {
        return if (existing) ResourceUtil.toResourceExisting(ThreadLocalPageContext.get(), path) else ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), path)
    }

    @Override
    fun createHttpServletRequest(contextRoot: File?, serverName: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?, headers: Map<String?, Object?>?,
                                 parameters: Map<String?, String?>?, attributes: Map<String?, Object?>?, session: HttpSession?): HttpServletRequest? {

        // header
        val _headers: Array<Pair<String?, Object?>?> = arrayOfNulls<Pair?>(headers!!.size())
        run {
            var index = 0
            val it: Iterator<Entry<String?, Object?>?> = headers.entrySet().iterator()
            var entry: Entry<String?, Object?>?
            while (it.hasNext()) {
                entry = it.next()
                _headers[index++] = Pair<String?, Object?>(entry.getKey(), entry.getValue())
            }
        }
        // parameters
        val _parameters: Array<Pair<String?, Object?>?> = arrayOfNulls<Pair?>(headers!!.size())
        run {
            var index = 0
            val it: Iterator<Entry<String?, String?>?> = parameters.entrySet().iterator()
            var entry: Entry<String?, String?>?
            while (it.hasNext()) {
                entry = it.next()
                _parameters[index++] = Pair<String?, Object?>(entry.getKey(), entry.getValue())
            }
        }
        return HttpServletRequestDummy(ResourceUtil.toResource(contextRoot), serverName, scriptName, queryString, cookies, _headers, _parameters,
                Caster.toStruct(attributes, null), session, null)
    }

    @Override
    fun createHttpServletResponse(io: OutputStream?): HttpServletResponse? {
        return HttpServletResponseDummy(io) // do not change, flex extension is depending on this
    }

    // FUTURE add to interface
    fun createServletConfig(root: File?, attributes: Map<String?, Object?>?, params: Map<String?, String?>?): ServletConfig? {
        var root: File? = root
        var attributes: Map<String?, Object?>? = attributes
        var params = params
        val servletName = ""
        if (attributes == null) attributes = HashMap<String?, Object?>()
        if (params == null) params = HashMap<String?, String?>()
        if (root == null) root = File(".") // working directory that the java command was called from
        val servletContext = ServletContextImpl(root, attributes, params, 1, 0)
        return ServletConfigImpl(servletContext, servletName)
    }

    @Override
    fun createPageContext(req: HttpServletRequest?, rsp: HttpServletResponse?, out: OutputStream?): PageContext? {
        val config: Config = ThreadLocalPageContext.getConfig() as? ConfigWeb
                ?: throw RuntimeException("need a web context to create a PageContext")
        val factory: CFMLFactory = (config as ConfigWeb).getFactory()
        return factory.getPageContext(factory.getServlet(), req, rsp, null, false, -1, false) as PageContext
    }

    @Override
    @Throws(PageException::class)
    fun createComponentFromName(pc: PageContext?, fullName: String?): Component? {
        return pc.loadComponent(fullName)
    }

    @Override
    @Throws(PageException::class)
    fun createComponentFromPath(pc: PageContext?, path: String?): Component? {
        var path = path
        path = path.trim()
        var pathContracted: String? = ContractPath.call(pc, path)
        if (Constants.isComponentExtension(ResourceUtil.getExtension(pathContracted, ""))) pathContracted = ResourceUtil.removeExtension(pathContracted, pathContracted)
        pathContracted = pathContracted.replace(File.pathSeparatorChar, '.').replace('/', '.').replace('\\', '.')
        while (pathContracted.toLowerCase().startsWith(".")) pathContracted = pathContracted.substring(1)
        return createComponentFromName(pc, pathContracted)
    }

    @Override
    fun createRefBoolean(b: Boolean): RefBoolean? {
        return RefBooleanImpl(b)
    }

    @Override
    fun createRefInteger(i: Int): RefInteger? {
        return RefIntegerImpl(i)
    }

    // FUTURE add this and more to interface
    fun createRefInteger(i: Int, _syncronized: Boolean): RefInteger? {
        return if (_syncronized) RefIntegerSync(i) else RefIntegerImpl(i)
    }

    @Override
    fun createRefLong(l: Long): RefLong? {
        return RefLongImpl(l)
    }

    @Override
    fun createRefDouble(d: Long): RefDouble? {
        return RefDoubleImpl(d)
    }

    @Override
    fun createRefString(value: String?): RefString? {
        return RefStringImpl(value)
    }

    @Override
    fun createUUID(): String? {
        return CreateUUID.invoke()
    }

    @Override
    fun createGUID(): String? {
        return CreateGUID.invoke()
    }

    @Override
    fun createProperty(name: String?, type: String?): Property? {
        val pi = PropertyImpl()
        pi.setName(name)
        pi.setType(type)
        return pi
    }

    @Override
    fun createMapping(config: Config?, virtual: String?, strPhysical: String?, strArchive: String?, inspect: Short, physicalFirst: Boolean, hidden: Boolean, readonly: Boolean,
                      topLevel: Boolean, appMapping: Boolean, ignoreVirtual: Boolean, appListener: ApplicationListener?, listenerMode: Int, listenerType: Int): Mapping? {
        return MappingImpl(config, virtual, strPhysical, strArchive, inspect, physicalFirst, hidden, readonly, topLevel, appMapping, ignoreVirtual, appListener, listenerMode,
                listenerType)
    }

    @Override
    fun createCastableStruct(value: Object?): Struct? {
        return CastableStruct(value)
    }

    @Override
    fun createCastableStruct(value: Object?, type: Int): Struct? {
        return CastableStruct(value, type)
    }

    @Override
    fun now(): DateTime? {
        return DateTimeImpl()
    }

    @Override
    fun <K> createKeyLock(): KeyLock<K?>? {
        // TODO Auto-generated method stub
        return KeyLockImpl<K?>()
    }

    companion object {
        private var singelton: CreationImpl? = null

        /**
         * @return singleton instance
         */
        fun getInstance(engine: CFMLEngine?): Creation? {
            if (singelton == null) singelton = CreationImpl(engine)
            return singelton
        }
    }
}