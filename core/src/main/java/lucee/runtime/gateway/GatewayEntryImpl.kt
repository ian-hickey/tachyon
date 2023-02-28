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

class GatewayEntryImpl private constructor(engine: GatewayEngine?, id: String?, cd: ClassDefinition?, cfcPath: String?, listenerCfcPath: String?, startupMode: Int, custom: Struct?, readOnly: Boolean) : GatewayEntry {
    private val id: String?
    private val custom: Struct?
    private val readOnly: Boolean
    private val listenerCfcPath: String?
    private val startupMode: Int
    private val cfcPath: String?
    private val classDefintion: ClassDefinition?
    private val engine: GatewayEngine?
    private var gateway: Gateway? = null

    constructor(engine: GatewayEngine?, id: String?, cd: ClassDefinition?, cfcPath: String?, listenerCfcPath: String?, startupMode: String?, custom: Struct?, readOnly: Boolean) : this(engine, id, cd, cfcPath, listenerCfcPath, toStartupMode(startupMode), custom, readOnly) {}

    /**
     * @return the gateway
     * @throws ClassException
     * @throws PageException
     * @throws BundleException
     */
    @Override
    @Throws(ClassException::class, PageException::class, BundleException::class)
    override fun createGateway(config: Config?) {
        // TODO config is ignored here???
        if (gateway == null) {
            if (classDefintion != null && classDefintion.hasClass()) {
                val clazz: Class = classDefintion.getClazz()
                gateway = GatewayFactory.toGateway(ClassUtil.loadInstance(clazz))
            } else if (!StringUtil.isEmpty(cfcPath)) {
                gateway = CFCGateway(cfcPath)
            } else throw ApplicationException("missing gateway source definitions")
            try {
                // new GatewayThread(engine,gateway,GatewayThread.START).run();
                gateway.init(engine, getId(), getListenerCfcPath(), getCustom())
                if (getStartupMode() == GatewayEntry.STARTUP_MODE_AUTOMATIC) {
                    // new GatewayThread(engine, gateway, GatewayThread.START).start();
                    /*
					 * try{ //gateway.doStart(); } catch(GatewayException ge){
					 * engine.log(gateway,GatewayEngine.LOGLEVEL_ERROR, ge.getMessage()); }
					 */
                }
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
    }

    @Override
    override fun getGateway(): Gateway? {
        return gateway
    }

    @Override
    override fun getId(): String? {
        return id
    }

    @Override
    override fun getCustom(): Struct? {
        return Duplicator.duplicate(custom, true) as Struct
    }

    @Override
    override fun isReadOnly(): Boolean {
        return readOnly
    }

    /**
     * @return the cfcPath
     */
    @Override
    override fun getListenerCfcPath(): String? {
        return listenerCfcPath
    }

    @Override
    override fun getCfcPath(): String? {
        return cfcPath
    }

    /**
     * @return the className
     */
    @Override
    override fun getClassDefinition(): ClassDefinition? {
        return classDefintion
    }

    /**
     * @return the startupMode
     */
    @Override
    override fun getStartupMode(): Int {
        return startupMode
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj === this) return true
        if (obj !is GatewayEntryImpl) return false
        val other = obj as GatewayEntryImpl?
        if (!other!!.getId()!!.equals(id)) return false
        if (!equal(other.classDefintion.toString(), classDefintion.toString())) return false
        if (!equal(other.cfcPath, cfcPath)) return false
        if (!equal(other.listenerCfcPath, listenerCfcPath)) return false
        if (other.getStartupMode() != startupMode) return false
        val otherCustom: Struct? = other.getCustom()
        if (otherCustom.size() !== custom.size()) return false

        // Key[] keys = otherCustom.keys();
        val it: Iterator<Entry<Key?, Object?>?> = otherCustom.entryIterator()
        var e: Entry<Key?, Object?>?
        var ot: Object
        var oc: Object
        while (it.hasNext()) {
            e = it.next()
            ot = custom.get(e.getKey(), null)
            oc = e.getValue()
            if (ot == null) return false
            if (!ot.equals(oc)) return false
        }
        return true
    }

    fun duplicateReadOnly(engine: GatewayEngine?): GatewayEntry? {
        return GatewayEntryImpl(engine, id, classDefintion, cfcPath, listenerCfcPath, startupMode, custom, true)
    }

    companion object {
        private fun toStartupMode(startupMode: String?): Int {
            var startupMode = startupMode
            startupMode = startupMode.trim().toLowerCase()
            return if ("manual".equals(startupMode)) STARTUP_MODE_MANUAL else if ("disabled".equals(startupMode)) STARTUP_MODE_DISABLED else STARTUP_MODE_AUTOMATIC
        }

        fun toStartup(mode: Int, defautValue: String?): String? {
            if (mode == STARTUP_MODE_MANUAL) return "manual" else if (mode == STARTUP_MODE_DISABLED) return "disabled" else if (mode == STARTUP_MODE_AUTOMATIC) return "automatic"
            return defautValue
        }

        fun toStartup(strMode: String?, defaultValue: Int): Int {
            var strMode = strMode
            strMode = strMode.trim().toLowerCase()
            if ("manual".equals(strMode)) return STARTUP_MODE_MANUAL else if ("disabled".equals(strMode)) return STARTUP_MODE_DISABLED else if ("automatic".equals(strMode)) return STARTUP_MODE_AUTOMATIC
            return defaultValue
        }

        private fun equal(left: String?, right: String?): Boolean {
            if (left == null && right == null) return true
            return if (left != null && right != null) left.equals(right) else false
        }
    }

    init {
        this.engine = engine
        this.id = id
        this.listenerCfcPath = listenerCfcPath
        classDefintion = cd
        this.custom = custom
        this.readOnly = readOnly
        this.cfcPath = cfcPath
        this.startupMode = startupMode
    }
}