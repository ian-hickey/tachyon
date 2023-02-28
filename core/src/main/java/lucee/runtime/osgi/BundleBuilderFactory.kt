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
package lucee.runtime.osgi

import java.io.ByteArrayInputStream

class BundleBuilderFactory {
    companion object {
        // Indicates the OSGi specification to use for reading this bundle.
        const val MANIFEST_VERSION = 2
        private val INDIVIDUAL_FILTER: Set<String?>? = HashSet<String?>()
        private val MAIN_FILTER: Set<String?>? = HashSet<String?>()
        fun createSymbolicName(jar: Resource?): String? {
            var name: String? = jar.getName()
            val index: Int = name.lastIndexOf('.')
            if (index != -1) {
                name = name.substring(0, index)
            }
            return toSymbolicName(name)
        }

        private fun toSymbolicName(name: String?): String? {
            var name = name
            name = name.replace(' ', '.')
            name = name.replace('_', '.')
            name = name.replace('-', '.')
            return name
        }

        private fun addPackages(packages: Collection<String?>?, str: String?) {
            val st = StringTokenizer(str, ",")
            while (st.hasMoreTokens()) {
                packages.add(st.nextToken().trim())
            }
        }

        @Throws(IOException::class)
        private fun copy(`in`: InputStream?, out: OutputStream?) {
            val buffer = ByteArray(0xffff)
            var len: Int
            while (`in`.read(buffer).also { len = it } != -1) out.write(buffer, 0, len)
        }

        init {
            MAIN_FILTER.add("SHA1-Digest-Manifest")
            MAIN_FILTER.add("MD5-Digest-Manifest")
            // MAIN_FILTER.add("Sealed");
            INDIVIDUAL_FILTER.add("SHA1-Digest")
            INDIVIDUAL_FILTER.add("MD5-Digest")
            // INDIVIDUAL_FILTER.add("Sealed");
        }
    }

    private var name: String? = null
    private val symbolicName: String?
    var description: String? = null
    private var manifest: Manifest? = null
    private val existingPackages: Set<String?>? = HashSet<String?>()
    private var ignoreExistingManifest = false
    var activator: String? = null

    // private List<Resource> jars=new ArrayList<Resource>();
    var exportPackage: List<String?>? = null
        private set
    var fragmentHost: List<String?>? = null
        private set
    var importPackage: List<String?>? = null
        private set
    var requireBundle: List<String?>? = null
        private set
    var requireBundleFragment: List<String?>? = null
        private set
    var dynamicImportPackage: List<String?>? = null
        private set
    var classPath: List<String?>? = null
        private set

    // private BundleFile bf;
    private var jar: Resource?
    private var version: Version? = null
    private var bundleActivationPolicy: String? = null

    /**
     *
     * @param symbolicName this entry specifies a unique identifier for a bundle, based on the reverse
     * domain name convention (used also by the java packages).
     * @param name Defines a human-readable name for this bundle, Simply assigns a short name to the
     * bundle.
     * @param description A description of the bundle's functionality.
     * @param version Designates a version number to the bundle.
     * @param activator Indicates the class name to be invoked once a bundle is activated.
     * @param name
     * @throws IOException
     * @throws BundleException
     * @throws BundleBuilderFactoryException
     */
    constructor(jar: Resource?, symbolicName: String?) {
        if (!jar.isFile()) throw IOException("[$jar] is not a file")
        this.jar = jar
        // bf = new BundleFile(jar);
        if (StringUtil.isEmpty(symbolicName)) {
            // if(StringUtil.isEmpty(name))
            throw BundleException("symbolic name is reqired")
        }
        this.symbolicName = toSymbolicName(symbolicName)
    }

    constructor(jar: Resource?) {
        if (!jar.isFile()) throw ApplicationException("[$jar] is not a file")
        this.jar = jar
        symbolicName = createSymbolicName(jar)
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun setIgnoreExistingManifest(ignoreExistingManifest: Boolean) {
        this.ignoreExistingManifest = ignoreExistingManifest
    }

    fun getVersion(): Version? {
        return version
    }

    @Throws(BundleException::class)
    fun setVersion(version: String?) {
        if (StringUtil.isEmpty(version, true)) return
        this.version = OSGiUtil.toVersion(version)
    }

    fun setVersion(version: Version?) {
        if (version == null) return
        this.version = version
    }

    fun addExportPackage(strExportPackage: String?) {
        if (StringUtil.isEmpty(strExportPackage)) return
        if (exportPackage == null) exportPackage = ArrayList<String?>()
        addPackages(exportPackage, strExportPackage)
    }

    fun addRequireBundle(strRequireBundle: String?) {
        if (StringUtil.isEmpty(strRequireBundle)) return
        if (requireBundle == null) requireBundle = ArrayList<String?>()
        addPackages(requireBundle, strRequireBundle)
    }

    fun addRequireBundleFragment(strRequireBundleFragment: String?) {
        if (StringUtil.isEmpty(strRequireBundleFragment)) return
        if (requireBundleFragment == null) requireBundleFragment = ArrayList<String?>()
        addPackages(requireBundleFragment, strRequireBundleFragment)
    }

    fun addFragmentHost(strExportPackage: String?) {
        if (StringUtil.isEmpty(strExportPackage)) return
        if (fragmentHost == null) fragmentHost = ArrayList<String?>()
        addPackages(fragmentHost, strExportPackage)
    }

    fun setBundleActivationPolicy(bundleActivationPolicy: String?) {
        this.bundleActivationPolicy = bundleActivationPolicy
    }

    fun addImportPackage(strImportPackage: String?) {
        if (StringUtil.isEmpty(strImportPackage)) return
        if (importPackage == null) importPackage = ArrayList<String?>()
        addPackages(importPackage, strImportPackage)
    }

    fun addDynamicImportPackage(strDynImportPackage: String?) {
        if (StringUtil.isEmpty(strDynImportPackage)) return
        if (dynamicImportPackage == null) dynamicImportPackage = ArrayList<String?>()
        addPackages(dynamicImportPackage, strDynImportPackage)
    }

    fun addClassPath(str: String?) {
        if (classPath == null) classPath = ArrayList<String?>()
        addPackages(classPath, str)
    }

    private fun extendManifest(mf: Manifest?) {
        val attrs: Attributes = mf.getMainAttributes()
        attrs.putValue("Bundle-ManifestVersion", "" + MANIFEST_VERSION)
        if (!StringUtil.isEmpty(name)) attrs.putValue("Bundle-Name", name)
        attrs.putValue("Bundle-SymbolicName", symbolicName)
        if (!StringUtil.isEmpty(description)) attrs.putValue("Bundle-Description", description)
        if (!StringUtil.isEmpty(bundleActivationPolicy)) attrs.putValue("Bundle-ActivationPolicy", bundleActivationPolicy)
        if (version != null) attrs.putValue("Bundle-Version", version.toString())
        if (!StringUtil.isEmpty(activator)) {
            if (!activator.equalsIgnoreCase("none")) {
                attrs.putValue("Bundle-Activator", activator)
                addImportPackage("org.osgi.framework")
            } else {
                // attrs.remove("Bundle-Activator");
                attrs.putValue("Bundle-Activator", "")
            }
        }

        // Export-Package
        var str: String? = if (ignoreExistingManifest) null else attrs.getValue("Export-Package")
        // no existing Export-Package
        val set: Set<String?>?
        if (Util.isEmpty(str, true)) {
            set = existingPackages
        } else {
            set = HashSet<String?>()
            addPackages(set, str)
        }
        if (!ArrayUtil.isEmpty(exportPackage) && !isAsterix(exportPackage)) {
            val it = exportPackage!!.iterator()
            while (it.hasNext()) {
                set.add(it.next())
            }
        }
        exportPackage = ListUtil.toList(set)
        addList(attrs, "Export-Package", exportPackage)

        // Require-Bundle
        str = attrs.getValue("Require-Bundle")
        if (Util.isEmpty(str, true)) addList(attrs, "Require-Bundle", requireBundle)

        // Require-Bundle
        str = attrs.getValue("Require-Bundle-Fragment")
        if (Util.isEmpty(str, true)) addList(attrs, "Require-Bundle-Fragment", requireBundleFragment)

        // str = attrs.getValue("Fragment-Host");
        // if(Util.isEmpty(str,true))
        attrs.remove("Fragment-Host")
        addList(attrs, "Fragment-Host", fragmentHost)
        str = attrs.getValue("Import-Package")
        if (Util.isEmpty(str, true)) addList(attrs, "Import-Package", importPackage)
        str = attrs.getValue("DynamicImport-Package")
        if (Util.isEmpty(str, true)) addList(attrs, "DynamicImport-Package", dynamicImportPackage)
        str = attrs.getValue("Bundle-ClassPath")
        if (Util.isEmpty(str, true)) addList(attrs, "Bundle-ClassPath", classPath)
    }

    /*
	 * private static List<String> createExportPackageFromResource(Resource jar) { // get all
	 * directories List<Resource> dirs = ResourceUtil.listRecursive(jar,DirectoryResourceFilter.FILTER);
	 * List<String> rtn=new ArrayList<String>(); // remove directories with no files (of any kind)
	 * Iterator<Resource> it = dirs.iterator(); Resource[] children; int count; while(it.hasNext()) {
	 * Resource r = it.next(); children = r.listResources(); count=0; if(children!=null)for(int
	 * i=0;i<children.length;i++){ if(children[i].isFile())count++; } // has files if(count>0) {
	 * 
	 * } }
	 * 
	 * return null; }
	 */
    private fun isAsterix(list: List<String?>?): Boolean {
        if (list == null) return false
        val it = list.iterator()
        while (it.hasNext()) {
            if ("*".equals(it.next())) return true
        }
        return false
    }

    private fun addList(attrs: Attributes?, name: String?, values: List<String?>?) {
        if (values == null || values.isEmpty()) return
        val sb = StringBuilder()
        val it = values.iterator()
        var first = true
        while (it.hasNext()) {
            if (!first) {
                sb.append(',')
            }
            sb.append(it.next())
            first = false
        }
        attrs.putValue(name, sb.toString())
    }

    @Throws(IOException::class)
    fun build() {
        val res: Resource = SystemUtil.getTempFile(".jar", false)
        try {
            build(res)
            IOUtil.copy(res, jar)
        } finally {
            res.delete()
        }
    }

    @Throws(IOException::class)
    fun build(target: Resource?) {
        val os: OutputStream = target.getOutputStream()
        try {
            build(os)
        } finally {
            IOUtil.close(os)
        }
    }

    @Throws(IOException::class)
    fun build(os: OutputStream?) {
        val zos: ZipOutputStream = MyZipOutputStream(os, CharsetUtil.UTF8)
        try {

            // jar
            handleEntry(zos, jar, JarEntryListener(zos))

            // Manifest (do a blank one when method above has not loaded one)
            if (manifest == null) manifest = Manifest()
            extendManifest(manifest)
            val mf: String = ManifestUtil.toString(manifest, 128, MAIN_FILTER, INDIVIDUAL_FILTER)
            val `is`: InputStream = ByteArrayInputStream(mf.getBytes(CharsetUtil.UTF8))
            val ze = ZipEntry("META-INF/MANIFEST.MF")
            zos.putNextEntry(ze)
            try {
                copy(`is`, zos)
            } finally {
                IOUtil.close(`is`)
                zos.closeEntry()
            }
        } finally {
            IOUtil.close(zos)
        }
    }

    @Throws(IOException::class)
    private fun handleEntry(target: ZipOutputStream?, file: Resource?, listener: EntryListener?) {
        val zis = ZipInputStream(file.getInputStream())
        try {
            var entry: ZipEntry?
            while (zis.getNextEntry().also { entry = it } != null) {
                listener!!.handleEntry(file, zis, entry)
                zis.closeEntry()
            }
        } finally {
            IOUtil.close(zis)
        }
    }

    internal inner class JarEntryListener(zos: ZipOutputStream?) : EntryListener {
        private val zos: ZipOutputStream?
        @Override
        @Throws(IOException::class)
        override fun handleEntry(zipFile: Resource?, source: ZipInputStream?, entry: ZipEntry?) {

            // log for export-package
            if (!entry.isDirectory()) {
                var name: String? = entry.getName()
                val index: Int = name.lastIndexOf('/')
                if (index != -1 && !name.startsWith("META-INF")) {
                    name = name.substring(0, index)
                    if (name!!.length() > 0) existingPackages.add(ListUtil.trim(name.replace('/', '.'), "."))
                }
            }

            // security
            if ("META-INF/IDRSIG.DSA".equalsIgnoreCase(entry.getName()) || "META-INF/IDRSIG.SF".equalsIgnoreCase(entry.getName())
                    || "META-INF/INDEX.LIST".equalsIgnoreCase(entry.getName())) {
                return
            }

            // manifest
            if ("META-INF/MANIFEST.MF".equalsIgnoreCase(entry.getName())) {
                if (!ignoreExistingManifest) {
                    manifest = Manifest(source)
                    val attrs: Attributes = manifest.getMainAttributes()

                    // they are in bootdelegation
                    // ManifestUtil.removeFromList(attrs,"Import-Package","javax.*");
                    ManifestUtil.removeOptional(attrs, "Import-Package")

                    // ManifestUtil.removeFromList(attrs,"Import-Package","org.osgi.*");
                }
                return
            }

            // ignore the following stuff
            if (entry.getName().endsWith(".DS_Store") || entry.getName().startsWith("__MACOSX")) {
                return
            }
            val ze: MyZipEntry = MyZipEntry(entry.getName())
            ze.setComment(entry.getComment())
            ze.setTime(entry.getTime())
            ze.file = zipFile
            try {
                zos.putNextEntry(ze)
            } catch (naee: NameAlreadyExistsException) {
                if (entry.isDirectory()) {
                    return
                }
                log("--------------------------------")
                log(ze.getName())
                log("before:" + naee.getFile())
                log("curren:$zipFile")
                log("size:" + naee.getSize().toString() + "==" + entry.getSize())
                return  // TODO throw naee;
            }
            try {
                copy(source, zos)
            } finally {
                zos.closeEntry()
            }
        }

        init {
            this.zos = zos
        }
    }

    interface EntryListener {
        @Throws(IOException::class)
        fun handleEntry(zipFile: Resource?, source: ZipInputStream?, entry: ZipEntry?)
    }

    inner class MyZipOutputStream(out: OutputStream?, charset: Charset?) : ZipOutputStream(out) {
        private val names: Map<String?, Resource?>? = HashMap<String?, Resource?>()
        @Override
        @Throws(IOException::class)
        fun putNextEntry(e: ZipEntry?) {
            val file: Resource? = names!![e.getName()]
            if (names.containsKey(e.getName())) throw NameAlreadyExistsException(e.getName(), file, e.getSize())
            if (e is MyZipEntry) names.put(e.getName(), (e as MyZipEntry?)!!.file)
            super.putNextEntry(e)
        }
    }

    inner class MyZipEntry : ZipEntry {
        var file: Resource? = null

        constructor(name: String?) : super(name) {}
        constructor(e: ZipEntry?) : super(e) {}
    }

    fun log(str: String?) {
        LogUtil.log(Log.LEVEL_INFO, BundleBuilderFactory::class.java.getName(), str)
    }
}