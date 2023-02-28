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
package lucee.cli

import java.io.File

//import lucee.cli.servlet.ServletConfigImpl;
//import lucee.cli.servlet.ServletContextImpl;
class CLIFactory(root: File, servletName: String?, config: Map<String, String>) : Thread() {
    private val root: File
    private val servletName: String?
    private val config: Map<String, String>
    private var idleTime: Long
    @Override
    fun run() {
        val name: String = root.getAbsolutePath()
        var current: InetAddress? = null
        current = try {
            InetAddress.getLocalHost()
        } catch (e1: UnknownHostException) {
            e1.printStackTrace()
            return
        }
        try {
            try {
                // first try to call existing service
                invoke(current, name)
            } catch (e: ConnectException) {
                startInvoker(name)
                invoke(current, name)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @Throws(RemoteException::class, NotBoundException::class)
    private operator fun invoke(current: InetAddress?, name: String) {
        val registry: Registry = LocateRegistry.getRegistry(current.getHostAddress(), PORT)
        registry.lookup(name).invoke(config)
    }

    @Throws(ServletException::class, RemoteException::class)
    private fun startInvoker(name: String) {
        val myReg: Registry? = getRegistry(PORT)
        val invoker = CLIInvokerImpl(root, servletName)
        myReg.rebind(name, UnicastRemoteObject.exportObject(invoker, 0))
        if (idleTime > 0) {
            val closer = Closer(myReg, invoker, name, idleTime)
            closer.setDaemon(false)
            closer.start()
        }
    }

    companion object {
        private const val PORT = 8893
        fun getRegistry(port: Int): Registry? {
            var registry: Registry? = null
            try {
                registry = LocateRegistry.createRegistry(port)
            } catch (e: RemoteException) {
            }
            try {
                if (registry == null) registry = LocateRegistry.getRegistry(port)
            } catch (e: RemoteException) {
            }
            RemoteServer.setLog(System.out)
            return registry
        }
    }

    init {
        this.root = root
        this.servletName = servletName
        this.config = config
        idleTime = 60000
        val strIdle = config["idle"]
        if (strIdle != null) try {
            idleTime = Long.parseLong(strIdle)
        } catch (t: Throwable) {
        }
    }
}