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
package lucee.commons.io.res.util

import java.io.File

class FileWrapper private constructor(res: Resource) : File(res.getPath()), Resource {
    private val res: Resource
    @Override
    fun canRead(): Boolean {
        return res.canRead()
    }

    @Override
    fun canWrite(): Boolean {
        return res.canWrite()
    }

    @Override
    operator fun compareTo(pathname: File): Int {
        if (res is File) (res as File).compareTo(pathname)
        return res.getPath().compareTo(pathname.getPath())
    }

    @Override
    fun createNewFile(): Boolean {
        return res.createNewFile()
    }

    @Override
    fun delete(): Boolean {
        return res.delete()
    }

    @Override
    fun deleteOnExit() {
        if (res is File) (res as File).deleteOnExit()
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return res.equals(obj)
    }

    @Override
    fun exists(): Boolean {
        return res.exists()
    }

    @get:Override
    val absoluteFile: File
        get() = if (res.isAbsolute()) this else FileWrapper(res.getAbsoluteResource())

    @get:Override
    val absolutePath: String
        get() = res.getAbsolutePath()

    @get:Throws(IOException::class)
    @get:Override
    val canonicalFile: File
        get() = FileWrapper(res.getCanonicalResource())

    @get:Throws(IOException::class)
    @get:Override
    val canonicalPath: String
        get() = res.getCanonicalPath()

    @get:Override
    val name: String
        get() = res.getName()

    @get:Override
    val parent: String
        get() = res.getParent()

    @get:Override
    val parentFile: File
        get() = FileWrapper(parentResource)

    @get:Override
    val path: String
        get() = res.getPath()

    @Override
    override fun hashCode(): Int {
        return res.hashCode()
    }

    @get:Override
    val isAbsolute: Boolean
        get() = res.isAbsolute()

    @get:Override
    val isDirectory: Boolean
        get() = res.isDirectory()

    @get:Override
    val isFile: Boolean
        get() = res.isFile()

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isHidden: Boolean
        get() = res.isHidden()
        set(value) {
            res.setHidden(value)
        }

    @Override
    fun lastModified(): Long {
        return res.lastModified()
    }

    @Override
    fun length(): Long {
        return res.length()
    }

    @Override
    fun list(): Array<String> {
        return res.list()
    }

    @Override
    fun list(filter: FilenameFilter): Array<String> {
        if (res is File) (res as File).list(filter)
        return list(FileNameFilterWrapper(filter) as ResourceNameFilter?)
    }

    @Override
    fun listFiles(): Array<File?>? {
        // if(res instanceof File) return ((File)res).listFiles();
        return toFiles(listResources())
    }

    private fun toFiles(resources: Array<Resource>?): Array<File?>? {
        if (resources == null) return null
        val files: Array<File?> = arrayOfNulls<File>(resources.size)
        for (i in resources.indices) {
            files[i] = FileWrapper(resources[i])
        }
        return files
    }

    @Override
    fun listFiles(filter: FileFilter): Array<File?>? {
        // if(res instanceof File) return ((File)res).listFiles(filter);
        return toFiles(listResources(FileFilterWrapper(filter)))
    }

    @Override
    fun listFiles(filter: FilenameFilter): Array<File?>? {
        // if(res instanceof File) return ((File)res).listFiles(filter);
        return toFiles(listResources(FileNameFilterWrapper(filter)))
    }

    @Override
    fun mkdir(): Boolean {
        return res.mkdir()
    }

    @Override
    fun mkdirs(): Boolean {
        return res.mkdirs()
    }

    @Override
    fun renameTo(dest: File): Boolean {
        return try {
            if (res is File) return (res as File).renameTo(dest)
            if (dest is Resource) return res.renameTo(dest as Resource)
            ResourceUtil.moveTo(this, ResourceUtil.toResource(dest), true)
            true
        } catch (ioe: IOException) {
            false
        }
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        return res.setLastModified(time)
    }

    @Override
    fun setReadOnly(): Boolean {
        return res.setReadOnly()
    }

    @Override
    override fun toString(): String {
        return res.toString()
    }

    @Override
    fun toURI(): URI? {
        return if (res is File) (res as File).toURI() else null
    }

    @Override
    @Throws(MalformedURLException::class)
    fun toURL(): URL? {
        return if (res is File) (res as File).toURL() else null
    }

    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        res.createDirectory(createParentWhenNotExists)
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        res.createFile(createParentWhenNotExists)
    }

    @get:Override
    val absoluteResource: Resource
        get() = res.getAbsoluteResource()

    @get:Throws(IOException::class)
    @get:Override
    val canonicalResource: Resource
        get() = res.getCanonicalResource()

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() = res.getInputStream()

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() = res.getMode()
        set(mode) {
            res.setMode(mode)
        }

    @get:Throws(IOException::class)
    @get:Override
    val outputStream: OutputStream
        get() = res.getOutputStream()

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        return res.getOutputStream(append)
    }

    @get:Override
    val parentResource: Resource
        get() = res.getParentResource()

    @Override
    fun getReal(realpath: String?): String {
        return res.getReal(realpath)
    }

    @Override
    fun getRealResource(realpath: String?): Resource {
        return res.getRealResource(realpath)
    }

    @get:Override
    val resourceProvider: ResourceProvider
        get() = res.getResourceProvider()

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isArchive: Boolean
        get() = res.isArchive()
        set(value) {
            res.setArchive(value)
        }

    @get:Override
    val isReadable: Boolean
        get() = res.isReadable()

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isSystem: Boolean
        get() = res.isSystem()
        set(value) {
            res.setSystem(value)
        }

    @get:Override
    val isWriteable: Boolean
        get() = res.isWriteable()

    @Override
    fun list(filter: ResourceNameFilter?): Array<String> {
        return res.list(filter)
    }

    @Override
    fun list(filter: ResourceFilter?): Array<String> {
        return res.list(filter)
    }

    @Override
    fun listResources(): Array<Resource> {
        return res.listResources()
    }

    @Override
    fun listResources(filter: ResourceFilter?): Array<Resource> {
        return res.listResources(filter)
    }

    @Override
    fun listResources(filter: ResourceNameFilter?): Array<Resource> {
        return res.listResources(filter)
    }

    @Override
    @Throws(IOException::class)
    fun moveTo(dest: Resource?) {
        res.moveTo(dest)
    }

    @Override
    @Throws(IOException::class)
    fun remove(force: Boolean) {
        res.remove(force)
    }

    @Override
    fun renameTo(dest: Resource?): Boolean {
        return res.renameTo(dest)
    }

    @Override
    fun getAttribute(attribute: Short): Boolean {
        return res.getAttribute(attribute)
    }

    @Override
    @Throws(IOException::class)
    fun setAttribute(attribute: Short, value: Boolean) {
        res.setAttribute(attribute, value)
    }

    @Override
    fun setReadable(value: Boolean): Boolean {
        return res.setReadable(value)
    }

    @Override
    fun setWritable(value: Boolean): Boolean {
        return res.setWritable(value)
    }

    @Override
    @Throws(IOException::class)
    fun copyFrom(res: Resource, append: Boolean) {
        res.copyFrom(res, append)
    }

    @Override
    @Throws(IOException::class)
    fun copyTo(res: Resource, append: Boolean) {
        res.copyTo(res, append)
    }

    companion object {
        /**
         * @param res
         * @return
         */
        fun toFile(res: Resource): File {
            return if (res is File) res as File else FileWrapper(res)
        }
    }

    /**
     * Constructor of the class
     *
     * @param res
     */
    init {
        this.res = res
    }
}