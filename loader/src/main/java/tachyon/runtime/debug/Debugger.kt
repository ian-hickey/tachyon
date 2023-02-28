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
package tachyon.runtime.debug

import java.io.IOException

/**
 * debugger interface
 */
interface Debugger {
    fun init(config: Config?)

    /**
     * reset the debug object
     */
    fun reset()

    /**
     * @param pc current PagContext
     * @param source Page Source for the entry
     * @return returns a single DebugEntry.
     */
    fun getEntry(pc: PageContext?, source: PageSource?): DebugEntryTemplate?

    /**
     * @param pc current PagContext
     * @param source Page Source for the entry
     * @param key key
     * @return returns a single DebugEntry with a key.
     */
    fun getEntry(pc: PageContext?, source: PageSource?, key: String?): DebugEntryTemplate?

    /**
     * returns a single DebugEntry for a specific postion (startPos,endPos in the PageSource)
     *
     * @param pc current PagContext
     * @param source Page Source for the entry
     * @param startPos start position in the file
     * @param endPos end position in the file
     * @return returns a debug entry.
     */
    fun getEntry(pc: PageContext?, source: PageSource?, startPos: Int, endPos: Int): DebugEntryTemplatePart?

    /**
     * sets if toHTML print html output info or not
     *
     * @param output The output to set.
     */
    fun setOutput(output: Boolean)

    /**
     * @return Returns the queries.
     */
    val queries: List<tachyon.runtime.debug.QueryEntry?>?

    /**
     * @param pc page context
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun writeOut(pc: PageContext?)

    /**
     * returns the Debugging Info
     *
     * @param pc page context
     * @return debugging Info
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getDebuggingData(pc: PageContext?): Struct?

    @Throws(PageException::class)
    fun getDebuggingData(pc: PageContext?, addAddionalInfo: Boolean): Struct?

    /**
     * adds new Timer info to debug
     *
     * @param label Label
     * @param exe Execution time
     * @param template Template
     * @return debug timer object
     */
    fun addTimer(label: String?, exe: Long, template: String?): DebugTimer?

    /**
     * add new Trace to debug
     *
     * @param type type
     * @param category category
     * @param text text
     * @param page page
     * @param varName variable name
     * @param varValue variable value
     * @return debug trace object
     */
    fun addTrace(type: Int, category: String?, text: String?, page: PageSource?, varName: String?, varValue: String?): DebugTrace?
    fun addTrace(type: Int, category: String?, text: String?, template: String?, line: Int, action: String?, varName: String?, varValue: String?): DebugTrace?
    val traces: Array<tachyon.runtime.debug.DebugTrace?>?
    fun addException(config: Config?, pe: PageException?)
    val exceptions: Array<Any?>?
    fun addImplicitAccess(scope: String?, name: String?)
    fun getImplicitAccesses(scope: Int, name: String?): Array<ImplicitAccess?>?

    /**
     * add new query execution time
     *
     * @param query query
     * @param datasource datasource name
     * @param name name
     * @param sql sql
     * @param recordcount recordcount
     * @param src src
     * @param time time
     * @see .addQuery
     */
    @Deprecated
    @Deprecated("""use instead
	  """)
    fun addQuery(query: Query?, datasource: String?, name: String?, sql: SQL?, recordcount: Int, src: PageSource?, time: Int)

    /**
     * add new query execution time
     *
     * @param query query
     * @param datasource datasource
     * @param name name
     * @param sql sql
     * @param recordcount recordcount
     * @param src src
     * @param time time
     */
    fun addQuery(query: Query?, datasource: String?, name: String?, sql: SQL?, recordcount: Int, src: PageSource?, time: Long)
    fun getTraces(pc: PageContext?): Array<DebugTrace?>?

    /**
     *
     * @param labelCategory the name of the category, multiple records with the same category get
     * combined
     * @param data you wanna show
     */
    fun addGenericData(labelCategory: String?, data: Map<String?, String?>?)

    /**
     * returning the generic data set for this request
     *
     * @return a Map organized by category/data-column/data-value
     */
    val genericData: Map<String?, Map<String?, List<String?>?>?>?
    fun addDump(ps: PageSource?, dump: String?): DebugDump?
    fun setOutputLog(outputLog: DebugOutputLog?)
}