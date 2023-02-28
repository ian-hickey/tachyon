/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.library

import java.io.Externalizable

class ClassDefinitionImpl<T> : ClassDefinition<T?>, Externalizable {
    /**
     * do not use to load class!!!
     */
    private var className: String? = null
    private var name: String? = null
    private var version: Version? = null
    private var id: Identification? = null
    private var versionOnlyMattersWhenDownloading = false

    @Transient
    private var clazz: Class<T?>? = null

    constructor(className: String?, name: String?, version: String?, id: Identification?) {
        this.className = className?.trim()
        this.name = if (StringUtil.isEmpty(name, true)) null else name.trim()
        this.version = OSGiUtil.toVersion(version, null)
        this.id = id
    }

    constructor(id: Identification?, className: String?, name: String?, version: Version?) {
        this.className = className?.trim()
        this.name = if (StringUtil.isEmpty(name, true)) null else name.trim()
        this.version = version
        this.id = id
    }

    constructor(className: String?) {
        this.className = className?.trim()
        name = null
        version = null
        id = null
    }

    constructor(clazz: Class<T?>?) {
        className = clazz.getName()
        this.clazz = clazz
        name = null
        version = null
        id = null
    }

    /**
     * only used by deserializer!
     */
    constructor() {}

    fun setVersionOnlyMattersWhenDownloading(versionOnlyMattersWhenDownloading: Boolean): ClassDefinitionImpl<T?>? {
        this.versionOnlyMattersWhenDownloading = versionOnlyMattersWhenDownloading
        return this
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        out.writeObject(className)
        out.writeObject(name)
        out.writeObject(if (version == null) null else version.toString())
        out.writeObject(id)
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        className = `in`.readObject()
        name = `in`.readObject()
        val tmp = `in`.readObject() as String
        if (tmp != null) version = OSGiUtil.toVersion(tmp, null)
        id = `in`.readObject() as Identification
    }

    @Override
    @Throws(ClassException::class, BundleException::class)
    fun getClazz(): Class<T?>? {
        return getClazz(false)
    }

    @Throws(ClassException::class, BundleException::class)
    fun getClazz(forceLoadingClass: Boolean): Class<T?>? {
        if (!forceLoadingClass && clazz != null) return clazz

        // regular class definition
        return if (name == null) ClassUtil.loadClass(className).also { clazz = it } else ClassUtil.loadClassByBundle(className, name, version, id, JavaSettingsImpl.getBundleDirectories(null), versionOnlyMattersWhenDownloading).also { clazz = it }
    }

    @Override
    fun getClazz(defaultValue: Class<T?>?): Class<T?>? {
        return try {
            getClazz()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Override
    fun hasClass(): Boolean {
        return !StringUtil.isEmpty(className, true)
    }

    @Override
    fun isBundle(): Boolean {
        return !StringUtil.isEmpty(name, true)
    }

    @Override
    fun hasVersion(): Boolean {
        return version != null
    }

    @Override
    fun isClassNameEqualTo(otherClassName: String?): Boolean {
        return isClassNameEqualTo(otherClassName, false)
    }

    @Override
    fun isClassNameEqualTo(otherClassName: String?, ignoreCase: Boolean): Boolean {
        var otherClassName = otherClassName ?: return false
        otherClassName = otherClassName.trim()
        return if (ignoreCase) otherClassName.equalsIgnoreCase(className) else otherClassName.equals(className)
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj !is ClassDefinition) return false
        val other: ClassDefinition? = obj as ClassDefinition?
        return (StringUtil.emptyIfNull(other.getClassName()).equals(StringUtil.emptyIfNull(className))
                && StringUtil.emptyIfNull(other.getName()).equals(StringUtil.emptyIfNull(name))
                && if (other.getVersion() != null) other.getVersion().equals(version) else version == null)
    }

    @Override
    override fun toString(): String { // do not remove, this is used as key in ConfigWebFactory
        return if (isBundle()) "class:$className;name:$name;version:$version;" else className!!
    }

    @Override
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    @Override
    fun getClassName(): String? {
        return className
    }

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getVersion(): Version? {
        return version
    }

    @Override
    fun getVersionAsString(): String? {
        return if (version == null) null else version.toString()
    }

    @Override
    fun getId(): String? {
        return HashUtil.create64BitHashAsString(toString())
    }

    companion object {
        fun toClassDefinition(className: String?, id: Identification?, attributes: Map<String?, String?>?): ClassDefinition? {
            if (StringUtil.isEmpty(className, true)) return null
            var bn: String? = null
            var bv: String? = null
            if (attributes != null) {
                // name
                bn = attributes["name"]
                if (StringUtil.isEmpty(bn)) bn = attributes["bundle-name"]

                // version
                bv = attributes["version"]
                if (StringUtil.isEmpty(bv)) bv = attributes["bundle-version"]
            }
            return ClassDefinitionImpl<Any?>(className, bn, bv, id)
        }
    }
}