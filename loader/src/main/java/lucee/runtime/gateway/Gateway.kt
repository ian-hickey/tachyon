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
package lucee.runtime.gateway

import java.io.IOException

interface Gateway {
    /**
     * method to initialize the gateway
     *
     * @param engine the gateway engine
     * @param id the id of the gateway
     * @param cfcPath the path to the listener component
     * @param config the configuration as map
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun init(engine: GatewayEngine?, id: String?, cfcPath: String?, config: Map<String?, String?>?)

    /**
     * returns the id of the gateway
     *
     * @return the id of the gateway
     */
    fun getId(): String?

    /**
     * sends a message based on given data
     *
     * @param data data
     * @return answer from gateway
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun sendMessage(data: Map<*, *>?): String?

    /**
     * return helper object
     *
     * @return helper object
     */
    fun getHelper(): Object?

    /**
     * starts the gateway
     *
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun doStart()

    /**
     * stop the gateway
     *
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun doStop()

    /**
     * restart the gateway
     *
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun doRestart()

    /**
     * returns a string that is used by the event gateway administrator to display status
     *
     * @return status (STARTING, RSTOPPING, STOPPED, FAILED)
     */
    fun getState(): Int

    companion object {
        const val STARTING = 1
        const val RUNNING = 2
        const val STOPPING = 3
        const val STOPPED = 4
        const val FAILED = 5
    }
}