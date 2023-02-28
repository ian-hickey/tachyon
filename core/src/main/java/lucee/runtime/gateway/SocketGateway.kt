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

import java.io.BufferedReader

class SocketGateway : GatewaySupport {
    private var engine: GatewayEngine? = null
    private var port = 0
    private var welcomeMessage: String? = "Welcome to the Lucee Socket Gateway"
    private var id: String? = null
    private var cfmlEngine: CFMLEngine? = null
    private var caster: Cast? = null
    private var creator: Creation? = null
    private val sockets: List<SocketServerThread?>? = ArrayList<SocketServerThread?>()
    private var serverSocket: ServerSocket? = null
    protected var state: Int = STOPPED
    private var cfcPath: String? = null
    private var thread: Thread? = null
    @Override
    @Throws(GatewayException::class)
    fun init(engine: GatewayEngine?, id: String?, cfcPath: String?, config: Map?) {
        this.engine = engine
        cfmlEngine = CFMLEngineFactory.getInstance()
        caster = cfmlEngine.getCastUtil()
        creator = cfmlEngine.getCreationUtil()
        this.cfcPath = cfcPath
        this.id = id

        // config
        val oPort: Object = config.get("port")
        port = caster.toIntValue(oPort, 1225)
        val oWM: Object = config.get("welcomeMessage")
        val strWM: String = caster.toString(oWM, "").trim()
        if (strWM.length() > 0) welcomeMessage = strWM
    }

    @Override
    fun doStart() {
        state = STARTING
        try {
            createServerSocket()
            state = RUNNING
            do {
                try {
                    val sst: SocketServerThread = SocketServerThread(serverSocket.accept())
                    sst.start()
                    sockets.add(sst)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    error("Failed to listen on Socket [" + id + "] on port [" + port + "]: " + t.getMessage())
                }
            } while (getState() == RUNNING || getState() == STARTING)
            close(serverSocket)
            serverSocket = null
        } catch (e: Exception) {
            state = FAILED
            error("Error in Socet Gateway [" + id + "]: " + e.getMessage())
            LogUtil.log(ThreadLocalPageContext.get(), SocketGateway::class.java.getName(), e)
        }
    }

    @Override
    fun doStop() {
        state = STOPPING
        try {

            // close all open connections
            val it = sockets!!.iterator()
            while (it.hasNext()) {
                close(it.next()!!.socket)
            }

            // close server socket
            close(serverSocket)
            serverSocket = null
            state = STOPPED
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            state = FAILED
            error("Error in Socket Gateway [" + id + "]: " + t.getMessage())
            // throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
        }
    }

    @Throws(PageException::class, RuntimeException::class)
    private fun createServerSocket() {
        try {
            serverSocket = ServerSocket(port)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            error("Failed to start Socket Gateway [" + id + "] on port [" + port + "] " + t.getMessage())
            throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(t)
        }
    }

    private fun invokeListener(line: String?, originatorID: String?) {
        val data: Struct = creator.createStruct()
        data.setEL(creator.createKey("message"), line)
        val event: Struct = creator.createStruct()
        event.setEL(creator.createKey("data"), data)
        event.setEL(creator.createKey("originatorID"), originatorID)
        event.setEL(creator.createKey("cfcMethod"), "onIncomingMessage")
        event.setEL(creator.createKey("cfcTimeout"), Double.valueOf(10))
        event.setEL(creator.createKey("cfcPath"), cfcPath)
        event.setEL(creator.createKey("gatewayType"), "Socket")
        event.setEL(creator.createKey("gatewayId"), id)
        if (engine.invokeListener(this, "onIncomingMessage", event)) info("Socket Gateway Listener [$id] invoked.") else error("Failed to call Socket Gateway Listener [$id]")
    }

    private inner class SocketServerThread(socket: Socket?) : ParentThreasRefThread() {
        val socket: Socket?
        private var out: PrintWriter?
        val _id: String?
        @Override
        fun run() {
            var `in`: BufferedReader? = null
            try {
                `in` = BufferedReader(InputStreamReader(socket.getInputStream()))
                out.println(welcomeMessage)
                out.print("> ")
                var line: String?
                while (`in`.readLine().also { line = it } != null) {
                    if (line.trim().equals("exit")) break
                    invokeListener(line, _id)
                }
                // socketRegistry.remove(this.getName());
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                addParentStacktrace(t)
                error("Failed to read from Socket Gateway [" + id + "]: " + t.getMessage())
            } finally {
                close(out)
                out = null
                close(`in`)
                close(socket)
                sockets.remove(this)
            }
        }

        fun writeOutput(str: String?) {
            out.println(str)
            out.print("> ")
        }

        init {
            this.socket = socket
            out = PrintWriter(socket.getOutputStream(), true)
            _id = String.valueOf(hashCode())
        }
    }

    @Override
    fun sendMessage(_data: Map?): String? {
        val data: Struct = caster.toStruct(_data, null, false)
        val msg = data.get("message", null) as String
        val originatorID = data.get("originatorID", null) as String
        var status = "OK"
        if (msg != null) {
            var it = sockets!!.iterator()
            var sst: SocketServerThread?
            try {
                var hasSend = false
                while (it.hasNext()) {
                    sst = it.next()
                    if (originatorID != null && !sst!!._id.equalsIgnoreCase(originatorID)) continue
                    sst!!.writeOutput(msg)
                    hasSend = true
                }
                if (!hasSend) {
                    if (sockets.size() === 0) {
                        error("There is no connection")
                        status = "EXCEPTION"
                    } else {
                        it = sockets.iterator()
                        val sb = StringBuilder()
                        while (it.hasNext()) {
                            if (sb.length() > 0) sb.append(", ")
                            sb.append(it.next()!!._id)
                        }
                        error("There is no connection with originatorID [$originatorID], available originatorIDs are [$sb]")
                        status = "EXCEPTION"
                    }
                }
            } catch (e: Exception) {
                LogUtil.log(ThreadLocalPageContext.get(), SocketGateway::class.java.getName(), e)
                error("Failed to send message with exception: " + e.toString())
                status = "EXCEPTION"
            }
        }
        return status
    }

    @Override
    fun doRestart() {
        doStop()
        doStart()
    }

    @Override
    fun getId(): String? {
        return id
    }

    @Override
    fun getState(): Int {
        return state
    }

    @Override
    fun getHelper(): Object? {
        return null
    }

    fun info(msg: String?) {
        engine.log(this, GatewayEngine.LOGLEVEL_INFO, msg)
    }

    fun error(msg: String?) {
        engine.log(this, GatewayEngine.LOGLEVEL_ERROR, msg)
    }

    private fun close(writer: Writer?) {
        if (writer == null) return
        try {
            writer.close()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun close(reader: Reader?) {
        if (reader == null) return
        try {
            reader.close()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun close(socket: Socket?) {
        if (socket == null) return
        try {
            socket.close()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun close(socket: ServerSocket?) {
        if (socket == null) return
        try {
            socket.close()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Override
    override fun setThread(thread: Thread?) {
        this.thread = thread
    }

    @Override
    override fun getThread(): Thread? {
        return thread
    }
}