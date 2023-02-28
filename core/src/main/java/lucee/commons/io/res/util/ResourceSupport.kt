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

import java.io.IOException

/**
 * Helper class to build resources
 */
abstract class ResourceSupport : Resource {
    @Override
    @Throws(IOException::class)
    fun copyFrom(res: Resource?, append: Boolean) {
        IOUtil.copy(res, outputStream, true)
    }

    @Override
    @Throws(IOException::class)
    fun copyTo(res: Resource, append: Boolean) {
        IOUtil.copy(this, res.getOutputStream(append), true)
    }

    @get:Override
    val absoluteResource: Resource
        get() = this

    @get:Override
    val absolutePath: String
        get() = getPath()

    @get:Throws(IOException::class)
    @get:Override
    val outputStream: OutputStream
        get() = outputStream

    @get:Throws(IOException::class)
    @get:Override
    val canonicalResource: Resource
        get() = this

    @get:Throws(IOException::class)
    @get:Override
    val canonicalPath: String
        get() = getPath()

    @Override
    @Throws(IOException::class)
    fun moveTo(dest: Resource) {
        ResourceUtil.moveTo(this, dest, false)
    }

    @Override
    fun list(filter: ResourceFilter): Array<String>? {
        val files = list() ?: return null
        val list: List<String> = ArrayList<String>()
        var res: Resource?
        for (i in files.indices) {
            res = getRealResource(files[i])
            if (filter.accept(res)) list.add(files[i])
        }
        return list.toArray(arrayOfNulls<String>(list.size()))
    }

    @Override
    fun list(filter: ResourceNameFilter): Array<String?>? {
        val lst = list() ?: return null
        val list: List<String> = ArrayList<String>()
        for (i in lst.indices) {
            if (filter.accept(getParentResource(), lst[i])) list.add(lst[i])
        }
        if (list.size() === 0) return arrayOfNulls(0)
        return if (list.size() === lst.size) lst else list.toArray(arrayOfNulls<String>(list.size()))
    }

    @Override
    fun listResources(filter: ResourceNameFilter): Array<Resource>? {
        val files = list() ?: return null
        val list: List<Resource> = ArrayList<Resource>()
        for (i in files.indices) {
            if (filter.accept(this, files[i])) list.add(getRealResource(files[i]))
        }
        return list.toArray(arrayOfNulls<Resource>(list.size()))
    }

    @Override
    fun listResources(filter: ResourceFilter): Array<Resource>? {
        val files = list() ?: return null
        val list: List<Resource> = ArrayList<Resource>()
        var res: Resource?
        for (i in files.indices) {
            res = this.getRealResource(files[i])
            if (filter.accept(res)) list.add(res)
        }
        return list.toArray(arrayOfNulls<Resource>(list.size()))
    }

    @Override
    fun getReal(realpath: String?): String {
        return getRealResource(realpath).getPath()
    }

    @Override
    fun list(): Array<String?>? {
        val children: Array<Resource> = listResources() ?: return null
        val rtn = arrayOfNulls<String>(children.size)
        for (i in children.indices) {
            rtn[i] = children[i].getName()
        }
        return rtn
    }

    @Override
    fun canRead(): Boolean {
        return isReadable()
    }

    @Override
    fun canWrite(): Boolean {
        return isWriteable()
    }

    @Override
    fun renameTo(dest: Resource): Boolean {
        return try {
            moveTo(dest)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    fun createNewFile(): Boolean {
        try {
            createFile(false)
            return true
        } catch (e: IOException) {
        }
        return false
    }

    @Override
    fun mkdir(): Boolean {
        try {
            createDirectory(false)
            return true
        } catch (e: IOException) {
        }
        return false
    }

    @Override
    fun mkdirs(): Boolean {
        return try {
            createDirectory(true)
            true
        } catch (e: IOException) {
            false
        }
    }

    @Override
    fun delete(): Boolean {
        try {
            remove(false)
            return true
        } catch (e: IOException) {
        }
        return false
    }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isArchive: Boolean
        get() = getAttribute(Resource.ATTRIBUTE_ARCHIVE)
        set(value) {
            setAttribute(ATTRIBUTE_ARCHIVE, value)
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isSystem: Boolean
        get() = getAttribute(Resource.ATTRIBUTE_SYSTEM)
        set(value) {
            setAttribute(ATTRIBUTE_SYSTEM, value)
        }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var isHidden: Boolean
        get() = getAttribute(Resource.ATTRIBUTE_HIDDEN)
        set(value) {
            setAttribute(ATTRIBUTE_HIDDEN, value)
        }

    @Override
    fun setReadOnly(): Boolean {
        return setWritable(false)
    }

    @Override
    override fun equals(obj: Object): Boolean {
        if (this === obj) return true
        if (obj !is Resource) return false
        val other: Resource = obj as Resource
        if (getResourceProvider() !== other.getResourceProvider()) return false
        if (getResourceProvider().isCaseSensitive()) {
            return if (getPath().equals(other.getPath())) true else ResourceUtil.getCanonicalPathEL(this).equals(ResourceUtil.getCanonicalPathEL(other))
        }
        return if (getPath().equalsIgnoreCase(other.getPath())) true else ResourceUtil.getCanonicalPathEL(this).equalsIgnoreCase(ResourceUtil.getCanonicalPathEL(other))
    }

    @Override
    override fun toString(): String {
        return getPath()
    }

    @Override
    fun getAttribute(attribute: Short): Boolean {
        return false
    }

    @Override
    @Throws(IOException::class)
    fun setAttribute(attribute: Short, value: Boolean) {
        throw IOException("the resource [" + getPath().toString() + "] does not support attributes")
    }
}