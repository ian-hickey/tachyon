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

import java.util.Map

class CFCGateway(private val cfcPath: String?) : GatewaySupport {
    // private static final Object OBJ = new Object();
    // private Component _cfc;
    private var id: String? = null
    private var state: Int = Gateway.STOPPED

    // private Config config;
    // private String requestURI;
    // private Resource cfcDirectory;
    private var engine: GatewayEngineImpl? = null
    private var thread: Thread? = null
    @Override
    @Throws(GatewayException::class)
    fun init(engine: GatewayEngine?, id: String?, cfcPath: String?, config: Map?) {
        this.engine = engine
        this.id = id

        // requestURI=engine.toRequestURI(cfcPath);
        val args: Struct = StructImpl(Struct.TYPE_LINKED)
        args.setEL(KeyConstants._id, id)
        args.setEL(KeyConstants._config, Caster.toStruct(config, null, false))
        if (!StringUtil.isEmpty(cfcPath)) {
            try {
                args.setEL(KeyConstants._listener, this.engine!!.getComponent(cfcPath, id))
            } catch (e: PageException) {
                engine.log(this, GatewayEngine.LOGLEVEL_ERROR, e.getMessage())
            }
        }
        try {
            callOneWay("init", args)
        } catch (pe: PageException) {
            engine.log(this, GatewayEngine.LOGLEVEL_ERROR, pe.getMessage())
            // throw new PageGatewayException(pe);
        }
    }

    @Override
    @Throws(GatewayException::class)
    fun doRestart() {
        engine!!.log(this, GatewayEngine.LOGLEVEL_INFO, "restart")
        val args: Struct = StructImpl()
        try {
            val has = callOneWay("restart", args)
            if (!has) {
                if (callOneWay("stop", args)) {
                    // engine.clear(cfcPath,id);
                    callOneWay("start", args)
                }
            }
        } catch (pe: PageException) {
            throw PageGatewayException(pe)
        }
    }

    @Override
    @Throws(GatewayException::class)
    fun doStart() {
        engine!!.log(this, GatewayEngine.LOGLEVEL_INFO, "start")
        val args: Struct = StructImpl()
        state = STARTING
        try {
            callOneWay("start", args)
            engine!!.log(this, GatewayEngine.LOGLEVEL_INFO, "running")
            state = RUNNING
        } catch (pe: PageException) {
            state = FAILED
            throw PageGatewayException(pe)
        }
    }

    @Override
    @Throws(GatewayException::class)
    fun doStop() {
        engine!!.log(this, GatewayEngine.LOGLEVEL_INFO, "stop")
        val args: Struct = StructImpl()
        state = STOPPING
        try {
            callOneWay("stop", args)
            // engine.clear(cfcPath,id);
            state = STOPPED
        } catch (pe: PageException) {
            state = FAILED
            throw PageGatewayException(pe)
        }
    }

    @Override
    fun getHelper(): Object? {
        val args: Struct = StructImpl(Struct.TYPE_LINKED)
        return callEL("getHelper", args, null)
    }

    @Override
    fun getId(): String? {
        return id
    }

    @Override
    fun getState(): Int {
        val args: Struct = StructImpl()
        val state: Integer = Integer.valueOf(state)
        try {
            return GatewayEngineImpl.toIntState(Caster.toString(call("getState", args, state)), this.state)
        } catch (pe: PageException) {
            engine!!.log(this, GatewayEngine.LOGLEVEL_ERROR, pe.getMessage())
        }
        return this.state
    }

    @Override
    @Throws(GatewayException::class)
    fun sendMessage(data: Map?): String? {
        val args: Struct = StructImpl(Struct.TYPE_LINKED)
        args.setEL("data", Caster.toStruct(data, null, false))
        return try {
            Caster.toString(call("sendMessage", args, ""))
        } catch (pe: PageException) {
            throw PageGatewayException(pe)
        }
    }

    private fun callEL(methodName: String?, arguments: Struct?, defaultValue: Object?): Object? {
        return engine!!.callEL(cfcPath, id, methodName, arguments, true, defaultValue)
    }

    @Throws(PageException::class)
    private fun callOneWay(methodName: String?, arguments: Struct?): Boolean {
        return engine!!.callOneWay(cfcPath, id, methodName, arguments, true)
    }

    @Throws(PageException::class)
    private fun call(methodName: String?, arguments: Struct?, defaultValue: Object?): Object? {
        return engine!!.call(cfcPath, id, methodName, arguments, true, defaultValue)
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