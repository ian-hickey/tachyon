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
package tachyon.runtime.gateway

import java.io.IOException

class GatewayEngineImpl(config: ConfigWeb?) : GatewayEngine {
    private val entries: Map<String?, GatewayEntry?>? = HashMap<String?, GatewayEntry?>()
    private val config: ConfigWeb?
    private val log: Log?
    @Throws(ClassException::class, PageException::class, IOException::class, BundleException::class)
    fun addEntries(config: Config?, entries: Map<String?, GatewayEntry?>?) {
        val it: Iterator<Entry<String?, GatewayEntry?>?> = entries.entrySet().iterator()
        while (it.hasNext()) {
            addEntry(config, it.next().getValue())
        }
    }

    @Throws(ClassException::class, PageException::class, IOException::class, BundleException::class)
    fun addEntry(config: Config?, ge: GatewayEntry?) {
        val id: String = ge!!.getId().toLowerCase().trim()
        val existing: GatewayEntry? = entries!![id]
        var g: Gateway? = null

        // does not exist
        if (existing == null) {
            entries.put(id, load(config, ge))
        } else if (!existing.equals(ge)) {
            g = existing.getGateway()
            if (g.getState() === Gateway.RUNNING) g.doStop()
            entries.put(id, load(config, ge))
        }
        // not changed
        // else print.out("untouched:"+id);
    }

    @Throws(ClassException::class, PageException::class, BundleException::class)
    private fun load(config: Config?, ge: GatewayEntry?): GatewayEntry? {
        ge!!.createGateway(config)
        return ge
    }

    /**
     * @return the entries
     */
    fun getEntries(): Map<String?, GatewayEntry?>? {
        return entries
    }

    fun remove(ge: GatewayEntry?) {
        val id: String = ge!!.getId().toLowerCase().trim()
        val existing: GatewayEntry = entries.remove(id)
        var g: Gateway? = null

        // does not exist
        if (existing != null) {
            g = existing.getGateway()
            try {
                if (g.getState() === Gateway.RUNNING) g.doStop()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    /**
     * get the state of gateway
     *
     * @param gatewayId
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getState(gatewayId: String?): Int {
        return getGateway(gatewayId).getState()
    }

    /**
     * get helper object
     *
     * @param gatewayId
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getHelper(gatewayId: String?): Object? {
        return getGateway(gatewayId).getHelper()
    }

    /**
     * send the message to the gateway
     *
     * @param gatewayId
     * @param data
     * @return
     * @throws PageException
     */
    @Throws(PageException::class, IOException::class)
    fun sendMessage(gatewayId: String?, data: Struct?): String? {
        val g: Gateway? = getGateway(gatewayId)
        if (g.getState() !== Gateway.RUNNING) throw GatewayException("Gateway [$gatewayId] is not running")
        return g.sendMessage(data)
    }

    /**
     * start the gateway
     *
     * @param gatewayId
     * @throws PageException
     */
    @Throws(PageException::class)
    fun start(gatewayId: String?) {
        executeThread(gatewayId, GatewayThread.START)
    }

    private fun start(gateway: Gateway?) {
        executeThread(gateway, GatewayThread.START)
    }

    fun autoStart() {
        var g: Gateway
        for (ge in entries!!.values()) {
            if (ge!!.getStartupMode() !== GatewayEntry.STARTUP_MODE_AUTOMATIC) continue
            g = ge!!.getGateway()
            if (g.getState() !== Gateway.RUNNING && g.getState() !== Gateway.STARTING) {
                start(g)
            }
        }
    }

    /**
     * stop the gateway
     *
     * @param gatewayId
     * @throws PageException
     */
    @Throws(PageException::class)
    fun stop(gatewayId: String?) {
        executeThread(gatewayId, GatewayThread.STOP)
    }

    private fun stop(gateway: Gateway?) {
        executeThread(gateway, GatewayThread.STOP)
    }

    /**
     * stop all entries
     */
    fun stopAll() {
        val it: Iterator<GatewayEntry?> = getEntries()!!.values().iterator()
        var g: Gateway
        while (it.hasNext()) {
            g = it.next()!!.getGateway()
            if (g != null) stop(g)
        }
    }

    fun reset(start: Boolean) {
        val it: Iterator<Entry<String?, GatewayEntry?>?> = entries.entrySet().iterator()
        var entry: Entry<String?, GatewayEntry?>?
        var ge: GatewayEntry
        var g: Gateway
        while (it.hasNext()) {
            entry = it.next()
            ge = entry.getValue()
            g = ge.getGateway()
            if (g.getState() === Gateway.RUNNING) {
                try {
                    g.doStop()
                    if (g is GatewaySupport) {
                        val t: Thread = (g as GatewaySupport)!!.getThread()
                        t.interrupt()
                        SystemUtil.stop(t)
                    }
                } catch (e: IOException) {
                    log(g.getId(), LOGLEVEL_ERROR, e.getMessage(), e)
                }
            }
            if (start && ge.getStartupMode() === GatewayEntry.STARTUP_MODE_AUTOMATIC) start(g)
        }
    }

    fun clear() {
        synchronized(entries) {
            val it: Iterator<Entry<String?, GatewayEntry?>?> = entries.entrySet().iterator()
            var entry: Entry<String?, GatewayEntry?>?
            while (it.hasNext()) {
                entry = it.next()
                if (entry.getValue().getGateway().getState() === Gateway.RUNNING) stop(entry.getValue().getGateway())
            }
            entries.clear()
        }
    }

    /**
     * restart the gateway
     *
     * @param gatewayId
     * @throws PageException
     */
    @Throws(PageException::class)
    fun restart(gatewayId: String?) {
        executeThread(gatewayId, GatewayThread.RESTART)
    }

    @Throws(PageException::class)
    private fun getGateway(gatewayId: String?): Gateway? {
        return getGatewayEntry(gatewayId).getGateway()
    }

    @Throws(PageException::class)
    private fun getGatewayEntry(gatewayId: String?): GatewayEntry? {
        val id: String = gatewayId.toLowerCase().trim()
        val ge: GatewayEntry? = entries!![id]
        if (ge != null) return ge

        // create list
        val it: Iterator<String?> = entries.keySet().iterator()
        val sb = StringBuilder()
        while (it.hasNext()) {
            if (sb.length() > 0) sb.append(", ")
            sb.append(it.next())
        }
        throw ExpressionException("there is no gateway instance with id [$gatewayId], available gateway instances are [$sb]")
    }

    private fun getGatewayEntry(gateway: Gateway?): GatewayEntry? {
        val gatewayId: String = gateway.getId()
        // it must exist, because it only can come from here
        return entries!![gatewayId]
    }

    @Throws(PageException::class)
    private fun executeThread(gatewayId: String?, action: Int) {
        GatewayThread(this, getGateway(gatewayId), action).start()
    }

    private fun executeThread(g: Gateway?, action: Int) {
        GatewayThread(this, g, action).start()
    }

    @Override
    fun invokeListener(gateway: Gateway?, method: String?, data: Map?): Boolean { // FUTUTE add generic type to interface
        return invokeListener(gateway.getId(), method, data)
    }

    fun invokeListener(gatewayId: String?, method: String?, data: Map?): Boolean { // do not add this method to loade, it can be removed with Tachyon 5
        var data: Map? = data
        data = GatewayUtil.toCFML(data)
        val entry: GatewayEntry?
        try {
            entry = getGatewayEntry(gatewayId)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
        val cfcPath: String = entry!!.getListenerCfcPath()
        if (!StringUtil.isEmpty(cfcPath, true)) {
            try {
                if (!callOneWay(cfcPath, gatewayId, method, Caster.toStruct(data, null, false), false)) log(gatewayId, LOGLEVEL_ERROR, "function [$method] does not exist in cfc [$cfcPath]") else return true
            } catch (e: PageException) {
                log(gatewayId, LOGLEVEL_ERROR, e.getMessage(), e)
            }
        } else log(gatewayId, LOGLEVEL_ERROR, "there is no listener cfc defined")
        return false
    }

    fun callEL(cfcPath: String?, id: String?, functionName: String?, arguments: Struct?, cfcPeristent: Boolean, defaultValue: Object?): Object? {
        return try {
            call(cfcPath, id, functionName, arguments, cfcPeristent, defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun callOneWay(cfcPath: String?, id: String?, functionName: String?, arguments: Struct?, cfcPeristent: Boolean): Boolean {
        return call(cfcPath, id, functionName, arguments, cfcPeristent, OBJ) !== OBJ
    }

    @Throws(PageException::class)
    fun getComponent(cfcPath: String?, id: String?): Object? {
        val requestURI = toRequestURI(cfcPath)
        val oldPC: PageContext = ThreadLocalPageContext.get()
        var pc: PageContextImpl? = null
        return try {
            pc = createPageContext(requestURI, id, "init", null, false, true)
            // ThreadLocalPageContext.register(pc);
            getCFC(pc, requestURI)
        } finally {
            val f: CFMLFactory = config.getFactory()
            f.releaseTachyonPageContext(pc, true)
            ThreadLocalPageContext.register(oldPC)
        }
    }

    @Throws(PageException::class)
    fun call(cfcPath: String?, id: String?, functionName: String?, arguments: Struct?, cfcPeristent: Boolean, defaultValue: Object?): Object? {
        val requestURI = toRequestURI(cfcPath)
        val oldPC: PageContext = ThreadLocalPageContext.get()
        var pc: PageContextImpl? = null
        try {
            pc = createPageContext(requestURI, id, functionName, arguments, cfcPeristent, true)
            val ext: String = ResourceUtil.getExtension(cfcPath, null)
            val config: ConfigWeb = ThreadLocalPageContext.getConfig() as ConfigWeb
            val dialect: Int = if (ext == null) CFMLEngine.DIALECT_CFML else config.getFactory().toDialect(ext)
            // ThreadLocalPageContext.register(pc);
            val cfc: Component? = getCFC(pc, requestURI)
            if (cfc.containsKey(functionName)) {
                if (dialect == CFMLEngine.DIALECT_LUCEE) pc.execute(requestURI, true, false) else pc.executeCFML(requestURI, true, false)

                // Result
                return pc.variablesScope().get(AMF_FORWARD, null)
            }
        } finally {
            val f: CFMLFactory = config.getFactory()
            f.releaseTachyonPageContext(pc, true)
            ThreadLocalPageContext.register(oldPC)
        }
        return defaultValue
    }

    @Throws(PageException::class)
    private fun getCFC(pc: PageContextImpl?, requestURI: String?): Component? {
        val req: HttpServletRequest = pc.getHttpServletRequest()
        return try {
            val ext: String = ResourceUtil.getExtension(requestURI, "")
            val config: ConfigWeb = ThreadLocalPageContext.getConfig(pc) as ConfigWeb
            val dialect: Int = config.getFactory().toDialect(ext)
            req.setAttribute("client", "tachyon-gateway-1-0")
            req.setAttribute("call-type", "store-only")
            if (dialect == CFMLEngine.DIALECT_LUCEE) pc.execute(requestURI, true, false) else pc.executeCFML(requestURI, true, false)
            req.getAttribute("component") as Component
        } finally {
            req.removeAttribute("call-type")
            req.removeAttribute("component")
        }
    }

    @Throws(PageException::class)
    private fun createPageContext(requestURI: String?, id: String?, functionName: String?, arguments: Struct?, cfcPeristent: Boolean, register: Boolean): PageContextImpl? {
        val attrs: Struct = StructImpl()
        val remotePersisId: String
        remotePersisId = try {
            Md5.getDigestAsString(requestURI + id)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        val pc: PageContextImpl = ThreadUtil.createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", requestURI,
                "method=" + functionName + if (cfcPeristent) "&" + ComponentPageImpl.REMOTE_PERSISTENT_ID.toString() + "=" + remotePersisId else "", null, arrayOf<Pair?>(Pair<String?, Object?>("AMF-Forward", "true")), null, null, attrs, register, Long.MAX_VALUE)
        pc.setGatewayContext(true)
        if (arguments != null) attrs.setEL(KeyConstants._argumentCollection, arguments)
        attrs.setEL("client", "tachyon-gateway-1-0")
        return pc
    }

    private fun toRequestURI(cfcPath: String?): String? {
        // MUSTMUST support also Tachyon extension!
        var requestURI: String = cfcPath.replace('.', '/')
        if (!requestURI.startsWith("/")) requestURI = "/" + requestURI + "." + Constants.GATEWAY_COMPONENT_EXTENSION
        return requestURI
    }

    @Override
    fun log(gateway: Gateway?, level: Int, message: String?) {
        log(gateway.getId(), level, message)
    }

    fun log(gatewayId: String?, level: Int, message: String?) {
        log(gatewayId, level, message, null)
    }

    fun log(gatewayId: String?, level: Int, message: String?, e: Exception?) {
        var l = level
        when (level) {
            LOGLEVEL_INFO -> l = Log.LEVEL_INFO
            LOGLEVEL_DEBUG -> l = Log.LEVEL_DEBUG
            LOGLEVEL_ERROR -> l = Log.LEVEL_ERROR
            LOGLEVEL_FATAL -> l = Log.LEVEL_FATAL
            LOGLEVEL_WARN -> l = Log.LEVEL_WARN
            LOGLEVEL_TRACE -> l = Log.LEVEL_TRACE
        }
        if (e == null) log.log(l, "Gateway:$gatewayId", message) else log.log(l, "Gateway:$gatewayId", message, e)
    }

    private var persistentRemoteCFC: Map<String?, Component?>? = null
    fun getPersistentRemoteCFC(id: String?): Component? {
        if (persistentRemoteCFC == null) persistentRemoteCFC = HashMap<String?, Component?>()
        return persistentRemoteCFC!![id]
    }

    fun setPersistentRemoteCFC(id: String?, cfc: Component?): Component? {
        if (persistentRemoteCFC == null) persistentRemoteCFC = HashMap<String?, Component?>()
        return persistentRemoteCFC.put(id, cfc)
    }

    companion object {
        private val OBJ: Object? = Object()
        private val AMF_FORWARD: Collection.Key? = KeyImpl.getInstance("AMF-Forward")
        fun toIntState(state: String?, defaultValue: Int): Int {
            var state = state
            state = state.trim().toLowerCase()
            if ("running".equals(state)) return Gateway.RUNNING
            if ("started".equals(state)) return Gateway.RUNNING
            if ("run".equals(state)) return Gateway.RUNNING
            if ("failed".equals(state)) return Gateway.FAILED
            if ("starting".equals(state)) return Gateway.STARTING
            if ("stopped".equals(state)) return Gateway.STOPPED
            return if ("stopping".equals(state)) Gateway.STOPPING else defaultValue
        }

        fun toStringState(state: Int, defaultValue: String?): String? {
            if (Gateway.RUNNING === state) return "running"
            if (Gateway.FAILED === state) return "failed"
            if (Gateway.STOPPED === state) return "stopped"
            if (Gateway.STOPPING === state) return "stopping"
            return if (Gateway.STARTING === state) "starting" else defaultValue
        }
    }

    init {
        this.config = config
        log = ThreadLocalPageContext.getLog(config, "gateway")
    }
}