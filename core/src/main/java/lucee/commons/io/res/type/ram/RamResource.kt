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
package lucee.commons.io.res.type.ram

import java.io.ByteArrayInputStream

/**
 * a ram resource
 */
class RamResource : ResourceSupport {
    private val provider: RamResourceProviderOld
    private val parent: String?

    @get:Override
    val name: String?
    private var _core: RamResourceCore? = null

    internal constructor(provider: RamResourceProviderOld, path: String?) {
        this.provider = provider
        if (path!!.equals("/") || StringUtil.isEmpty(path)) {
            // if(path.equals("/")) {
            parent = null
            name = ""
        } else {
            val pn: Array<String> = ResourceUtil.translatePathName(path)
            parent = pn[0]
            name = pn[1]
        }
    }

    private constructor(provider: RamResourceProviderOld, parent: String, name: String?) {
        this.provider = provider
        this.parent = parent
        this.name = name
    }

    val core: lucee.commons.io.res.type.ram.RamResourceCore?
        get() {
            if (_core == null || _core.getType() === 0) {
                _core = provider.getCore(innerPath)
            }
            return _core
        }

    fun removeCore() {
        if (_core == null) return
        _core.remove()
        _core = null
    }

    @Throws(IOException::class)
    private fun createCore(type: Int): RamResourceCore {
        return provider.createCore(innerPath, type).also { _core = it }
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
        val core: RamResourceCore = core ?: throw IOException("Can't remove resource [$path],resource does not exist")
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
    private val parentRamResource: RamResource?
        private get() = if (isRoot) null else RamResource(provider, parent)

    @Override
    fun getRealResource(realpath: String): Resource? {
        var realpath = realpath
        realpath = ResourceUtil.merge(innerPath, realpath)
        return if (realpath.startsWith("../")) null else RamResource(provider, realpath)
    }

    @get:Override
    val isAbsolute: Boolean
        get() = true

    @get:Override
    val isDirectory: Boolean
        get() = exists() && core!!.getType() === RamResourceCore.TYPE_DIRECTORY

    @get:Override
    val isFile: Boolean
        get() = exists() && core!!.getType() === RamResourceCore.TYPE_FILE

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
        val core: RamResourceCore? = core
        return if (core!!.getType() !== RamResourceCore.TYPE_DIRECTORY) null else core.getChildNames()
        /*
		 * List list = core.getChildren(); if(list==null && list.size()==0) return new String[0];
		 * 
		 * Iterator it = list.iterator(); String[] children=new String[list.size()]; RamResourceCore cc; int
		 * count=0; while(it.hasNext()) { cc=(RamResourceCore) it.next(); children[count++]=cc.getName(); }
		 * return children;
		 */
    }

    @Override
    fun listResources(): Array<Resource?>? {
        val list = list() ?: return null
        val children: Array<Resource?> = arrayOfNulls<Resource>(list.size)
        var p = innerPath
        if (!isRoot) p = p.concat("/")
        for (i in children.indices) {
            children[i] = RamResource(provider, p, list[i])
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
            createCore(RamResourceCore.TYPE_FILE)
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
            createCore(RamResourceCore.TYPE_DIRECTORY)
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
            val core: RamResourceCore? = core
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
        return RamOutputStream(this, append)
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
    internal inner class RamOutputStream
    /**
     * Constructor of the class
     *
     * @param res
     */(private val res: RamResource, private val append: Boolean) : ByteArrayOutputStream() {
        @Override
        @Throws(IOException::class)
        fun close() {
            try {
                super.close()
                var core: RamResourceCore? = res.core
                if (core == null) core = res.createCore(RamResourceCore.TYPE_FILE)
                core.setData(this.toByteArray(), append)
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
}