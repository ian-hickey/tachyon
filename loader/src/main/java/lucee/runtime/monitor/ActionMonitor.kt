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

// added with Lucee 4.1
interface ActionMonitor : Monitor {
    /**
     * logs certain action within a Request
     *
     * @param pc page context
     * @param type type
     * @param label label
     * @param executionTime execution time
     * @param data data
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun log(pc: PageContext?, type: String?, label: String?, executionTime: Long, data: Object?)

    /**
     * logs certain action outside a Request, like sending mails
     *
     * @param config config
     * @param type type
     * @param label label
     * @param executionTime execution time
     * @param data data
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun log(config: ConfigWeb?, type: String?, label: String?, executionTime: Long, data: Object?)

    @Throws(PageException::class)
    fun getData(arguments: Map<String?, Object?>?): Query?
}