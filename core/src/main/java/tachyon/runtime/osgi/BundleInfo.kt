/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.osgi

import java.io.File

class BundleInfo(file: File?) : Serializable {
    private var version: Version? = null
    private var bundleName: String? = null
    var symbolicName: String? = null
    var exportPackage: String? = null
    var importPackage: String? = null
    var activator: String? = null
    var manifestVersion = 0
    var description: String? = null
    var dynamicImportPackage: String? = null
    var classPath: String? = null
    var requireBundle: String? = null
    var fragementHost: String? = null
    private var headers: Map<String?, Object?>? = null

    constructor(file: Resource?) : this(toFileResource(file)) {}

    val isBundle: Boolean
        get() = try {
            symbolicName != null && getVersion() != null
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            false
        }

    fun getVersion(): Version? {
        return version
    }

    val versionAsString: String?
        get() = if (version == null) null else version.toString()

    fun info(): Object? {
        val sct: Struct = StructImpl()
        sct.setEL(KeyConstants._Name, bundleName)
        sct.setEL("Fragment-Host", fragementHost)
        sct.setEL("Activator", activator)
        sct.setEL("ClassPath", classPath)
        sct.setEL("Description", description)
        sct.setEL("DynamicImportPackage", dynamicImportPackage)
        sct.setEL("ExportPackage", exportPackage)
        sct.setEL("ImportPackage", importPackage)
        sct.setEL("SymbolicName", symbolicName)
        sct.setEL(KeyConstants._Version, versionAsString)
        sct.setEL("ManifestVersion", manifestVersion)
        sct.setEL("RequireBundle", requireBundle)
        return sct
    }

    /**
     * Value can be a string (for a Single entry or a List<String> for multiple entries)
     *
     * @return
    </String> */
    fun getHeaders(): Map<String?, Object?>? {
        return headers
    }

    private fun createHeaders(attrs: Attributes?): Map<String?, Object?>? {
        val headers: Map<String?, Object?> = HashMap<String?, Object?>()
        val it: Iterator<Entry<Object?, Object?>?> = attrs.entrySet().iterator()
        var e: Entry<Object?, Object?>?
        var key: String
        var value: String
        var existing: Object?
        var list: List<String?>?
        while (it.hasNext()) {
            e = it.next()
            key = e.getKey().toString()
            value = StringUtil.unwrap(e.getValue().toString())
            existing = headers[key]
            if (existing != null) {
                if (existing is String) {
                    list = ArrayList()
                    list.add(existing as String?)
                    headers.put(key, list)
                } else list = existing
                list.add(value)
            } else headers.put(key, value)
        }
        return headers
    }

    fun toBundleDefinition(): BundleDefinition? {
        return BundleDefinition(symbolicName, getVersion())
    }

    companion object {
        private const val serialVersionUID = -8723070772449992030L
        private val bundles: Map<String?, BundleInfo?>? = HashMap<String?, BundleInfo?>()
        @Throws(IOException::class, BundleException::class)
        fun getInstance(id: String?, `is`: InputStream?, closeStream: Boolean): BundleInfo? {
            var bi = bundles!![id]
            if (bi != null) return bi
            val tmp: File = File.createTempFile("temp-extension", "lex")
            return try {
                val os = FileOutputStream(tmp)
                IOUtil.copy(`is`, os, closeStream, true)
                bundles.put(id, BundleInfo(tmp).also { bi = it })
                bi
            } finally {
                tmp.delete()
            }
        }

        @Throws(IOException::class)
        protected fun toFileResource(file: Resource?): File? {
            if (file is FileResource) return file as File?
            throw IOException("only file resources (local file system) are supported")
        }
    }

    init {
        val jar = JarFile(file)
        try {
            val manifest: Manifest = jar.getManifest()
            if (manifest == null) return
            val attrs: Attributes = manifest.getMainAttributes()
            if (attrs == null) return
            manifestVersion = Caster.toIntValue(attrs.getValue("Bundle-ManifestVersion"), 1)
            bundleName = attrs.getValue("Bundle-Name")
            symbolicName = attrs.getValue("Bundle-SymbolicName")
            val tmp: String = attrs.getValue("Bundle-Version")
            version = if (StringUtil.isEmpty(tmp, true)) null else OSGiUtil.toVersion(tmp)
            exportPackage = attrs.getValue("Export-Package")
            importPackage = attrs.getValue("Import-Package")
            dynamicImportPackage = attrs.getValue("DynamicImport-Package")
            activator = attrs.getValue("Bundle-Activator")
            description = attrs.getValue("Bundle-Description")
            classPath = attrs.getValue("Bundle-ClassPath")
            requireBundle = attrs.getValue("Require-Bundle")
            fragementHost = attrs.getValue("Fragment-Host")
            headers = createHeaders(attrs)
        } finally {
            IOUtil.closeEL(jar)
        }
    }
}