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
package lucee.commons.io.res.type.compress

import java.io.IOException

class CompressResource internal constructor(provider: CompressResourceProvider, zip: Compress, path: String?, caseSensitive: Boolean) : ResourceSupport() {
    private val provider: CompressResourceProvider
    private val zip: Compress
    val compressPath: String?

    @get:Override
    var name: String? = null
    private var parent: String? = null
    private val caseSensitive: Boolean

    /**
     * @return return ram resource that contain the data
     * @throws IOException
     */
    private val ramResource: Resource
        private get() = try {
            zip.getRamProviderResource(compressPath)
        } catch (e: IOException) {
            throw ExceptionUtil.toRuntimeException(e)
        }

    @Override
    fun exists(): Boolean {
        try {
            provider.read(this)
        } catch (e: IOException) {
            return false
        }
        return ramResource.exists()
    }

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() {
            ResourceUtil.checkGetInputStreamOK(this)
            return ramResource.getInputStream()
        }
    val compressResource: Resource
        get() = zip.getCompressFile()

    @Override
    fun getParent(): String? {
        return if (StringUtil.isEmpty(parent)) null else provider.getScheme().concat("://").concat(zip.getCompressFile().getPath()).concat("!").concat(parent)
    }

    @get:Override
    val parentResource: Resource?
        get() = if (StringUtil.isEmpty(parent)) null else CompressResource(provider, zip, parent, caseSensitive)

    @Override
    fun getPath(): String {
        return provider.getScheme().concat("://").concat(zip.getCompressFile().getPath()).concat("!").concat(compressPath)
    }

    @Override
    fun getRealResource(realpath: String): Resource? {
        var realpath = realpath
        realpath = ResourceUtil.merge(compressPath, realpath)
        return if (realpath.startsWith("../")) null else CompressResource(provider, zip, realpath, caseSensitive)
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = provider

    @get:Override
    val isAbsolute: Boolean
        get() = ramResource.isAbsolute()

    @get:Override
    val isDirectory: Boolean
        get() = ramResource.isDirectory()

    @get:Override
    val isFile: Boolean
        get() = ramResource.isFile()

    @get:Override
    val isReadable: Boolean
        get() = ramResource.isReadable()

    @get:Override
    val isWriteable: Boolean
        get() = ramResource.isWriteable()

    @Override
    fun lastModified(): Long {
        return ramResource.lastModified()
    }

    @Override
    fun length(): Long {
        return ramResource.length()
    }

    @Override
    fun listResources(): Array<Resource?>? {
        val names = list() ?: return null
        val children: Array<Resource?> = arrayOfNulls<Resource>(names.size)
        for (i in children.indices) {
            children[i] = CompressResource(provider, zip, compressPath.concat("/").concat(names[i]), caseSensitive)
        }
        return children
    }

    @Override
    fun list(): Array<String> {
        return ramResource.list()
    }

    @Override
    @Throws(IOException::class)
    fun remove(force: Boolean) {
        val rr: Resource = ramResource
        if (rr.getParent() == null) throw IOException("Can't remove root resource [" + getPath() + "]")
        if (!rr.exists()) throw IOException("Can't remove resource [" + getPath() + "], resource does not exist")
        val children: Array<Resource?>? = listResources()
        if (children != null && children.size > 0) {
            if (!force) {
                throw IOException("Can't delete directory [" + getPath() + "], directory is not empty")
            }
            for (i in children.indices) {
                children[i].remove(true)
            }
        }
        rr.remove(force)
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        val lm: Boolean = ramResource.setLastModified(time)
        zip.synchronize(provider.async)
        return lm
    }

    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists)
        ramResource.createDirectory(createParentWhenNotExists)
        zip.synchronize(provider.async)
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists)
        ramResource.createFile(createParentWhenNotExists)
        zip.synchronize(provider.async)
    }

    // Resource res = getRamResource();
    // Resource p = res.getParentResource();
    // if(p!=null && !p.exists())p.mkdirs();
    @get:Throws(IOException::class)
    @get:Override
    val outputStream: OutputStream
        get() {
            ResourceUtil.checkGetOutputStreamOK(this)
            // Resource res = getRamResource();
            // Resource p = res.getParentResource();
            // if(p!=null && !p.exists())p.mkdirs();
            return CompressOutputStreamSynchronizer(ramResource.getOutputStream(), zip, provider.async)
        }

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        return CompressOutputStreamSynchronizer(ramResource.getOutputStream(append), zip, provider.async)
    }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() = ramResource.getMode()
        set(mode) {
            ramResource.setMode(mode)
            zip.synchronize(provider.async)
        }

    @Override
    fun setReadable(value: Boolean): Boolean {
        if (!isFile) return false
        ramResource.setReadable(value)
        zip.synchronize(provider.async)
        return true
    }

    @Override
    fun setWritable(value: Boolean): Boolean {
        if (!isFile) return false
        ramResource.setWritable(value)
        zip.synchronize(provider.async)
        return true
    }

    /**
     * Constructor of the class
     *
     * @param provider
     * @param zip
     * @param path
     * @param caseSensitive
     */
    init {
        var path = path
        if (StringUtil.isEmpty(path)) path = "/"
        this.provider = provider
        this.zip = zip
        compressPath = path
        if ("/".equals(path)) {
            parent = null
            name = ""
        } else {
            val pn: Array<String> = ResourceUtil.translatePathName(path)
            parent = pn[0]
            name = pn[1]
        }
        this.caseSensitive = caseSensitive
    }
}