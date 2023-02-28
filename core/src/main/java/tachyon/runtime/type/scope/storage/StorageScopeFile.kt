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
package tachyon.runtime.type.scope.storage

import tachyon.commons.io.IOUtil

/**
 * client scope that store it's data in a resource
 */
abstract class StorageScopeFile : StorageScopeImpl {
    private var res: Resource?

    /**
     * Constructor of the class
     *
     * @param pc
     * @param name
     * @param sct
     */
    protected constructor(pc: PageContext?, res: Resource?, strType: String?, type: Int, sct: Struct?) : super(if (sct == null) StructImpl().also { sct = it } else sct, doNowIfNull(pc, Caster.toDate(sct.get(TIMECREATED, null), false, pc.getTimeZone(), null)),
            doNowIfNull(pc, Caster.toDate(sct.get(LASTVISIT, null), false, pc.getTimeZone(), null)), -1,
            if (type == SCOPE_CLIENT) Caster.toIntValue(sct.get(HITCOUNT, "1"), 1) else 0, strType, type) {
        this.res = res // pc.getConfig().getClientScopeDir().getRealResource(name+"-"+pc.getCFID()+".script");
    }

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    protected constructor(other: StorageScopeFile?, deepCopy: Boolean) : super(other, deepCopy) {
        res = other!!.res
    }

    @Override
    override fun touchBeforeRequest(pc: PageContext?) {
        setTimeSpan(pc)
        super.touchBeforeRequest(pc)
    }

    @Override
    override fun touchAfterRequest(pc: PageContext?) {
        setTimeSpan(pc)
        super.touchAfterRequest(pc)
        store(pc)
    }

    @Override
    override fun store(pc: PageContext?) {
        // if(!super.hasContent()) return;
        try {
            if (!res.exists()) ResourceUtil.createFileEL(res, true)
            IOUtil.write(res, (getTimeSpan() + System.currentTimeMillis()).toString() + ":" + serializer.serializeStruct(sct, ignoreSet), "UTF-8", false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Override
    override fun unstore(pc: PageContext?) {
        try {
            if (!res.exists()) return
            res.remove(true)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Override
    fun getStorageType(): String? {
        return STORAGE_TYPE
    }

    companion object {
        private const val serialVersionUID = -7519591903822909934L
        val STORAGE_TYPE: String? = "File"
        private val serializer: ScriptConverter? = ScriptConverter()
        protected var evaluator: CFMLExpressionInterpreter? = CFMLExpressionInterpreter(false)
        private fun doNowIfNull(pc: PageContext?, dt: DateTime?): DateTime? {
            return if (dt == null) DateTimeImpl(pc.getConfig()) else dt
        }

        protected fun _loadData(pc: PageContext?, res: Resource?, log: Log?): Struct? {
            if (res.exists()) {
                try {
                    var str: String? = IOUtil.toString(res, "UTF-8")
                    val index: Int = str.indexOf(':')
                    if (index != -1) {
                        val expires: Long = Caster.toLongValue(str.substring(0, index), -1L)
                        // check is for backward compatibility, old files have no expires date inside. they do ot expire
                        if (expires != -1L) {
                            str = str.substring(index + 1)
                            /*
						 * if(checkExpires && expires<System.currentTimeMillis()){ print.o("expired("+new
						 * Date(expires)+"):"+res); return null; } else { str=str.substring(index+1);
						 * print.o("not expired("+new Date(expires)+"):"+res); print.o(str); }
						 */
                        }
                    }
                    val s: Struct = evaluator.interpret(pc, str) as Struct
                    ScopeContext.debug(log, "load existing file storage [$res]")
                    return s
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    ScopeContext.error(log, t)
                }
            }
            ScopeContext.debug(log, "create new file storage [$res]")
            return null
        }

        protected fun _loadResource(config: ConfigWeb?, type: Int, name: String?, cfid: String?): Resource? {
            val ci: ConfigPro? = config as ConfigPro?
            val dir: Resource = if (type == SCOPE_CLIENT) ci.getClientScopeDir() else ci.getSessionScopeDir()
            return dir.getRealResource(getFolderName(name, cfid, true))
        }

        /**
         * return a folder name that match given input
         *
         * @param name
         * @param cfid
         * @param addExtension
         * @return
         */
        fun getFolderName(name: String?, cfid: String?, addExtension: Boolean): String? {
            var name = name
            if (addExtension) return getFolderName(name, cfid, false).toString() + ".scpt"
            if (!StringUtil.isEmpty(name)) name = encode(name) // StringUtil.toVariableName(StringUtil.toLowerCase(name));
            else name = "__empty__"
            return name.toString() + "/" + cfid.substring(0, 2) + "/" + cfid.substring(2)
        }
    }
}