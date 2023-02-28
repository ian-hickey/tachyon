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

interface RequestMonitor : Monitor {
    @Throws(IOException::class)
    fun log(pc: PageContext?, error: Boolean)

    /**
     * returns data for a single context
     *
     * @param config config
     * @param arguments arguments
     * @return Returns a query.
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getData(config: ConfigWeb?, arguments: Map<String?, Object?>?): Query?
}