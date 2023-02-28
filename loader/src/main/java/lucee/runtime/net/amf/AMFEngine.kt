/**
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
package lucee.runtime.net.amf

import java.io.IOException

/**
 * AMF Engine
 */
interface AMFEngine {
    /**
     * method to initialize the engine
     *
     * @param config config
     * @param arguments arguments
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun init(config: ConfigWeb?, arguments: Map<String?, String?>?)

    /**
     * Main entry point for the AMF (Flex) Engine
     *
     * @param servlet Sevlet
     * @param req request
     * @param rsp response
     *
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun service(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?)
}