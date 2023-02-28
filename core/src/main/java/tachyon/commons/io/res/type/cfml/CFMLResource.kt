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

import java.io.IOException

class CFMLResource(provider: CFMLResourceProvider, component: Component) : ResourceSupport() {
    private val provider: CFMLResourceProvider
    private val component: Component

    @get:Override
    val isReadable: Boolean
        get() = provider.callbooleanRTE(null, component, "isReadable", ZERO_ARGS)

    @get:Override
    val isWriteable: Boolean
        get() = provider.callbooleanRTE(null, component, "isWriteable", ZERO_ARGS)

    @Override
    @Throws(IOException::class)
    fun remove(force: Boolean) {
        provider.callRTE(null, component, "remove", arrayOf<Object>(if (force) Boolean.TRUE else Boolean.FALSE))
    }

    @Override
    fun exists(): Boolean {
        return provider.callbooleanRTE(null, component, "exists", ZERO_ARGS)
    }

    @get:Override
    val name: String
        get() = provider.callStringRTE(null, component, "getName", ZERO_ARGS)

    @get:Override
    val parent: String
        get() = provider.callStringRTE(null, component, "getParent", ZERO_ARGS)

    @get:Override
    val parentResource: Resource?
        get() = provider.callResourceRTE(null, component, "getParentResource", ZERO_ARGS, true)

    @Override
    fun getRealResource(realpath: String): Resource {
        return provider.callResourceRTE(null, component, "getRealResource", arrayOf(realpath), true)
    }

    @get:Override
    val path: String
        get() = provider.callStringRTE(null, component, "getPath", ZERO_ARGS)

    @get:Override
    val isAbsolute: Boolean
        get() = provider.callbooleanRTE(null, component, "isAbsolute", ZERO_ARGS)

    @get:Override
    val isDirectory: Boolean
        get() = provider.callbooleanRTE(null, component, "isDirectory", ZERO_ARGS)

    @get:Override
    val isFile: Boolean
        get() = provider.callbooleanRTE(null, component, "isFile", ZERO_ARGS)

    @Override
    fun lastModified(): Long {
        val pc: PageContext = ThreadLocalPageContext.get()
        return try {
            val date: DateTime = Caster.toDate(provider.call(pc, component, "lastModified", ZERO_ARGS), true, pc.getTimeZone())
            date.getTime()
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun length(): Long {
        return provider.calllongRTE(null, component, "length", ZERO_ARGS)
    }

    @Override
    fun listResources(): Array<Resource?> {
        return provider.callResourceArrayRTE(null, component, "listResources", ZERO_ARGS)
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        val pc: PageContext = ThreadLocalPageContext.get()
        return provider.callbooleanRTE(pc, component, "setLastModified", arrayOf<Object>(DateTimeImpl(pc, time, false)))
    }

    @Override
    fun setWritable(writable: Boolean): Boolean {
        return provider.callbooleanRTE(null, component, "setWritable", arrayOf<Object>(if (writable) Boolean.TRUE else Boolean.FALSE))
    }

    @Override
    fun setReadable(readable: Boolean): Boolean {
        return provider.callbooleanRTE(null, component, "setReadable", arrayOf<Object>(if (readable) Boolean.TRUE else Boolean.FALSE))
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists)
        provider.lock(this)
        try {
            provider.callRTE(null, component, "createFile", arrayOf<Object>(if (createParentWhenNotExists) Boolean.TRUE else Boolean.FALSE))
        } finally {
            provider.unlock(this)
        }
    }

    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists)
        provider.lock(this)
        try {
            provider.callRTE(null, component, "createDirectory", arrayOf<Object>(if (createParentWhenNotExists) Boolean.TRUE else Boolean.FALSE))
        } finally {
            provider.unlock(this)
        }
    }

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() {
            ResourceUtil.checkGetInputStreamOK(this)
            return try {
                var obj: Object
                obj = if (provider.isUseStreams()) provider.call(null, component, "getInputStream", ZERO_ARGS) else provider.call(null, component, "getBinary", ZERO_ARGS)
                if (obj == null) obj = ByteArray(0)
                Caster.toInputStream(obj, null)
            } catch (pe: PageException) {
                throw PageRuntimeException(pe)
            }
        }

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        return try {
            if (provider.isUseStreams()) {
                val obj: Object = provider.call(null, component, "getOutputStream", arrayOf<Object>(if (append) Boolean.TRUE else Boolean.FALSE))
                return Caster.toOutputStream(obj)
            }
            CFMLResourceOutputStream(this)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    fun setBinary(obj: Object) {
        val barr: ByteArray
        if (obj is CharSequence) {
            val cs = obj as CharSequence
            val str = cs.toString()
            barr = str.getBytes(CharsetUtil.UTF8)
        } else {
            barr = Caster.toBinary(obj)
        }
        provider.call(null, component, "setBinary", arrayOf(barr))
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() = provider.callintRTE(null, component, "getMode", ZERO_ARGS)
        set(mode) {
            provider.callRTE(null, component, "setMode", arrayOf<Object>(Caster.toDouble(mode)))
        }

    companion object {
        private const val serialVersionUID = 7693378761683536212L
        private val ZERO_ARGS: Array<Object?> = arrayOfNulls<Object>(0)
    }

    init {
        this.provider = provider
        this.component = component
    }
}