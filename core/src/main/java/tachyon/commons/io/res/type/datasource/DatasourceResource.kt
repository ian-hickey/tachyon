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
package tachyon.commons.io.res.type.datasource

import java.io.ByteArrayInputStream

class DatasourceResource internal constructor(provider: DatasourceResourceProvider, data: ConnectionData, path: String?) : ResourceSupport() {
    private val provider: DatasourceResourceProvider
    private var parent: String? = null

    @get:Override
    var name: String? = null
    private val data: ConnectionData
    private var fullPathHash = 0
    private var pathHash = 0
    private fun fullPathHash(): Int {
        if (fullPathHash == 0) fullPathHash = innerPath.hashCode()
        return fullPathHash
    }

    private fun pathHash(): Int {
        if (pathHash == 0 && parent != null) pathHash = parent.hashCode()
        return pathHash
    }

    private fun attr(): Attr? {
        return provider.getAttr(data, fullPathHash(), parent, name)
    }

    private val isRoot: Boolean
        private get() = parent == null

    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists)
        provider.create(data, fullPathHash(), pathHash(), parent, name, Attr.TYPE_DIRECTORY)
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists)
        provider.create(data, fullPathHash(), pathHash(), parent, name, Attr.TYPE_FILE)
    }

    @Override
    @Throws(IOException::class)
    fun remove(force: Boolean) {
        ResourceUtil.checkRemoveOK(this)
        if (isRoot) throw IOException("Can't remove root resource [$path]")
        val children: Array<Resource?>? = listResources()
        if (children != null && children.size > 0) {
            if (!force) {
                throw IOException("Can't delete directory [$path], directory is not empty")
            }
            for (i in children.indices) {
                children[i].remove(true)
            }
        }
        provider.delete(data, fullPathHash(), parent, name)
    }

    @Override
    fun exists(): Boolean {
        return attr()!!.exists()
    }

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() {
            ResourceUtil.checkGetInputStreamOK(this)
            return provider.getInputStream(data, fullPathHash(), parent, name)
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() = attr().getMode()
        set(mode) {
            if (!exists()) throw IOException("can't set mode on resource [$this], resource does not exist")
            provider.setMode(data, fullPathHash(), parent, name, mode)
        }

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        ResourceUtil.checkGetOutputStreamOK(this)
        var barr: ByteArray? = null
        if (append && !provider.concatSupported(data) && isFile) {
            try {
                val baos = ByteArrayOutputStream()
                IOUtil.copy(inputStream, baos, true, true)
                barr = baos.toByteArray()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        val os: OutputStream = provider.getOutputStream(data, fullPathHash(), pathHash(), parent, name, append)
        if (!ArrayUtil.isEmpty(barr)) IOUtil.copy(ByteArrayInputStream(barr), os, true, false)
        return os
    }

    @Override
    fun getParent(): String? {
        if (isRoot) return null
        val p = if (StringUtil.isEmpty(parent)) "/" else parent!!
        return provider.getScheme().concat("://").concat(data.key()).concat(ResourceUtil.translatePath(p, true, false))
    }

    @get:Override
    val parentResource: Resource?
        get() = parentDatasourceResource
    private val parentDatasourceResource: DatasourceResource?
        private get() = if (isRoot) null else DatasourceResource(provider, data, parent)

    @get:Override
    val path: String
        get() = provider.getScheme().concat("://").concat(data.key()).concat(innerPath)
    private val innerPath: String
        private get() = if (parent == null) "/" else parent.concat(name)

    @Override
    fun getRealResource(realpath: String): Resource? {
        var realpath = realpath
        realpath = ResourceUtil.merge(innerPath, realpath)
        return if (realpath.startsWith("../")) null else DatasourceResource(provider, data, realpath)
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider

    @get:Override
    val isAbsolute: Boolean
        get() = true

    @get:Override
    val isDirectory: Boolean
        get() = attr()!!.isDirectory()

    @get:Override
    val isFile: Boolean
        get() = attr()!!.isFile()

    @get:Override
    val isReadable: Boolean
        get() = ModeUtil.isReadable(mode)

    @get:Override
    val isWriteable: Boolean
        get() = ModeUtil.isWritable(mode)

    @Override
    fun lastModified(): Long {
        return attr().getLastModified()
    }

    @Override
    fun length(): Long {
        return attr()!!.size().toLong()
    }

    @Override
    fun listResources(): Array<Resource?>? {
        if (!attr()!!.isDirectory()) return null
        val path: String
        path = parent?.concat(name)?.concat("/") ?: "/"
        var children: Array<Attr?>? = null
        children = try {
            provider.getAttrs(data, path.hashCode(), path)
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
        if (children == null) return arrayOfNulls<Resource>(0)
        val attrs: Array<Resource?> = arrayOfNulls<Resource>(children.size)
        for (i in children.indices) {
            // TODO optimieren, alle attr mitgeben
            attrs[i] = DatasourceResource(provider, data, path + children[i].getName())
        }
        return attrs
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        return if (!exists()) false else provider.setLastModified(data, fullPathHash(), parent, name, time)
    }

    @Override
    @Throws(IOException::class)
    fun moveTo(dest: Resource?) {
        super.moveTo(dest) // TODO
    }

    @Override
    fun setReadable(readable: Boolean): Boolean {
        return if (!exists()) false else try {
            mode = ModeUtil.setReadable(mode, readable)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    fun setWritable(writable: Boolean): Boolean {
        return if (!exists()) false else try {
            mode = ModeUtil.setWritable(mode, writable)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    override fun toString(): String {
        return path
    }

    /**
     * Constructor of the class
     *
     * @param provider
     * @param data
     * @param path
     */
    init {
        this.provider = provider
        this.data = data
        if ("/".equals(path)) {
            parent = null
            name = ""
        } else {
            val pn: Array<String> = ResourceUtil.translatePathName(path)
            parent = pn[0]
            name = pn[1]
        }
    }
}