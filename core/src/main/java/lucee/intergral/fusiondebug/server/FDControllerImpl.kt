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
package lucee.intergral.fusiondebug.server

import java.util.ArrayList

/**
 *
 */
class FDControllerImpl(engine: CFMLEngineImpl?, serial: String?) : IFDController {
    private var exceptionTypes: List? = null
    private val engine: CFMLEngineImpl?
    private val isEnterprise: Boolean
    @Override
    fun getEngineName(): String? {
        return Constants.NAME
    }

    @Override
    fun getEngineVersion(): String? {
        return engine.getInfo().getVersion().toString()
    }

    @Override
    fun getExceptionTypes(): List? {
        if (exceptionTypes == null) {
            exceptionTypes = ArrayList()
            exceptionTypes.add("application")
            exceptionTypes.add("expression")
            exceptionTypes.add("database")
            exceptionTypes.add("custom_type")
            exceptionTypes.add("lock")
            exceptionTypes.add("missinginclude")
            exceptionTypes.add("native")
            exceptionTypes.add("security")
            exceptionTypes.add("template")
        }
        return exceptionTypes
    }

    @Deprecated
    @Deprecated("use instead <code>{@link #getLicenseInformation(String)}</code>")
    fun getLicenseInformation(): String? {
        throw RuntimeException("please replace your fusiondebug-api-server-1.0.xxx-SNAPSHOT.jar with a newer version")
    }

    @Override
    fun getLicenseInformation(key: String?): String? {
        if (!isEnterprise) {
            LogUtil.log(Log.LEVEL_ERROR, "integral", "FD Server Licensing does not work with the Open Source Version of Lucee or Enterprise Version of Lucee that is not enabled")
            return null
        }
        return FDLicense.getLicenseInformation(key)
    }

    @Override
    fun output(message: String?) {
        LogUtil.log(Log.LEVEL_INFO, "integral", message)
    }

    @Override
    fun pause(): List? {
        val threads: List<IFDThread?> = ArrayList<IFDThread?>()
        val it: Iterator<Entry<String?, CFMLFactory?>?> = engine.getCFMLFactories().entrySet().iterator()
        var entry: Entry<String?, CFMLFactory?>?
        while (it.hasNext()) {
            entry = it.next()
            pause(entry.getKey(), entry.getValue() as CFMLFactoryImpl, threads)
        }
        return threads
    }

    private fun pause(name: String?, factory: CFMLFactoryImpl?, threads: List<IFDThread?>?) {
        val pcs: Map<Integer?, PageContextImpl?> = factory.getActivePageContexts()
        val it: Iterator<PageContextImpl?> = pcs.values().iterator()
        var pc: PageContextImpl?
        while (it.hasNext()) {
            pc = it.next()
            try {
                pc.getThread().wait()
            } catch (e: InterruptedException) {
                LogUtil.log("integral", e)
            }
            threads.add(FDThreadImpl(this, factory, name, pc))
        }
    }

    @Override
    fun getCaughtStatus(exceptionType: String?, executionUnitName: String?, executionUnitPackage: String?, sourceFilePath: String?, sourceFileName: String?, lineNumber: Int): Boolean {
        // TODO [007]
        return true
    }

    @Override
    fun getByNativeIdentifier(id: String?): IFDThread? {
        val it: Iterator<Entry<String?, CFMLFactory?>?> = engine.getCFMLFactories().entrySet().iterator()
        var entry: Entry<String?, CFMLFactory?>?
        var thread: FDThreadImpl?
        while (it.hasNext()) {
            entry = it.next()
            thread = getByNativeIdentifier(entry.getKey(), entry.getValue() as CFMLFactoryImpl, id)
            if (thread != null) return thread
        }
        return null
    }

    /**
     * checks a single CFMLFactory for the thread
     *
     * @param name
     * @param factory
     * @param id
     * @return matching thread or null
     */
    private fun getByNativeIdentifier(name: String?, factory: CFMLFactoryImpl?, id: String?): FDThreadImpl? {
        val pcs: Map<Integer?, PageContextImpl?> = factory.getActivePageContexts()
        val it: Iterator<PageContextImpl?> = pcs.values().iterator()
        var pc: PageContextImpl?
        while (it.hasNext()) {
            pc = it.next()
            if (equals(pc, id)) return FDThreadImpl(this, factory, name, pc)
        }
        return null
    }

    /**
     * check if thread of PageContext match given id
     *
     * @param pc
     * @param id
     * @return match the id the pagecontext
     */
    private fun equals(pc: PageContextImpl?, id: String?): Boolean {
        val thread: Thread = pc.getThread()
        if (Caster.toString(FDThreadImpl.id(pc)).equals(id)) return true
        if (Caster.toString(thread.getId()).equals(id)) return true
        return if (Caster.toString(thread.hashCode()).equals(id)) true else false
    }

    @Override
    fun getCompletionMethod(): String? {
        return "serviceCFML"
    }

    @Override
    fun getCompletionType(): String? {
        return CFMLEngineImpl::class.java.getName()
    }

    @Override
    fun release() {
        engine.allowRequestTimeout(true)
    }

    init {
        isEnterprise = SerialNumber.isEnterprise(serial)
        this.engine = engine
    }
}