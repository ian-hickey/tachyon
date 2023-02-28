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
package tachyon.commons.io.res.type.cache

import java.io.ByteArrayInputStream

/**
 * a ram resource
 */
class CacheResource : ResourceSupport, ResourceMetaData {
    private val provider: CacheResourceProvider
    private val parent: String?

    @get:Override
    val name: String?

    // private CacheResourceCore _core;
    internal constructor(provider: CacheResourceProvider, path: String?) {
        this.provider = provider
        if (path!!.equals("/")) {
            parent = null
            name = ""
        } else {
            val pn: Array<String> = ResourceUtil.translatePathName(path)
            parent = pn[0]
            name = pn[1]
        }
    }

    private constructor(provider: CacheResourceProvider, parent: String, name: String?) {
        this.provider = provider
        this.parent = parent
        this.name = name
    }

    private val core: tachyon.commons.io.res.type.cache.CacheResourceCore?
        private get() = provider.getCore(parent, name)

    @Throws(IOException::class)
    private fun removeCore() {
        provider.removeCore(parent, name)
    }

    @Throws(IOException::class)
    private fun createCore(type: Int): CacheResourceCore {
        return provider.createCore(parent, name, type)
    }

    @Throws(IOException::class)
    private fun touch() {
        provider.touch(parent, name)
    }

    @get:Override
    val path: String
        get() = provider.getScheme().concat("://").concat(innerPath)
    private val innerPath: String
        private get() = if (parent == null) "/" else parent.concat(name)

    @Override
    fun getParent(): String? {
        return if (isRoot) null else provider.getScheme().concat("://").concat(ResourceUtil.translatePath(parent, true, false))
    }

    @get:Override
    val isReadable: Boolean
        get() = ModeUtil.isReadable(mode)

    @get:Override
    val isWriteable: Boolean
        get() = ModeUtil.isWritable(mode)

    @Override
    @Throws(IOException::class)
    fun remove(force: Boolean) {
        if (isRoot) throw IOException("Can't remove root resource [$path]")
        provider.read(this)
        val core: CacheResourceCore = core
                ?: throw IOException("Can't remove resource [$path], resource does not exist")
        val children: Array<Resource?>? = listResources()
        if (children != null && children.size > 0) {
            if (!force) {
                throw IOException("Can't delete directory [$path], directory is not empty")
            }
            for (i in children.indices) {
                children[i].remove(true)
            }
        }
        removeCore()
    }

    @Override
    fun exists(): Boolean {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return true
        }
        return core != null
    }

    @get:Override
    val parentResource: Resource?
        get() = parentRamResource
    private val parentRamResource: CacheResource?
        private get() = if (isRoot) null else CacheResource(provider, parent)

    @Override
    fun getRealResource(realpath: String): Resource? {
        var realpath = realpath
        realpath = ResourceUtil.merge(innerPath, realpath)
        return if (realpath.startsWith("../")) null else CacheResource(provider, realpath)
    }

    @get:Override
    val isAbsolute: Boolean
        get() = true

    @get:Override
    val isDirectory: Boolean
        get() = exists() && core!!.getType() === CacheResourceCore.TYPE_DIRECTORY

    @get:Override
    val isFile: Boolean
        get() = exists() && core!!.getType() === CacheResourceCore.TYPE_FILE

    @Override
    fun lastModified(): Long {
        return if (!exists()) 0 else core.getLastModified()
    }

    @Override
    fun length(): Long {
        if (!exists()) return 0
        val data: ByteArray = core.getData() ?: return 0
        return data.size.toLong()
    }

    @Override
    fun list(): Array<String?>? {
        if (!exists()) return null
        val core: CacheResourceCore? = core
        return if (core!!.getType() !== CacheResourceCore.TYPE_DIRECTORY) null else try {
            provider.getChildNames(innerPath)
        } catch (e: IOException) {
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    @Override
    fun listResources(): Array<Resource?>? {
        val list = list() ?: return null
        val children: Array<Resource?> = arrayOfNulls<Resource>(list.size)
        var p = innerPath
        if (!isRoot) p = p.concat("/")
        for (i in children.indices) {
            children[i] = CacheResource(provider, p, list[i])
        }
        return children
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        if (!exists()) return false
        core.setLastModified(time)
        return true
    }

    @Override
    fun setReadOnly(): Boolean {
        return setWritable(false)
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists)
        provider.lock(this)
        try {
            createCore(CacheResourceCore.TYPE_FILE)
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
            createCore(CacheResourceCore.TYPE_DIRECTORY)
        } finally {
            provider.unlock(this)
        }
    }

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() {
            ResourceUtil.checkGetInputStreamOK(this)
            provider.lock(this)
            val core: CacheResourceCore? = core
            var data: ByteArray = core.getData()
            if (data == null) data = ByteArray(0)
            provider.unlock(this)
            return ByteArrayInputStream(data)
        }

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        ResourceUtil.checkGetOutputStreamOK(this)
        provider.lock(this)
        return CacheOutputStream(this, append)
    }

    val contentType: ContentType
        get() = ResourceUtil.getContentType(this)

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider

    @Override
    override fun toString(): String {
        return path
    }

    /**
     * This is used by the MemoryResource too write back data to, that are written to outputstream
     */
    internal inner class CacheOutputStream
    /**
     * Constructor of the class
     *
     * @param res
     */(private val res: CacheResource, private val append: Boolean) : ByteArrayOutputStream() {
        @Override
        @Throws(IOException::class)
        fun close() {
            try {
                super.close()
                var core: CacheResourceCore? = res.core
                if (core == null) core = res.createCore(CacheResourceCore.TYPE_FILE) else core.setLastModified(System.currentTimeMillis())
                core.setData(this.toByteArray(), append)
                touch()
            } finally {
                res.resourceProvider.unlock(res)
            }
        }
    }

    @Override
    fun setReadable(value: Boolean): Boolean {
        return if (!exists()) false else try {
            mode = ModeUtil.setReadable(mode, value)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    fun setWritable(value: Boolean): Boolean {
        return if (!exists()) false else try {
            mode = ModeUtil.setWritable(mode, value)
            true
        } catch (e: IOException) {
            false
        }
    }

    private val isRoot: Boolean
        private get() = parent == null

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() = if (!exists()) 0 else core.getMode()
        set(mode) {
            if (!exists()) throw IOException("Can't set mode on resource [$this], resource does not exist")
            core.setMode(mode)
        }

    @Override
    fun getAttribute(attribute: Short): Boolean {
        return if (!exists()) false else core.getAttributes() and attribute > 0
    }

    @Override
    @Throws(IOException::class)
    fun setAttribute(attribute: Short, value: Boolean) {
        if (!exists()) throw IOException("Can't get attributes on resource [$this], resource does not exist")
        var attr: Int = core.getAttributes()
        if (value) {
            if (attr and attribute.toInt() == 0) attr += attribute.toInt()
        } else {
            if (attr and attribute.toInt() > 0) attr -= attribute.toInt()
        }
        core.setAttributes(attr)
    }

    @get:Override
    val metaData: Struct?
        get() = provider.getMeta(parent, name)
}