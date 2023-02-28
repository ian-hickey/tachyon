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
package lucee.commons.io.log

import java.util.Iterator

/**
 *
 */
class LoggerAndSourceData(config: Config, id: String, name: String, appender: ClassDefinition, appenderArgs: Map<String?, String>, layout: ClassDefinition,
                          layoutArgs: Map<String?, String>, level: Int, readOnly: Boolean, dyn: Boolean) {
    private var _log: Log? = null
    private val cdAppender: ClassDefinition
    private var _appender: Object? = null
    private val appenderArgs: Map<String?, String>
    private val cdLayout: ClassDefinition
    private var layout: Object? = null
    private val layoutArgs: Map<String?, String>
    val level: Int
    val name: String
    private var config: Config
    val readOnly: Boolean
    private val id: String
    val dyn: Boolean
    fun id(): String {
        return id
    }

    private fun init() {
        if (_log == null) {
            config = ThreadLocalPageContext.getConfig(config)
            try {
                layout = eng().getLayout(cdLayout, layoutArgs, cdAppender, name)
                _appender = eng().getAppender(config, layout, name, cdAppender, appenderArgs)
                _log = eng().getLogger(config, _appender, name, level)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    val appenderClassDefinition: ClassDefinition
        get() = cdAppender

    /*
	 * public Object getAppender() { getLog();// initialize if necessary return _appender; }
	 */
    @Throws(PageException::class)
    fun close() {
        if (_log != null) {
            val a: Object? = _appender
            _log = null
            layout = null
            if (a != null) eng().closeAppender(a) // a.close();
            _appender = null
        }
    }

    @Throws(PageException::class)
    fun getAppenderArgs(catchException: Boolean): Map<String?, String> {
        getLog(catchException) // initialize if necessary
        return appenderArgs
    }

    /*
	 * public Object getLayout() { getLog();// initialize if necessary return layout; }
	 */
    val layoutClassDefinition: ClassDefinition
        get() = cdLayout

    @Throws(PageException::class)
    fun getLayoutArgs(catchException: Boolean): Map<String?, String> {
        getLog(catchException) // initialize if necessary
        return layoutArgs
    }

    @Throws(PageException::class)
    fun getLog(catchException: Boolean): Log? {
        if (_log == null) {
            config = ThreadLocalPageContext.getConfig(config)
            try {
                layout = eng().getLayout(cdLayout, layoutArgs, cdAppender, name)
                _appender = eng().getAppender(config, layout, name, cdAppender, appenderArgs)
                _log = eng().getLogger(config, _appender, name, level)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (!catchException) throw Caster.toPageException(t)
                SystemOut.printDate(t)
            }
        }
        return _log
    }

    private fun eng(): LogEngine {
        return (config as ConfigPro).getLogEngine()
    }

    companion object {
        private const val DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n"

        /*
	 * public Logger getLogger() { getLog();// make sure it exists return
	 * ((LogAdapter)_log).getLogger(); }
	 */
        fun id(name: String?, appender: ClassDefinition?, appenderArgs: Map<String, String>?, layout: ClassDefinition?, layoutArgs: Map<String, String>?, level: Int,
               readOnly: Boolean): String {
            val sb: StringBuilder = StringBuilder(name).append(';').append(appender).append(';')
            toString(sb, appenderArgs)
            sb.append(';').append(layout).append(';')
            toString(sb, layoutArgs)
            sb.append(';').append(level).append(';').append(readOnly)
            return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX)
        }

        private fun toString(sb: StringBuilder, map: Map<String, String>?) {
            if (map == null) return
            val it: Iterator<Entry<String, String>> = map.entrySet().iterator()
            var e: Entry<String, String>
            while (it.hasNext()) {
                e = it.next()
                sb.append(e.getKey()).append(':').append(e.getValue()).append('|')
            }
        }
    }

    init {
        // this.log=new LogAdapter(logger);
        this.config = config
        this.id = id
        this.name = name
        cdAppender = appender
        this.appenderArgs = appenderArgs
        cdLayout = layout
        this.layoutArgs = layoutArgs
        this.level = level
        this.readOnly = readOnly
        this.dyn = dyn
        init()
    }
}