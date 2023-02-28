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
package lucee.runtime.monitor

import java.io.IOException

class RequestMonitorWrap(monitor: Object?) : MonitorWrap(monitor, TYPE_REQUEST), RequestMonitor {
    private var log: Method? = null
    private var getData: Method? = null
    private val getDataRaw: Method? = null
    @Override
    @Throws(IOException::class)
    fun log(pc: PageContext?, error: Boolean) {
        try {
            if (log == null) {
                log = monitor.getClass().getMethod("log", PARAMS_LOG)
            }
            log.invoke(monitor, arrayOf<Object?>(pc, Caster.toBoolean(error)))
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun getData(config: ConfigWeb?, arguments: Map<String?, Object?>?): Query? {
        return try {
            if (getData == null) {
                getData = monitor.getClass().getMethod("getData", arrayOf<Class?>(ConfigWeb::class.java, Map::class.java))
            }
            getData.invoke(monitor, arrayOf(config, arguments)) as Query
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    } /*
	 * public Query getData(ConfigWeb config,long minAge, long maxAge, int maxrows) throws IOException{
	 * try { if(getData==null) { getData=monitor.getClass().getMethod("getData", new
	 * Class[]{long.class,long.class,int.class}); } return (Query) getData.invoke(monitor, new
	 * Object[]{Long.valueOf(minAge),Long.valueOf(maxAge),Integer.valueOf(maxrows)}); } catch (Exception e) { throw
	 * ExceptionUtil.toIOException(e); } }
	 * 
	 * public Query getDataRaw(ConfigWeb config, long minAge, long maxAge) throws IOException { try {
	 * if(getDataRaw==null) { getDataRaw=monitor.getClass().getMethod("getDataRaw", new
	 * Class[]{ConfigWeb.class,long.class,long.class}); } return (Query) getDataRaw.invoke(monitor, new
	 * Object[]{config,Long.valueOf(minAge),Long.valueOf(maxAge)}); } catch (Exception e) { throw
	 * ExceptionUtil.toIOException(e); } }
	 */

    companion object {
        private val PARAMS_LOG: Array<Class?>? = arrayOf<Class?>(PageContext::class.java, Boolean::class.javaPrimitiveType)
    }
}