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
package tachyon.runtime.util

import java.io.File

/**
 * Creation of different Objects
 */
interface Creation {
    /**
     * creates and returns an array instance
     *
     * @return array
     */
    fun createArray(): Array?

    /**
     * creates and returns an array based on a string list
     *
     * @param list string list
     * @param delimiter delimiter
     * @param removeEmptyItem remove Empty Item
     * @param trim trim
     * @return array
     */
    fun createArray(list: String?, delimiter: String?, removeEmptyItem: Boolean, trim: Boolean): Array?

    /**
     * creates and returns a DateTime instance
     *
     * @param time time
     * @return DateTime
     */
    fun createDateTime(time: Long): DateTime?

    /**
     * creates and returns a DateTime instance
     *
     * @param year year
     * @param month month
     * @param day day
     * @param hour hour
     * @param minute minute
     * @param seond second
     * @param millis milliseconds
     * @return DateTime
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, seond: Int, millis: Int): DateTime?

    /**
     * creates and returns a Date instance
     *
     * @param time time
     * @return DateTime
     */
    fun createDate(time: Long): Date?

    /**
     * creates and returns a Date instance
     *
     * @param year year
     * @param month month
     * @param day day
     * @return DateTime
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createDate(year: Int, month: Int, day: Int): Date?

    /**
     * creates and returns a Time instance
     *
     * @param time time
     * @return DateTime
     */
    fun createTime(time: Long): Time?

    /**
     * creates and returns a Time instance
     *
     * @param hour hour
     * @param minute minute
     * @param second second
     * @param millis millis
     * @return DateTime
     */
    fun createTime(hour: Int, minute: Int, second: Int, millis: Int): Time?

    /**
     * creates and returns a TimeSpan instance
     *
     * @param day day
     * @param hour hour
     * @param minute minute
     * @param second second
     * @return TimeSpan
     */
    fun createTimeSpan(day: Int, hour: Int, minute: Int, second: Int): TimeSpan?

    /**
     * creates and returns an array instance
     *
     * @param dimension Array dimension
     * @return array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createArray(dimension: Int): Array?

    /**
     * creates and returns a struct instance
     *
     * @return struct
     */
    fun createStruct(): Struct?
    fun createStruct(type: Int): Struct?

    @Deprecated
    @Throws(PageException::class)
    fun createStruct(type: String?): Struct?
    fun createCastableStruct(value: Object?): Struct?
    fun createCastableStruct(value: Object?, type: Int): Struct?

    /**
     * creates a query object with given data
     *
     * @param columns Query Columns
     * @param rows rows
     * @param name name
     * @return created query Object
     */
    @Deprecated
    @Deprecated("use instead <code>createQuery(Collection.Key[] columns, int rows, String name)</code>")
    fun createQuery(columns: Array<String?>?, rows: Int, name: String?): Query?

    /**
     * creates a query object with given data
     *
     * @param columns Query Columns
     * @param rows rows
     * @param name name
     * @return created query Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createQuery(columns: Array<Collection.Key?>?, rows: Int, name: String?): Query?

    /**
     * creates a query object with given data
     *
     * @param columns Query Columns
     * @param types Column Types
     * @param rows rows
     * @param name name
     * @return created query Object
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>createQuery(Collection.Key[] columns, String[] types, int rows, String name)</code>""")
    @Throws(PageException::class)
    fun createQuery(columns: Array<String?>?, types: Array<String?>?, rows: Int, name: String?): Query?

    /**
     * creates a query object with given data
     *
     * @param columns Query columns
     * @param types Column Types
     * @param rows rows
     * @param name name
     * @return created query Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createQuery(columns: Array<Collection.Key?>?, types: Array<String?>?, rows: Int, name: String?): Query?

    /**
     * @param dc Connection to a database
     * @param sql sql to execute
     * @param maxrow maxrow for the resultset
     * @param fetchsize fetch size
     * @param timeout in seconds
     * @param name name
     * @return created Query
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createQuery(dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: Int, name: String?): Query?

    /**
     * creates a collection Key out of a String
     *
     * @param key key
     * @return key
     */
    fun createKey(key: String?): Collection.Key?
    fun createRemoteClientTask(plans: Array<ExecutionPlan?>?, remoteClient: RemoteClient?, attrColl: Struct?, callerId: String?, type: String?): SpoolerTask?
    fun createClusterEntry(key: Key?, value: Serializable?, offset: Int): ClusterEntry?

    @Throws(PageException::class)
    fun createResource(path: String?, existing: Boolean): Resource?
    fun createHttpServletRequest(contextRoot: File?, serverName: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?,
                                 headers: Map<String?, Object?>?, parameters: Map<String?, String?>?, attributes: Map<String?, Object?>?, session: HttpSession?): HttpServletRequest?

    fun createHttpServletResponse(io: OutputStream?): HttpServletResponse?
    fun createPageContext(req: HttpServletRequest?, rsp: HttpServletResponse?, out: OutputStream?): PageContext?
    // FUTURE public ServletConfig createServletConfig(File root, Map<String, Object> attributes,
    // Map<String, String> params)
    /**
     * creates a component object from (Full)Name, for example tachyon.extensions.net.HTTPUtil
     *
     * @param pc Pagecontext for loading the CFC
     * @param fullName full name of the cfc example:tachyon.extensions.net.HTTPUtil
     * @return loaded cfc
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createComponentFromName(pc: PageContext?, fullName: String?): Component?

    /**
     * creates a component object from an absolute local path, for example
     * /Users/susi/Projects/Sorglos/wwwrooot/tachyon/extensions/net/HTTPUtil.cfc
     *
     * @param pc Pagecontext for loading the CFC
     * @param path path of the cfc example:/Users/susi/Projects/Sorglos/wwwrooot/
     * tachyon/extensions/net/HTTPUtil.cfc
     * @return loaded cfc
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun createComponentFromPath(pc: PageContext?, path: String?): Component?
    fun createRefBoolean(b: Boolean): RefBoolean?
    fun createRefInteger(i: Int): RefInteger?
    fun createRefLong(l: Long): RefLong?
    fun createRefDouble(d: Long): RefDouble?
    fun createUUID(): String?
    fun createGUID(): String?
    fun createProperty(name: String?, type: String?): Property?
    fun createMapping(config: Config?, virtual: String?, strPhysical: String?, strArchive: String?, inspect: Short, physicalFirst: Boolean, hidden: Boolean,
                      readonly: Boolean, topLevel: Boolean, appMapping: Boolean, ignoreVirtual: Boolean, appListener: ApplicationListener?, listenerMode: Int, listenerType: Int): Mapping?

    fun now(): DateTime?
    fun <K> createKeyLock(): KeyLock<K>?
    fun createRefString(value: String?): RefString?
}