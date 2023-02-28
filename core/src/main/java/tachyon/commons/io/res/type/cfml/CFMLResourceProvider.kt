/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.commons.io.res.type.cfml

import java.io.File

class CFMLResourceProvider : ResourceProviderPro {
    val lockTimeout = 20000
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, false)

    @get:Override
    var scheme: String? = null
        private set
    private var args: Map? = null

    // private ResourceProvider provider;
    private var resources: Resources? = null
    private var componentPath: String? = null
    private var component: Component? = null
    var isUseStreams = false
        private set

    @Override
    fun init(scheme: String?, args: Map): ResourceProvider {
        this.scheme = scheme
        this.args = args

        // CFC Path
        componentPath = Caster.toString(args.get("cfc"), null)
        if (StringUtil.isEmpty(componentPath, true)) componentPath = Caster.toString(args.get("component"), null)
        if (StringUtil.isEmpty(componentPath, true)) componentPath = Caster.toString(args.get("class"), null)

        // use Streams for data
        var _useStreams: Boolean = Caster.toBoolean(args.get("use-streams"), null)
        if (_useStreams == null) _useStreams = Caster.toBoolean(args.get("usestreams"), null)
        if (_useStreams != null) isUseStreams = _useStreams.booleanValue()
        return this
    }

    @Override
    fun getResource(path: String): Resource? {
        var path = path
        path = ResourceUtil.removeScheme(scheme, path)
        path = ResourceUtil.prettifyPath(path)
        if (!StringUtil.startsWith(path, '/')) path = "/$path"
        return callResourceRTE(getPageContext(null), null, "getResource", arrayOf(path), false)
    }

    private fun getPageContext(pc: PageContext?): PageContext {
        ThreadLocalPageContext.get(pc)
        if (pc != null) return pc
        val c: Config = ThreadLocalPageContext.getConfig()
        return if (c is ConfigWeb) {
            ThreadUtil.createPageContext(c as ConfigWeb, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", "/", "", arrayOfNulls<Cookie>(0), arrayOfNulls<Pair>(0), null, arrayOfNulls<Pair>(0),
                    StructImpl(), false, -1)
        } else try {
            CFMLEngineFactory.getInstance().createPageContext(File("."), "localhost", "/", "", arrayOfNulls<Cookie>(0), null, null, null,
                    DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, -1, false)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @get:Override
    val arguments: Map?
        get() = args

    @Override
    fun setResources(resources: Resources?) {
        this.resources = resources
    }

    @get:Override
    val isCaseSensitive: Boolean
        get() = callbooleanRTE(null, null, "isCaseSensitive", ZERO_ARGS)

    @get:Override
    val isModeSupported: Boolean
        get() = callbooleanRTE(null, null, "isModeSupported", ZERO_ARGS)

    @get:Override
    val isAttributesSupported: Boolean
        get() = callbooleanRTE(null, null, "isAttributesSupported", ZERO_ARGS)

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        lock.lock(res)
    }

    @Override
    fun unlock(res: Resource?) {
        lock.unlock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        lock.read(res)
    }

    fun callResourceRTE(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?, allowNull: Boolean): Resource? {
        var pc: PageContext? = pc
        pc = getPageContext(pc)
        return try {
            val res: Object = call(pc, getCFC(pc, component), methodName, args)
            if (allowNull && res == null) null else CFMLResource(this, Caster.toComponent(res))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun callResourceArrayRTE(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Array<Resource?> {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        return try {
            val arr: Array = Caster.toArray(call(pc, getCFC(pc, component), methodName, args))
            val it: Iterator<Object> = arr.valueIterator()
            val resources: Array<CFMLResource?> = arrayOfNulls<CFMLResource>(arr.size())
            var index = 0
            while (it.hasNext()) {
                resources[index++] = CFMLResource(this, Caster.toComponent(it.next()))
            }
            resources
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun callintRTE(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Int {
        return try {
            callint(pc, component, methodName, args)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    fun callint(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Int {
        return Caster.toIntValue(call(pc, component, methodName, args))
    }

    fun calllongRTE(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Long {
        return try {
            calllong(pc, component, methodName, args)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    fun calllong(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Long {
        return Caster.toLongValue(call(pc, component, methodName, args))
    }

    fun callbooleanRTE(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Boolean {
        return try {
            callboolean(pc, component, methodName, args)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    fun callboolean(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Boolean {
        return Caster.toBooleanValue(call(pc, component, methodName, args))
    }

    fun callStringRTE(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): String {
        return try {
            Caster.toString(call(pc, component, methodName, args))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    fun callString(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): String {
        return Caster.toString(call(pc, component, methodName, args))
    }

    fun callRTE(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Object {
        return try {
            call(pc, component, methodName, args)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, component: Component?, methodName: String?, args: Array<Object?>?): Object {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        return getCFC(pc, component).call(pc, methodName, args)
    }

    @Throws(PageException::class)
    private fun getCFC(pc: PageContext?, component: Component?): Component? {
        if (component != null) return component
        if (this.component != null) return this.component
        if (StringUtil.isEmpty(componentPath, true)) throw ApplicationException("You need to define the argument [component] for the [CFMLResourceProvider]")
        componentPath = componentPath.trim()
        this.component = pc.loadComponent(componentPath)
        call(pc, this.component, "init", arrayOf(scheme, Caster.toStruct(args)))
        return this.component
    }

    // fallback to default "/"
    @get:Override
    val separator: Char
        get() {
            try {
                val str = callStringRTE(null, component, "getSeparator", ZERO_ARGS)
                if (StringUtil.length(str, true) === 1) return str.charAt(0)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                // fallback to default "/"
            }
            return '/'
        }

    companion object {
        private val ZERO_ARGS: Array<Object?> = arrayOfNulls<Object>(0)
    }
}