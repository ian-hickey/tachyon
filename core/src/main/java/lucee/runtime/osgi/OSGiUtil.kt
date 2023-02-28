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

import java.io.File

object OSGiUtil {
    private const val QUALIFIER_APPENDIX_SNAPSHOT = 1
    private const val QUALIFIER_APPENDIX_BETA = 2
    private const val QUALIFIER_APPENDIX_RC = 3
    private const val QUALIFIER_APPENDIX_OTHER = 4
    private const val QUALIFIER_APPENDIX_STABLE = 5
    private const val MAX_REDIRECTS = 5
    private val bundlesThreadLocal: ThreadLocal<Set<String?>?>? = object : ThreadLocal<Set<String?>?>() {
        @Override
        protected fun initialValue(): Set<String?>? {
            return HashSet<String?>()
        }
    }
    private val JAR_EXT_FILTER: Filter? = Filter()
    private var bootDelegation: Array<String?>?
    private val packageBundleMapping: Map<String?, String?>? = HashMap<String?, String?>()

    /**
     * only installs a bundle, if the bundle does not already exist, if the bundle exists the existing
     * bundle is unloaded first.
     *
     * @param factory
     * @param context
     * @param bundle
     * @return
     * @throws IOException
     * @throws BundleException
     */
    @Throws(IOException::class, BundleException::class)
    fun installBundle(context: BundleContext?, bundle: Resource?, checkExistence: Boolean): Bundle? {
        if (checkExistence) {
            val bf: BundleFile = BundleFile.getInstance(bundle)
            if (!bf.isBundle()) throw BundleException(bundle.toString() + " is not a valid bundle!")
            val existing: Bundle? = loadBundleFromLocal(context, bf.getSymbolicName(), bf.getVersion(), null, false, null)
            if (existing != null) return existing
        }
        return _loadBundle(context, bundle.getAbsolutePath(), bundle.getInputStream(), true)
    }

    /**
     * does not check if the bundle already exists!
     *
     * @param context
     * @param path
     * @param is
     * @param closeStream
     * @return
     * @throws BundleException
     */
    @Throws(BundleException::class)
    private fun _loadBundle(context: BundleContext?, path: String?, `is`: InputStream?, closeStream: Boolean): Bundle? {
        log(Log.LEVEL_DEBUG, "add bundle:$path")
        return try {
            // we make this very simply so an old loader that is calling this still works
            context.installBundle(path, `is`)
        } finally {
            // we make this very simply so an old loader that is calling this still works
            if (closeStream && `is` != null) {
                try {
                    `is`.close()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
        }
    }

    /**
     * only installs a bundle, if the bundle does not already exist, if the bundle exists the existing
     * bundle is unloaded first. the bundle is not stored physically on the system.
     *
     * @param factory
     * @param context
     * @param bundle
     * @return
     * @throws IOException
     * @throws BundleException
     */
    @Throws(IOException::class, BundleException::class)
    fun installBundle(context: BundleContext?, bundleIS: InputStream?, closeStream: Boolean, checkExistence: Boolean): Bundle? {
        // store locally to test the bundle
        val name: String = System.currentTimeMillis().toString() + ".tmp"
        val dir: Resource = SystemUtil.getTempDirectory()
        var tmp: Resource = dir.getRealResource(name)
        var count = 0
        while (tmp.exists()) tmp = dir.getRealResource(count++.toString() + "_" + name)
        IOUtil.copy(bundleIS, tmp, closeStream)
        return try {
            installBundle(context, tmp, checkExistence)
        } finally {
            tmp.delete()
        }
    }

    fun toVersion(version: String?, defaultValue: Version?): Version? {
        if (StringUtil.isEmpty(version)) return defaultValue
        // String[] arr = ListUtil.listToStringArray(version, '.');
        val arr: Array<String?>
        arr = try {
            ListUtil.toStringArrayTrim(ListUtil.listToArray(version.trim(), '.'))
        } catch (e: PageException) {
            return defaultValue // should not happen
        }
        val major: Integer
        val minor: Integer
        val micro: Integer
        val qualifier: String?
        if (arr.size == 1) {
            major = Caster.toInteger(arr[0], null)
            minor = 0
            micro = 0
            qualifier = null
        } else if (arr.size == 2) {
            major = Caster.toInteger(arr[0], null)
            minor = Caster.toInteger(arr[1], null)
            micro = 0
            qualifier = null
        } else if (arr.size == 3) {
            major = Caster.toInteger(arr[0], null)
            minor = Caster.toInteger(arr[1], null)
            micro = Caster.toInteger(arr[2], null)
            qualifier = null
        } else {
            major = Caster.toInteger(arr[0], null)
            minor = Caster.toInteger(arr[1], null)
            micro = Caster.toInteger(arr[2], null)
            qualifier = arr[3]
        }
        return if (major == null || minor == null || micro == null) defaultValue else qualifier?.let { Version(major, minor, micro, it) }
                ?: Version(major, minor, micro)
    }

    @Throws(BundleException::class)
    fun toVersion(version: String?): Version? {
        val v: Version? = toVersion(version, null)
        if (v != null) return v
        throw BundleException(
                "Given version [$version] is invalid, a valid version is following this pattern <major-number>.<minor-number>.<micro-number>[.<qualifier>]")
    }

    @Throws(IOException::class)
    private fun getManifest(bundle: Resource?): Manifest? {
        var `is`: InputStream? = null
        var mf: Manifest? = null
        try {
            `is` = bundle.getInputStream()
            val zis = ZipInputStream(`is`)
            var entry: ZipEntry?
            while (zis.getNextEntry().also { entry = it } != null && mf == null) {
                if ("META-INF/MANIFEST.MF".equals(entry.getName())) {
                    mf = Manifest(zis)
                }
                zis.closeEntry()
            }
        } finally {
            IOUtil.close(`is`)
        }
        return mf
    }
    /*
	 * public static FrameworkFactory getFrameworkFactory() throws Exception { ClassLoader cl =
	 * OSGiUtil.class.getClassLoader(); java.net.URL url =
	 * cl.getResource("META-INF/services/org.osgi.framework.launch.FrameworkFactory"); if (url != null)
	 * { BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream())); try { for
	 * (String s = br.readLine(); s != null; s = br.readLine()) { s = s.trim(); // Try to load first
	 * non-empty, non-commented line. if ((s.length() > 0) && (s.charAt(0) != '#')) { return
	 * (FrameworkFactory) ClassUtil.loadInstance(cl, s); } } } finally { if (br != null) br.close(); } }
	 * throw new Exception("Could not find framework factory."); }
	 */
    /**
     * tries to load a class with ni bundle definition
     *
     * @param name
     * @param version
     * @param id
     * @param startIfNecessary
     * @return
     * @throws BundleException
     */
    fun loadClass(className: String?, defaultValue: Class?): Class? {

        // a class necessary need a package info, otherwise it is useless to search for it
        var className = className
        if (className.indexOf('.') === -1 && className.indexOf('/') === -1 && className.indexOf('\\') === -1) return defaultValue
        className = className.trim()
        val classPath: String = className.replace('.', '/').toString() + ".class"
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val bc: BundleCollection = engine.getBundleCollection()
        // first we try to load the class from the Lucee core
        try {
            // load from core
            if (bc.core.getEntry(classPath) != null) {
                return bc.core.loadClass(className)
            }
        } catch (e: Exception) {
        } // class is not visible to the Lucee core

        // now we check all started bundled (not only bundles used by core)
        val bundles: Array<Bundle?> = bc.getBundleContext().getBundles()
        for (b in bundles) {
            if (b !== bc.core && b.getEntry(classPath) != null) {
                try {
                    return b.loadClass(className)
                } catch (e: Exception) {
                } // class is not visible to that bundle
            }
        }

        // now we check lucee loader (SystemClassLoader?)
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()
        run {
            val cl: ClassLoader = factory.getClass().getClassLoader()
            if (cl.getResource(classPath) != null) {
                try {
                    // print.e("loader:");
                    return cl.loadClass(className)
                } catch (e: Exception) {
                }
            }
        }

        // now we check bundles not loaded
        val loaded: Set<String?> = HashSet<String?>()
        for (b in bundles) {
            loaded.add(b.getSymbolicName().toString() + "|" + b.getVersion())
        }
        try {
            val dir: File = factory.getBundleDirectory()
            val children: Array<File?> = dir.listFiles(JAR_EXT_FILTER)
            var bf: BundleFile
            var bi: Array<String?>?
            for (i in children.indices) {
                try {
                    bi = getBundleInfoFromFileName(children[i].getName())
                    if (bi != null && loaded.contains(bi[0].toString() + "|" + bi[1])) continue
                    bf = BundleFile.getInstance(children[i])
                    if (bf.isBundle() && !loaded.contains(bf.getSymbolicName().toString() + "|" + bf.getVersion()) && bf!!.hasClass(className)) {
                        var b: Bundle? = null
                        try {
                            b = _loadBundle(bc.getBundleContext(), bf!!.getFile())
                        } catch (e: IOException) {
                        }
                        if (b != null) {
                            startIfNecessary(b)
                            if (b.getEntry(classPath) != null) {
                                try {
                                    return b.loadClass(className)
                                } catch (e: Exception) {
                                } // class is not visible to that bundle
                            }
                        }
                    }
                } catch (t2: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t2)
                }
            }
        } catch (t1: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t1)
        }
        return defaultValue
    }

    fun getBundleInfoFromFileName(name: String?): Array<String?>? {
        var name = name
        name = ResourceUtil.removeExtension(name, name)
        val index: Int = name.indexOf('-')
        return if (index == -1) null else arrayOf(name.substring(0, index), name.substring(index + 1))
    }

    fun loadBundle(bf: BundleFile?, defaultValue: Bundle?): Bundle? {
        return if (!bf.isBundle()) defaultValue else try {
            loadBundle(bf)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Throws(IOException::class, BundleException::class)
    fun loadBundle(bf: BundleFile?): Bundle? {
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()

        // check in loaded bundles
        val bc: BundleContext = engine.getBundleContext()
        val bundles: Array<Bundle?> = bc.getBundles()
        for (b in bundles) {
            if (bf.getSymbolicName().equals(b.getSymbolicName())) {
                if (b.getVersion().equals(bf.getVersion())) return b
            }
        }
        return _loadBundle(bc, bf!!.getFile())
    }

    @Throws(BundleException::class, IOException::class)
    fun loadBundleByPackage(pq: PackageQuery?, loadedBundles: Set<Bundle?>?, startIfNecessary: Boolean, parents: Set<String?>?): Bundle? {
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()

        // if part of bootdelegation we ignore
        if (isPackageInBootelegation(pq!!.name)) {
            return null
        }

        // is it in jar directory but not loaded
        val dir: File = factory.getBundleDirectory()
        val children: Array<File?> = dir.listFiles(JAR_EXT_FILTER)
        var pds: List<PackageDefinition?>?
        for (child in children) {
            val bf: BundleFile = BundleFile.getInstance(child)
            if (bf.isBundle()) {
                if (parents!!.contains(toString(bf))) continue
                pds = toPackageDefinitions(bf.getExportPackage(), pq.name, pq.versionDefinitons)
                if (pds != null && !pds.isEmpty()) {
                    var b: Bundle? = exists(loadedBundles, bf)
                    if (b != null) {
                        if (startIfNecessary) _startIfNecessary(b, parents)
                        return null
                    }
                    b = loadBundle(bf)
                    if (b != null) {
                        loadedBundles.add(b)
                        if (startIfNecessary) _startIfNecessary(b, parents)
                        return b
                    }
                }
            }
        }
        val bn = packageBundleMapping!![pq.name]
        if (!StringUtil.isEmpty(bn)) {
            try {
                return loadBundle(bn, null, null, null, startIfNecessary, false, pq.isRequired, if (pq.isRequired) null else Boolean.FALSE)
            } catch (be: BundleException) {
                if (pq.isRequired) throw be
            }
        }
        for (e in packageBundleMapping.entrySet()) {
            if (pq.name.startsWith(e.getKey().toString() + ".")) {
                try {
                    return loadBundle(e.getValue(), null, null, null, startIfNecessary, false, pq.isRequired, if (pq.isRequired) null else Boolean.FALSE)
                } catch (be: BundleException) {
                    if (pq.isRequired) throw be
                }
            }
        }
        return null
    }

    private fun toString(bf: BundleFile?): Object? {
        return bf.getSymbolicName().toString() + ":" + bf.getVersionAsString()
    }

    private fun exists(loadedBundles: Set<Bundle?>?, bf: BundleFile?): Bundle? {
        if (loadedBundles != null) {
            var b: Bundle
            val it: Iterator<Bundle?> = loadedBundles.iterator()
            while (it.hasNext()) {
                b = it.next()
                if (b.getSymbolicName().equals(bf.getSymbolicName()) && b.getVersion().equals(bf.getVersion())) return b
            }
        }
        return null
    }

    private fun exists(loadedBundles: Set<Bundle?>?, bd: BundleDefinition?): Bundle? {
        if (loadedBundles != null) {
            var b: Bundle
            val it: Iterator<Bundle?> = loadedBundles.iterator()
            while (it.hasNext()) {
                b = it.next()
                if (b.getSymbolicName().equals(bd!!.name) && b.getVersion().equals(bd.version)) return b
            }
        }
        return null
    }

    @Throws(BundleException::class)
    fun loadBundle(name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, startIfNecessary: Boolean): Bundle? {
        return loadBundle(name, version, id, addional, startIfNecessary, false, true, null)
    }

    @Throws(BundleException::class)
    fun loadBundle(name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, startIfNecessary: Boolean, versionOnlyMattersForDownload: Boolean): Bundle? {
        return loadBundle(name, version, id, addional, startIfNecessary, versionOnlyMattersForDownload, true, null)
    }

    @Throws(BundleException::class)
    fun loadBundle(name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, startIfNecessary: Boolean, versionOnlyMattersForDownload: Boolean,
                   downloadIfNecessary: Boolean): Bundle? {
        return loadBundle(name, version, id, addional, startIfNecessary, versionOnlyMattersForDownload, versionOnlyMattersForDownload, null)
    }

    @Throws(BundleException::class)
    fun loadBundle(name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, startIfNecessary: Boolean, versionOnlyMattersForDownload: Boolean,
                   downloadIfNecessary: Boolean, printExceptions: Boolean?): Bundle? {
        return try {
            _loadBundle(name, version, id, addional, startIfNecessary, null, versionOnlyMattersForDownload, downloadIfNecessary, printExceptions)
        } catch (sfe: StartFailedException) {
            throw sfe.bundleException
        }
    }

    @Throws(BundleException::class, StartFailedException::class)
    fun _loadBundle(name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, startIfNecessary: Boolean, parents: Set<String?>?,
                    versionOnlyMattersForDownload: Boolean, downloadIfNecessary: Boolean, printExceptions: Boolean?): Bundle? {
        var name = name
        var printExceptions = printExceptions
        name = name.trim()
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()
        val arrVersionMatters = if (versionOnlyMattersForDownload && version != null) booleanArrayOf(true, false) else booleanArrayOf(true)

        // check in loaded bundles
        val bc: BundleContext = engine.getBundleContext()
        val bundles: Array<Bundle?> = bc.getBundles()
        val versionsFound = StringBuilder()
        for (versionMatters in arrVersionMatters) {
            for (b in bundles) {
                if (name.equalsIgnoreCase(b.getSymbolicName())) {
                    if (version == null || !versionMatters || version.equals(b.getVersion())) {
                        if (startIfNecessary) {
                            try {
                                _startIfNecessary(b, parents)
                            } catch (be: BundleException) {
                                throw StartFailedException(be, b)
                            }
                        }
                        return b
                    }
                    if (versionsFound.length() > 0) versionsFound.append(", ")
                    versionsFound.append(b.getVersion().toString())
                }
            }
        }

        // is it in jar directory but not loaded
        var bf: BundleFile? = _getBundleFile(factory, name, version, addional, versionsFound)
        if (versionOnlyMattersForDownload && (bf == null || !bf.isBundle())) bf = _getBundleFile(factory, name, null, addional, versionsFound)
        if (bf != null && bf.isBundle()) {
            var b: Bundle? = null
            try {
                b = _loadBundle(bc, bf.getFile())
            } catch (e: IOException) {
                LogUtil.log(ThreadLocalPageContext.get(), OSGiUtil::class.java.getName(), e)
            }
            if (b != null) {
                if (startIfNecessary) {
                    try {
                        startIfNecessary(b)
                    } catch (be: BundleException) {
                        throw StartFailedException(be, b)
                    }
                }
                return b
            }
        }

        // if not found try to download
        if (downloadIfNecessary) {
            try {
                val b: Bundle?
                if (version != null) {
                    val f: File = factory.downloadBundle(name, version.toString(), id)
                    b = _loadBundle(bc, f)
                } else {
                    // MUST find out why this breaks at startup with commandbox if version exists
                    val r: Resource? = downloadBundle(factory, name, null, id)
                    b = _loadBundle(bc, r)
                }
                if (startIfNecessary) {
                    try {
                        _start(b, parents)
                    } catch (be: BundleException) {
                        throw StartFailedException(be, b)
                    }
                }
                return b
            } catch (e: Exception) {
                log(e)
            }
        }
        var localDir = ""
        try {
            localDir = " (" + factory.getBundleDirectory().toString() + ")"
        } catch (e: IOException) {
        }
        var upLoc = ""
        try {
            upLoc = " (" + factory.getUpdateLocation().toString() + ")"
        } catch (e: IOException) {
        }
        var bundleError = ""
        val parentBundle = if (parents == null) " " else String.join(",", parents)
        val downloadText = if (downloadIfNecessary) " or from the update provider [$upLoc]" else ""
        bundleError = if (versionsFound.length() > 0) {
            ("The OSGi Bundle with name [" + name + "] for [" + parentBundle + "] is not available in version [" + version + "] locally [" + localDir + "]"
                    + downloadText + ", the following versions are available locally [" + versionsFound + "].")
        } else if (version != null) {
            ("The OSGi Bundle with name [" + name + "] in version [" + version + "] for [" + parentBundle + "] is not available locally [" + localDir + "]"
                    + downloadText + ".")
        } else {
            "The OSGi Bundle with name [$name] for [$parentBundle] is not available locally [$localDir]$downloadText."
        }
        if (printExceptions == null) printExceptions = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cli.printExceptions", null), false)
        try {
            throw BundleException(bundleError)
        } catch (be: BundleException) {
            if (printExceptions) be.printStackTrace()
            throw be
        }
    }

    @Throws(IOException::class, BundleException::class)
    private fun downloadBundle(factory: CFMLEngineFactory?, symbolicName: String?, symbolicVersion: String?, id: Identification?): Resource? {
        var symbolicVersion = symbolicVersion
        if (!Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.enable.bundle.download", null), true)) {
            val printExceptions: Boolean = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cli.printExceptions", null), false)
            val bundleError = ("Lucee is missing the Bundle jar [" + (if (symbolicVersion != null) symbolicName.toString() + ":" + symbolicVersion else symbolicName)
                    + "], and has been prevented from downloading it. If this jar is not a core jar,"
                    + " it will need to be manually downloaded and placed in the {{lucee-server}}/context/bundles directory.")
            try {
                throw RuntimeException(bundleError)
            } catch (re: RuntimeException) {
                if (printExceptions) re.printStackTrace()
                throw re
            }
        }
        val jarDir: Resource = ResourceUtil.toResource(factory.getBundleDirectory())
        val updateProvider: URL = factory.getUpdateLocation()
        if (symbolicVersion == null) symbolicVersion = "latest"
        val updateUrl = URL(updateProvider, "/rest/update/provider/download/" + symbolicName + "/" + symbolicVersion + "/" + (if (id != null) id.toQueryString() else "")
                + (if (id == null) "?" else "&") + "allowRedirect=true"
        )
        log(Logger.LOG_INFO, "Downloading bundle [$symbolicName:$symbolicVersion] from [$updateUrl]")
        var code: Int
        var conn: HttpURLConnection
        try {
            conn = updateUrl.openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")
            conn.setConnectTimeout(10000)
            conn.connect()
            code = conn.getResponseCode()
        } catch (e: UnknownHostException) {
            throw IOException("Downloading the bundle  [$symbolicName:$symbolicVersion] from [$updateUrl] failed", e)
        }
        // the update provider is not providing a download for this
        if (code != 200) {
            var count = 1
            // the update provider can also provide a different (final) location for this
            while ((code == 301 || code == 302) && count++ <= MAX_REDIRECTS) {
                var location: String = conn.getHeaderField("Location")
                // just in case we check invalid names
                if (location == null) location = conn.getHeaderField("location")
                if (location == null) location = conn.getHeaderField("LOCATION")
                LogUtil.log(Log.LEVEL_INFO, OSGiUtil::class.java.getName(), "Download redirected: $location") // MUST remove
                conn.disconnect()
                val url = URL(location)
                try {
                    conn = url.openConnection() as HttpURLConnection
                    conn.setRequestMethod("GET")
                    conn.setConnectTimeout(10000)
                    conn.connect()
                    code = conn.getResponseCode()
                } catch (e: UnknownHostException) {
                    log(e)
                    throw IOException("Failed to download the bundle  [$symbolicName:$symbolicVersion] from [$location]", e)
                }
            }

            // no download available!
            if (code != 200) {
                val msg = ("Download bundle failed for [" + symbolicName + "] in version [" + symbolicVersion + "] from [" + updateUrl
                        + "], please download manually and copy to [" + jarDir + "]")
                log(Logger.LOG_ERROR, msg)
                conn.disconnect()
                throw IOException(msg)
            }
        }

        // extract version if necessary
        return if ("latest".equals(symbolicVersion)) {
            // copy to temp file
            val temp: Resource = SystemUtil.getTempFile("jar", false)
            IOUtil.copy(conn.getContent() as InputStream, temp, true)
            try {
                conn.disconnect()

                // extract version and create file with correct name
                val bf: BundleFile = BundleFile.getInstance(temp)
                val jar: Resource = jarDir.getRealResource(symbolicName.toString() + "-" + bf.getVersionAsString() + ".jar")
                IOUtil.copy(temp, jar)
                jar
            } finally {
                temp.delete()
            }
        } else {
            val jar: Resource = jarDir.getRealResource(symbolicName.toString() + "-" + symbolicVersion + ".jar")
            IOUtil.copy(conn.getContent() as InputStream, jar, true)
            conn.disconnect()
            jar
        }
    }

    private fun toPackageDefinitions(str: String?, filterPackageName: String?, versionDefinitions: List<VersionDefinition?>?): List<PackageDefinition?>? {
        if (StringUtil.isEmpty(str)) return null
        val st = StringTokenizer(str, ",")
        val list: List<PackageDefinition?> = ArrayList<PackageDefinition?>()
        var pd: PackageDefinition?
        while (st.hasMoreTokens()) {
            pd = toPackageDefinition(st.nextToken().trim(), filterPackageName, versionDefinitions)
            if (pd != null) list.add(pd)
        }
        return list
    }

    private fun toPackageDefinition(str: String?, filterPackageName: String?, versionDefinitions: List<VersionDefinition?>?): PackageDefinition? {
        // first part is the package
        val list: StringList = ListUtil.toList(str, ';')
        var pd: PackageDefinition? = null
        var token: String
        var v: Version?
        while (list.hasNext()) {
            token = list.next().trim()
            if (pd == null) {
                if (!token.equals(filterPackageName)) return null
                pd = PackageDefinition(token)
            } else {
                val entry: StringList = ListUtil.toList(token, '=')
                if (entry.size() === 2 && entry.next().trim().equalsIgnoreCase("version")) {
                    val version: String = StringUtil.unwrap(entry.next().trim())
                    if (!version.equals("0.0.0")) {
                        v = toVersion(version, null)
                        if (v != null) {
                            if (versionDefinitions != null) {
                                val it = versionDefinitions.iterator()
                                while (it.hasNext()) {
                                    if (!it.next()!!.matches(v)) {
                                        return null
                                    }
                                }
                            }
                            pd.setVersion(v)
                        }
                    }
                }
            }
        }
        return pd
    }

    /**
     * this should be used when you not want to load a Bundle to the system
     *
     * @param name
     * @param version
     * @param id only necessary if downloadIfNecessary is set to true
     * @param downloadIfNecessary
     * @return
     * @throws BundleException
     */
    @Throws(BundleException::class)
    fun getBundleFile(name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, downloadIfNecessary: Boolean): BundleFile? {
        var name = name
        name = name.trim()
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()
        val versionsFound = StringBuilder()

        // is it in jar directory but not loaded
        var bf: BundleFile? = _getBundleFile(factory, name, version, addional, versionsFound)
        if (bf != null) return bf

        // if not found try to download
        if (downloadIfNecessary && version != null) {
            try {
                bf = BundleFile.getInstance(factory.downloadBundle(name, version.toString(), id))
                if (bf!!.isBundle()) return bf
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        if (versionsFound.length() > 0) throw BundleException("The OSGi Bundle with name [" + name + "] is not available in version [" + version
                + "] locally or from the update provider, the following versions are available locally [" + versionsFound + "].")
        if (version != null) throw BundleException("The OSGi Bundle with name [$name] in version [$version] is not available locally or from the update provider.")
        throw BundleException("The OSGi Bundle with name [$name] is not available locally or from the update provider.")
    }

    /**
     * check left value against right value
     *
     * @param left
     * @param right
     * @return returns if right is newer than left
     */
    fun isNewerThan(left: Version?, right: Version?): Boolean {

        // major
        if (left.getMajor() > right.getMajor()) return true
        if (left.getMajor() < right.getMajor()) return false

        // minor
        if (left.getMinor() > right.getMinor()) return true
        if (left.getMinor() < right.getMinor()) return false

        // micro
        if (left.getMicro() > right.getMicro()) return true
        if (left.getMicro() < right.getMicro()) return false

        // qualifier
        // left
        var q: String = left.getQualifier()
        var index: Int = q.indexOf('-')
        val qla = if (index == -1) "" else q.substring(index + 1).trim()
        val qln = if (index == -1) q else q.substring(0, index)
        val ql: Int = if (StringUtil.isEmpty(qln)) Integer.MIN_VALUE else Caster.toIntValue(qln, Integer.MAX_VALUE)

        // right
        q = right.getQualifier()
        index = q.indexOf('-')
        val qra = if (index == -1) "" else q.substring(index + 1).trim()
        val qrn = if (index == -1) q else q.substring(0, index)
        val qr: Int = if (StringUtil.isEmpty(qln)) Integer.MIN_VALUE else Caster.toIntValue(qrn, Integer.MAX_VALUE)
        if (ql > qr) return true
        if (ql < qr) return false
        val qlan = qualifierAppendix2Number(qla)
        val qran = qualifierAppendix2Number(qra)
        if (qlan > qran) return true
        if (qlan < qran) return false
        return if (qlan == QUALIFIER_APPENDIX_OTHER && qran == QUALIFIER_APPENDIX_OTHER) left.compareTo(right) > 0 else false
    }

    private fun qualifierAppendix2Number(str: String?): Int {
        if (Util.isEmpty(str, true)) return QUALIFIER_APPENDIX_STABLE
        if ("SNAPSHOT".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_SNAPSHOT
        if ("BETA".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_BETA
        return if ("RC".equalsIgnoreCase(str)) QUALIFIER_APPENDIX_RC else QUALIFIER_APPENDIX_OTHER
    }

    fun getBundleFile(name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, downloadIfNecessary: Boolean, defaultValue: BundleFile?): BundleFile? {
        var name = name
        name = name.trim()
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()
        val versionsFound = StringBuilder()

        // is it in jar directory but not loaded
        var bf: BundleFile? = _getBundleFile(factory, name, version, addional, versionsFound)
        if (bf != null) return bf

        // if not found try to download
        if (downloadIfNecessary && version != null) {
            try {
                bf = BundleFile.getInstance(factory.downloadBundle(name, version.toString(), id))
                if (bf!!.isBundle()) return bf
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        return defaultValue
    }

    private fun _getBundleFile(factory: CFMLEngineFactory?, name: String?, version: Version?, addional: List<Resource?>?, versionsFound: StringBuilder?): BundleFile? {
        var match: Resource? = null
        try {
            val dir: Resource = ResourceUtil.toResource(factory.getBundleDirectory())
            // first we check if there is a file match (fastest solution)
            if (version != null) {
                val jars: List<Resource?>? = createPossibleNameMatches(dir, addional, name, version)
                for (jar in jars!!) {
                    if (jar.isFile()) {
                        match = jar
                        val bf: BundleFile = BundleFile.getInstance(jar)
                        if (bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
                            if (version.equals(bf.getVersion())) {
                                return bf
                            }
                        }
                    }
                }
            }
            val children: List<Resource?>? = listFiles(dir, addional, JAR_EXT_FILTER)
            // now we make a closer filename test
            var curr: String
            if (version != null) {
                match = null
                val v: String = version.toString()
                for (child in children!!) {
                    curr = child.getName()
                    if (curr.equalsIgnoreCase(name.toString() + "-" + v.replace('-', '.')) || curr.equalsIgnoreCase(name.replace('.', '-').toString() + "-" + v)
                            || curr.equalsIgnoreCase(name.replace('.', '-').toString() + "-" + v.replace('.', '-'))
                            || curr.equalsIgnoreCase(name.replace('.', '-').toString() + "-" + v.replace('-', '.')) || curr.equalsIgnoreCase(name.replace('-', '.').toString() + "-" + v)
                            || curr.equalsIgnoreCase(name.replace('-', '.').toString() + "-" + v.replace('.', '-'))
                            || curr.equalsIgnoreCase(name.replace('-', '.').toString() + "-" + v.replace('-', '.'))) {
                        match = child
                        break
                    }
                }
                if (match != null) {
                    val bf: BundleFile = BundleFile.getInstance(match)
                    if (bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
                        if (version.equals(bf.getVersion())) {
                            return bf
                        }
                    }
                }
            } else {
                val matches: List<BundleFile?> = ArrayList<BundleFile?>()
                var bf: BundleFile?
                for (child in children!!) {
                    curr = child.getName()
                    if (curr.startsWith(name.toString() + "-") || curr.startsWith(name.replace('-', '.').toString() + "-") || curr.startsWith(name.replace('.', '-').toString() + "-")) {
                        match = child
                        bf = BundleFile.getInstance(child)
                        if (bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
                            matches.add(bf)
                        }
                    }
                }
                if (!matches.isEmpty()) {
                    bf = null
                    var _bf: BundleFile
                    val it: Iterator<BundleFile?> = matches.iterator()
                    while (it.hasNext()) {
                        _bf = it.next()
                        if (bf == null || isNewerThan(_bf.getVersion(), bf.getVersion())) bf = _bf
                    }
                    if (bf != null) {
                        return bf
                    }
                }
            }

            // now we check by Manifest comparsion
            var bf: BundleFile
            for (child in children) {
                match = child
                bf = BundleFile.getInstance(child)
                if (bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
                    if (version == null || version.equals(bf.getVersion())) {
                        return bf
                    }
                    if (versionsFound != null) {
                        if (versionsFound.length() > 0) versionsFound.append(", ")
                        versionsFound.append(bf.getVersionAsString())
                    }
                }
            }
        } catch (e: Exception) {
            log(e)
            if (match != null) {
                if (FileUtil.isLocked(match)) {
                    log(Log.LEVEL_ERROR, "cannot load the bundle [$match], bundle seem to have a windows lock")

                    // in case the file exists, but is locked we create a copy of if and use that copy
                    val bf: BundleFile
                    try {
                        bf = BundleFile.getInstance(FileUtil.createTempResourceFromLockedResource(match, false))
                        if (bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
                            if (version.equals(bf.getVersion())) {
                                return bf
                            }
                        }
                    } catch (e1: Exception) {
                        log(e1)
                    }
                }
            }
        }
        return null
    }

    private fun createPossibleNameMatches(dir: Resource?, addional: List<Resource?>?, name: String?, version: Version?): List<Resource?>? {
        val patterns = arrayOf<String?>(name.toString() + "-" + version.toString() + ".jar", name.toString() + "-" + version.toString().replace('.', '-') + ".jar",
                name.replace('.', '-').toString() + "-" + version.toString().replace('.', '-') + ".jar")
        val resources: List<Resource?> = ArrayList<Resource?>()
        for (pattern in patterns) {
            resources.add(dir.getRealResource(pattern))
        }
        if (addional != null && !addional.isEmpty()) {
            val it: Iterator<Resource?> = addional.iterator()
            var res: Resource?
            while (it.hasNext()) {
                res = it.next()
                if (res.isDirectory()) {
                    for (pattern in patterns) {
                        resources.add(res.getRealResource(pattern))
                    }
                } else if (res.isFile()) {
                    for (pattern in patterns) {
                        if (pattern.equalsIgnoreCase(res.getName()));
                        resources.add(res)
                    }
                }
            }
        }
        return resources
    }

    private fun listFiles(dir: Resource?, addional: List<Resource?>?, filter: Filter?): List<Resource?>? {
        val children: List<Resource?> = ArrayList<Resource?>()
        _add(children, dir.listResources(filter))
        if (addional != null && !addional.isEmpty()) {
            val it: Iterator<Resource?> = addional.iterator()
            var res: Resource?
            while (it.hasNext()) {
                res = it.next()
                if (res.isDirectory()) {
                    _add(children, res.listResources(filter))
                } else if (res.isFile()) {
                    if (filter.accept(res, res.getName())) children.add(res)
                }
            }
        }
        return children
    }

    private fun _add(children: List<Resource?>?, reses: Array<Resource?>?) {
        if (reses == null || reses.size == 0) return
        for (res in reses) {
            children.add(res)
        }
    }

    /**
     * get all local bundles (even bundles not loaded/installed)
     *
     * @param name
     * @param version
     * @return
     */
    val bundleDefinitions: List<BundleDefinition?>?
        get() {
            val engine: CFMLEngine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig())
            return getBundleDefinitions(engine.getBundleContext())
        }

    fun getBundleDefinitions(bc: BundleContext?): List<BundleDefinition?>? {
        val set: Set<String?> = HashSet()
        val list: List<BundleDefinition?> = ArrayList()
        val bundles: Array<Bundle?> = bc.getBundles()
        for (b in bundles) {
            list.add(BundleDefinition(b))
            set.add(b.getSymbolicName().toString() + ":" + b.getVersion())
        }
        // is it in jar directory but not loaded
        val engine: CFMLEngine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig())
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()
        try {
            val children: Array<File?> = factory.getBundleDirectory().listFiles(JAR_EXT_FILTER)
            var bf: BundleFile
            for (i in children.indices) {
                try {
                    bf = BundleFile.getInstance(children[i])
                    if (bf.isBundle() && !set.contains(bf.getSymbolicName().toString() + ":" + bf.getVersion())) list.add(BundleDefinition(bf.getSymbolicName(), bf.getVersion()))
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
        } catch (ioe: IOException) {
        }
        return list
    }

    fun getBundleLoaded(name: String?, version: Version?, defaultValue: Bundle?): Bundle? {
        val engine: CFMLEngine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig())
        return getBundleLoaded(engine.getBundleContext(), name, version, defaultValue)
    }

    fun getBundleLoaded(bc: BundleContext?, name: String?, version: Version?, defaultValue: Bundle?): Bundle? {
        var name = name
        name = name.trim()
        val bundles: Array<Bundle?> = bc.getBundles()
        for (b in bundles) {
            if (name.equalsIgnoreCase(b.getSymbolicName())) {
                if (version == null || version.equals(b.getVersion())) {
                    return b
                }
            }
        }
        return defaultValue
    }

    fun loadBundleFromLocal(name: String?, version: Version?, addional: List<Resource?>?, loadIfNecessary: Boolean, defaultValue: Bundle?): Bundle? {
        val engine: CFMLEngine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig())
        return loadBundleFromLocal(engine.getBundleContext(), name, version, addional, loadIfNecessary, defaultValue)
    }

    fun loadBundleFromLocal(bc: BundleContext?, name: String?, version: Version?, addional: List<Resource?>?, loadIfNecessary: Boolean, defaultValue: Bundle?): Bundle? {
        var name = name
        name = name.trim()
        val bundles: Array<Bundle?> = bc.getBundles()
        for (b in bundles) {
            if (name.equalsIgnoreCase(b.getSymbolicName())) {
                if (version == null || version.equals(b.getVersion())) {
                    return b
                }
            }
        }
        if (!loadIfNecessary) return defaultValue

        // is it in jar directory but not loaded
        val engine: CFMLEngine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig())
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()
        val bf: BundleFile? = _getBundleFile(factory, name, version, addional, null)
        if (bf != null) {
            try {
                return _loadBundle(bc, bf.getFile())
            } catch (e: Exception) {
            }
        }
        return defaultValue
    }

    /**
     * get local bundle, but does not download from update provider!
     *
     * @param name
     * @param version
     * @return
     * @throws BundleException
     */
    @Throws(BundleException::class)
    fun removeLocalBundle(name: String?, version: Version?, addional: List<Resource?>?, removePhysical: Boolean, doubleTap: Boolean) {
        var name = name
        name = name.trim()
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()

        // first we look for an active bundle and do stop it
        val b: Bundle? = getBundleLoaded(name, version, null)
        if (b != null) {
            stopIfNecessary(b)
            b.uninstall()
        }
        if (!removePhysical) return

        // now we remove the file
        val bf: BundleFile? = _getBundleFile(factory, name, version, null, null)
        if (bf != null) {
            if (!bf.getFile().delete() && doubleTap) bf.getFile().deleteOnExit()
        }
    }

    fun removeLocalBundleSilently(name: String?, version: Version?, addional: List<Resource?>?, removePhysical: Boolean) {
        try {
            removeLocalBundle(name, version, addional, removePhysical, true)
        } catch (e: Exception) {
        }
    }

    // bundle stuff
    @Throws(BundleException::class)
    fun startIfNecessary(bundles: Array<Bundle?>?) {
        for (b in bundles!!) {
            startIfNecessary(b)
        }
    }

    @Throws(BundleException::class)
    fun startIfNecessary(bundle: Bundle?): Bundle? {
        return _startIfNecessary(bundle, null)
    }

    @Throws(BundleException::class)
    private fun _startIfNecessary(bundle: Bundle?, parents: Set<String?>?): Bundle? {
        return if (bundle.getState() === Bundle.ACTIVE) bundle else _start(bundle, parents)
    }

    @Throws(BundleException::class)
    fun start(bundle: Bundle?): Bundle? {
        return try {
            _start(bundle, null)
        } finally {
            bundlesThreadLocal.get().clear()
        }
    }

    @Throws(BundleException::class)
    fun _start(bundle: Bundle?, parents: Set<String?>?): Bundle? {
        var parents = parents
        if (bundle == null) return bundle
        val bn: String = toString(bundle)
        if (bundlesThreadLocal.get().contains(bn)) return bundle
        bundlesThreadLocal.get().add(bn)
        val fh: String = bundle.getHeaders().get("Fragment-Host")
        // Fragment cannot be started
        if (!Util.isEmpty(fh)) {
            log(Log.LEVEL_DEBUG, "Do not start [" + bundle.getSymbolicName().toString() + "], because this is a fragment bundle for [" + fh + "]")
            return bundle
        }
        log(Log.LEVEL_DEBUG, "Start bundle: [" + bundle.getSymbolicName().toString() + ":" + bundle.getVersion().toString().toString() + "]")
        try {
            BundleUtil.start(bundle)
        } catch (be: BundleException) {
            // check if required related bundles are missing and load them if necessary
            val failedBD: List<BundleDefinition?> = ArrayList<BundleDefinition?>()
            if (parents == null) parents = HashSet<String?>()
            val loadedBundles: Set<Bundle?>? = loadBundles(parents, bundle, null, failedBD)
            try {
                // startIfNecessary(loadedBundles.toArray(new Bundle[loadedBundles.size()]));
                BundleUtil.start(bundle)
            } catch (be2: BundleException) {
                val listPackages = getRequiredPackages(bundle)
                val failedPD: List<PackageQuery?> = ArrayList<PackageQuery?>()
                loadPackages(parents, loadedBundles, listPackages, bundle, failedPD)
                try {
                    // startIfNecessary(loadedBundles.toArray(new Bundle[loadedBundles.size()]));
                    BundleUtil.start(bundle)
                } catch (be3: BundleException) {
                    if (failedBD.size() > 0) {
                        val itt = failedBD.iterator()
                        var _bd: BundleDefinition?
                        val sb = StringBuilder("Lucee was not able to download/load the following bundles [")
                        while (itt.hasNext()) {
                            _bd = itt.next()
                            sb.append(_bd!!.name.toString() + ":" + _bd.versionAsString).append(';')
                        }
                        sb.append("]")
                        throw BundleException(be2.getMessage() + sb, be2.getCause())
                    }
                    throw be3
                }
            }
        }
        return bundle
    }

    private fun loadPackages(parents: Set<String?>?, loadedBundles: Set<Bundle?>?, listPackages: List<PackageQuery?>?, bundle: Bundle?,
                             failedPD: List<PackageQuery?>?) {
        var pq: PackageQuery
        val it = listPackages!!.iterator()
        parents.add(toString(bundle))
        while (it.hasNext()) {
            pq = it.next()
            try {
                loadBundleByPackage(pq, loadedBundles, true, parents)
            } catch (_be: Exception) {
                failedPD.add(pq)
                // log(_be);
            }
        }
    }

    @Throws(BundleException::class)
    private fun loadBundles(parents: Set<String?>?, bundle: Bundle?, addional: List<Resource?>?, failedBD: List<BundleDefinition?>?): Set<Bundle?>? {
        val loadedBundles: Set<Bundle?> = HashSet<Bundle?>()
        loadedBundles.add(bundle)
        parents.add(toString(bundle))
        val listBundles = getRequiredBundles(bundle)
        var b: Bundle?
        var bd: BundleDefinition
        val it = listBundles!!.iterator()
        var secondChance: List<StartFailedException?>? = null
        while (it.hasNext()) {
            bd = it.next()
            b = exists(loadedBundles, bd)
            if (b != null) {
                _startIfNecessary(b, parents)
                continue
            }
            try {
                // if(parents==null) parents=new HashSet<Bundle>();
                b = _loadBundle(bd.name, bd.version, ThreadLocalPageContext.getConfig().getIdentification(), addional, true, parents, false, true, null)
                loadedBundles.add(b)
            } catch (sfe: StartFailedException) {
                sfe.setBundleDefinition(bd)
                if (secondChance == null) secondChance = ArrayList<StartFailedException?>()
                secondChance.add(sfe)
            } catch (_be: BundleException) {
                // if(failedBD==null) failedBD=new ArrayList<OSGiUtil.BundleDefinition>();
                failedBD.add(bd)
                log(_be)
            }
        }
        // we do this because it maybe was relaying on other bundles now loaded
        // TODO rewrite the complete impl so didd is not necessary
        if (secondChance != null) {
            val _it: Iterator<StartFailedException?> = secondChance.iterator()
            var sfe: StartFailedException?
            while (_it.hasNext()) {
                sfe = _it.next()
                try {
                    _startIfNecessary(sfe!!.bundle, parents)
                    loadedBundles.add(sfe!!.bundle)
                } catch (_be: BundleException) {
                    // if(failedBD==null) failedBD=new ArrayList<OSGiUtil.BundleDefinition>();
                    failedBD.add(sfe.getBundleDefinition())
                    log(_be)
                }
            }
        }
        return loadedBundles
    }

    private fun toString(b: Bundle?): String? {
        return b.getSymbolicName().toString() + ":" + b.getVersion().toString()
    }

    @Throws(BundleException::class)
    fun stopIfNecessary(bundle: Bundle?) {
        if (isFragment(bundle) || bundle.getState() !== Bundle.ACTIVE) return
        stop(bundle)
    }

    @Throws(BundleException::class)
    fun stop(b: Bundle?) {
        b.stop()
    }

    @Throws(BundleException::class)
    fun uninstall(b: Bundle?) {
        b.uninstall()
    }

    fun isFragment(bundle: Bundle?): Boolean {
        return bundle.adapt(BundleRevision::class.java).getTypes() and BundleRevision.TYPE_FRAGMENT !== 0
    }

    fun isFragment(bf: BundleFile?): Boolean {
        return !StringUtil.isEmpty(bf.getFragementHost(), true)
    }

    @Throws(BundleException::class)
    fun getRequiredBundles(bundle: Bundle?): List<BundleDefinition?>? {
        val rtn: List<BundleDefinition?> = ArrayList<BundleDefinition?>()
        val br: BundleRevision = bundle.adapt(BundleRevision::class.java)
        val requirements: List<Requirement?> = br.getRequirements(null)
        val it: Iterator<Requirement?> = requirements.iterator()
        var r: Requirement?
        var e: Entry<String?, String?>
        var value: String
        var name: String
        var index: Int
        var start: Int
        var end: Int
        var op: Int
        var bd: BundleDefinition
        while (it.hasNext()) {
            r = it.next()
            val iit: Iterator<Entry<String?, String?>?> = r.getDirectives().entrySet().iterator()
            while (iit.hasNext()) {
                e = iit.next()
                if (!"filter".equals(e.getKey())) continue
                value = e.getValue()
                // name
                index = value.indexOf("(osgi.wiring.bundle")
                if (index == -1) continue
                start = value.indexOf('=', index)
                end = value.indexOf(')', index)
                if (start == -1 || end == -1 || end < start) continue
                name = value.substring(start + 1, end).trim()
                rtn.add(BundleDefinition(name).also { bd = it })

                // version
                op = -1
                index = value.indexOf("(bundle-version")
                if (index == -1) continue
                end = value.indexOf(')', index)
                start = value.indexOf("<=", index)
                if (start != -1 && start < end) {
                    op = VersionDefinition.LTE
                    start += 2
                } else {
                    start = value.indexOf(">=", index)
                    if (start != -1 && start < end) {
                        op = VersionDefinition.GTE
                        start += 2
                    } else {
                        start = value.indexOf("=", index)
                        if (start != -1 && start < end) {
                            op = VersionDefinition.EQ
                            start++
                        }
                    }
                }
                if (op == -1 || start == -1 || end == -1 || end < start) continue
                bd.setVersion(op, value.substring(start, end).trim())
            }
        }
        return rtn
    }

    @Throws(BundleException::class)
    fun getRequiredPackages(bundle: Bundle?): List<PackageQuery?>? {
        val rtn: List<PackageQuery?> = ArrayList<PackageQuery?>()
        val br: BundleRevision = bundle.adapt(BundleRevision::class.java)
        val requirements: List<Requirement?> = br.getRequirements(null)
        val it: Iterator<Requirement?> = requirements.iterator()
        var r: Requirement?
        var e: Entry<String?, String?>
        var valued: String
        var pd: PackageQuery?
        var res = PackageQuery.RESOLUTION_NONE
        while (it.hasNext()) {
            r = it.next()
            val iit: Iterator<Entry<String?, String?>?> = r.getDirectives().entrySet().iterator()
            pd = null
            inner@ while (iit.hasNext()) {
                e = iit.next()
                if ("filter".equals(e.getKey())) {
                    pd = toPackageQuery(e.getValue())
                }
                if ("resolution".equals(e.getKey())) {
                    res = PackageQuery.toResolution(e.getValue(), PackageQuery.RESOLUTION_NONE)
                }
            }
            if (pd != null) {
                if (res != PackageQuery.RESOLUTION_NONE) {
                    pd.resolution = res
                }
                rtn.add(pd)
            }
        }
        return rtn
    }

    @Throws(BundleException::class)
    private fun toPackageQuery(value: String?): PackageQuery? {
        // name(&(osgi.wiring.package=org.jboss.logging)(version>=3.3.0)(!(version>=4.0.0)))
        var index: Int = value.indexOf("(osgi.wiring.package")
        if (index == -1) {
            return null
        }
        var start: Int = value.indexOf('=', index)
        var end: Int = value.indexOf(')', index)
        if (start == -1 || end == -1 || end < start) {
            return null
        }
        val name: String = value.substring(start + 1, end).trim()
        val pd = PackageQuery(name)
        var last = end
        var op: Int
        var not: Boolean
        // version
        while (value.indexOf("(version", last).also { index = it } != -1) {
            op = -1
            end = value.indexOf(')', index)
            start = value.indexOf("<=", index)
            if (start != -1 && start < end) {
                op = VersionDefinition.LTE
                start += 2
            } else {
                start = value.indexOf(">=", index)
                if (start != -1 && start < end) {
                    op = VersionDefinition.GTE
                    start += 2
                } else {
                    start = value.indexOf("==", index)
                    if (start != -1 && start < end) {
                        op = VersionDefinition.EQ
                        start += 2
                    } else {
                        start = value.indexOf("!=", index)
                        if (start != -1 && start < end) {
                            op = VersionDefinition.NEQ
                            start += 2
                        } else {
                            start = value.indexOf("=", index)
                            if (start != -1 && start < end) {
                                op = VersionDefinition.EQ
                                start += 1
                            } else {
                                start = value.indexOf("<", index)
                                if (start != -1 && start < end) {
                                    op = VersionDefinition.LT
                                    start += 1
                                } else {
                                    start = value.indexOf(">", index)
                                    if (start != -1 && start < end) {
                                        op = VersionDefinition.GT
                                        start += 1
                                    }
                                }
                            }
                        }
                    }
                }
            }
            not = value.charAt(index - 1) === '!'
            last = end
            if (op == -1 || start == -1 || end == -1 || end < start) continue
            pd.addVersion(op, value.substring(start, end).trim(), not)
        }
        return pd
    }

    @Throws(IOException::class, BundleException::class)
    private fun _loadBundle(context: BundleContext?, bundle: File?): Bundle? {
        return _loadBundle(context, bundle.getAbsolutePath(), FileInputStream(bundle), true)
    }

    @Throws(IOException::class, BundleException::class)
    private fun _loadBundle(context: BundleContext?, bundle: Resource?): Bundle? {
        return _loadBundle(context, bundle.getAbsolutePath(), bundle.getInputStream(), true)
    }

    private fun log(level: Int, msg: String?) {
        try {
            val log: Log = ThreadLocalPageContext.getLog("application")
            if (log != null) log.log(level, "OSGi", msg)
        } catch (t: Exception) {
            LogUtil.log(level, BundleBuilderFactory::class.java.getName(), msg)
        }
    }

    private fun log(t: Throwable?) {
        try {
            val log: Log = ThreadLocalPageContext.getLog("application")
            if (log != null) log.log(Log.LEVEL_ERROR, "OSGi", t)
        } catch (_t: Exception) {
            /* this can fail when called from an old loader */
            LogUtil.log(OSGiUtil::class.java.getName(), _t)
        }
    }

    fun toState(state: Int, defaultValue: String?): String? {
        when (state) {
            Bundle.ACTIVE -> return "active"
            Bundle.INSTALLED -> return "installed"
            Bundle.UNINSTALLED -> return "uninstalled"
            Bundle.RESOLVED -> return "resolved"
            Bundle.STARTING -> return "starting"
            Bundle.STOPPING -> return "stopping"
        }
        return defaultValue
    }

    /**
     * value can be a String (for a single entry) or a List<String> for multiple entries
     *
     * @param b
     * @return
    </String> */
    fun getHeaders(b: Bundle?): Map<String?, Object?>? {
        val headers: Dictionary<String?, String?> = b.getHeaders()
        val keys: Enumeration<String?> = headers.keys()
        val values: Enumeration<String?> = headers.elements()
        var key: String
        var value: String
        var existing: Object
        var list: List<String?>?
        val _headers: Map<String?, Object?> = HashMap<String?, Object?>()
        while (keys.hasMoreElements()) {
            key = keys.nextElement()
            value = StringUtil.unwrap(values.nextElement())
            existing = _headers[key]
            if (existing != null) {
                if (existing is String) {
                    list = ArrayList()
                    list.add(existing as String)
                    _headers.put(key, list)
                } else list = existing
                list.add(value)
            } else _headers.put(key, value)
        }
        return _headers
    }

    val bootdelegation: Array<String?>?
        get() {
            if (bootDelegation == null) {
                var `is`: InputStream? = null
                try {
                    val prop = Properties()
                    `is` = OSGiUtil::class.java.getClassLoader().getResourceAsStream("default.properties")
                    prop.load(`is`)
                    var bd: String = prop.getProperty("org.osgi.framework.bootdelegation")
                    if (!StringUtil.isEmpty(bd)) {
                        bd += ",java.lang,java.lang.*"
                        bootDelegation = ListUtil.trimItems(ListUtil.listToStringArray(StringUtil.unwrap(bd), ','))
                    }
                } catch (ioe: IOException) {
                } finally {
                    IOUtil.closeEL(`is`)
                }
            }
            return if (bootDelegation == null) arrayOfNulls<String?>(0) else bootDelegation
        }

    fun isClassInBootelegation(className: String?): Boolean {
        return isInBootelegation(className, false)
    }

    fun isPackageInBootelegation(className: String?): Boolean {
        return isInBootelegation(className, true)
    }

    private fun isInBootelegation(name: String?, isPackage: Boolean): Boolean {
        // extract package
        val pack: String?
        if (isPackage) pack = name else {
            val index: Int = name.lastIndexOf('.')
            if (index == -1) return false
            pack = name.substring(0, index)
        }
        val arr = bootdelegation
        for (bd in arr!!) {
            bd = bd.trim()
            // with wildcard
            if (bd.endsWith(".*")) {
                bd = bd.substring(0, bd!!.length() - 1)
                if (pack.startsWith(bd)) return true
            } else {
                if (bd!!.equals(pack)) return true
            }
        }
        return false
    }

    fun toBundleDefinitions(bundles: Array<BundleInfo?>?): Array<BundleDefinition?>? {
        if (bundles == null) return arrayOfNulls<BundleDefinition?>(0)
        val rtn = arrayOfNulls<BundleDefinition?>(bundles.size)
        for (i in bundles.indices) {
            rtn[i] = bundles[i]!!.toBundleDefinition()
        }
        return rtn
    }

    fun getFrameworkBundle(config: Config?, defaultValue: Bundle?): Bundle? {
        val bundles: Array<Bundle?> = ConfigWebUtil.getEngine(config).getBundleContext().getBundles()
        var b: Bundle? = null
        for (i in bundles.indices) {
            b = bundles[i]
            if (b != null && isFrameworkBundle(b)) return b
        }
        return defaultValue
    }

    fun isFrameworkBundle(b: Bundle?): Boolean { // FELIX specific
        return "org.apache.felix.framework".equalsIgnoreCase(b.getSymbolicName()) // TODO move to cire util class tha does not exist yet
    }

    fun getBundleFromClass(clazz: Class?, defaultValue: Bundle?): Bundle? {
        val cl: ClassLoader = clazz.getClassLoader()
        return if (cl is BundleClassLoader) {
            (cl as BundleClassLoader).getBundle()
        } else defaultValue
    }

    // DataMember
    val classPath: String?
        get() {
            val bcl: BundleClassLoader = OSGiUtil::class.java.getClassLoader() as BundleClassLoader
            val bundle: Bundle = bcl.getBundle()
            val bc: BundleContext = bundle.getBundleContext()
            // DataMember
            val set: Set<String?> = HashSet()
            set.add(ClassUtil.getSourcePathForClass(CFMLEngineFactory::class.java, null))
            set.add(ClassUtil.getSourcePathForClass(javax.servlet.jsp.JspException::class.java, null))
            set.add(ClassUtil.getSourcePathForClass(javax.servlet.Servlet::class.java, null))
            val sb = StringBuilder()
            for (path in set) {
                sb.append(path).append(File.pathSeparator)
            }
            for (b in bc.getBundles()) {
                if ("System Bundle".equalsIgnoreCase(b.getLocation())) continue
                sb.append(b.getLocation()).append(File.pathSeparator)
            }
            return sb.toString()
        }

    @Throws(BundleException::class)
    fun stop(clazz: Class?) {
        if (clazz == null) return
        val bundleCore: Bundle? = getBundleFromClass(CFMLEngineImpl::class.java, null)
        val bundleFromClass: Bundle? = getBundleFromClass(clazz, null)
        if (bundleFromClass != null && !bundleFromClass.equals(bundleCore)) {
            stopIfNecessary(bundleFromClass)
        }
        // TODO Auto-generated method stub
    }

    fun isValid(obj: Object?): Boolean {
        if (obj != null) {
            val cl: ClassLoader = obj.getClass().getClassLoader()
            if (cl is BundleClassLoader) {
                if (((cl as BundleClassLoader).getBundle() as Bundle).getState() !== Bundle.ACTIVE) return false
            }
        }
        return true
    }

    private class Filter : FilenameFilter, ResourceNameFilter {
        @Override
        fun accept(dir: File?, name: String?): Boolean {
            return accept(name)
        }

        @Override
        fun accept(dir: Resource?, name: String?): Boolean {
            return accept(name)
        }

        private fun accept(name: String?): Boolean {
            return name.endsWith(".jar")
        }
    }

    class VersionDefinition(version: Version?, op: Int, not: Boolean) : Serializable {
        private val version: Version?
        val op: Int
        fun matches(v: Version?): Boolean {
            if (EQ == op) return v.compareTo(version) === 0
            if (LTE == op) return v.compareTo(version) <= 0
            if (LT == op) return v.compareTo(version) < 0
            if (GTE == op) return v.compareTo(version) >= 0
            if (GT == op) return v.compareTo(version) > 0
            return if (NEQ == op) v.compareTo(version) !== 0 else false
        }

        fun getVersion(): Version? {
            return version
        }

        val versionAsString: String?
            get() = if (version == null) null else version.toString()

        @Override
        override fun toString(): String {
            val sb = StringBuilder("version ")
            sb.append(opAsString).append(' ').append(version)
            return sb.toString()
        }

        val opAsString: String?
            get() {
                when (op) {
                    EQ -> return "EQ"
                    LTE -> return "LTE"
                    GTE -> return "GTE"
                    NEQ -> return "NEQ"
                    LT -> return "LT"
                    GT -> return "GT"
                }
                return null
            }

        companion object {
            private const val serialVersionUID = 4915024473510761950L
            const val LTE = 1
            const val GTE = 2
            const val EQ = 4
            const val LT = 8
            const val GT = 16
            const val NEQ = 32
        }

        init {
            var op = op
            var not = not
            this.version = version
            if (not) {
                if (op == LTE) {
                    op = GT
                    not = false
                } else if (op == LT) {
                    op = GTE
                    not = false
                } else if (op == GTE) {
                    op = LT
                    not = false
                } else if (op == GT) {
                    op = LTE
                    not = false
                } else if (op == EQ) {
                    op = NEQ
                    not = false
                } else if (op == NEQ) {
                    op = EQ
                    not = false
                }
            }
            this.op = op
        }
    }

    class PackageQuery(val name: String?) {
        val versionDefinitons: List<VersionDefinition?>? = ArrayList<VersionDefinition?>()
        var resolution = RESOLUTION_NONE
        val isRequired: Boolean
            get() = resolution == RESOLUTION_NONE

        @Throws(BundleException::class)
        fun addVersion(op: Int, version: String?, not: Boolean) {
            versionDefinitons.add(VersionDefinition(toVersion(version), op, not))
        }

        @Override
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("name:").append(name)
            val it = versionDefinitons!!.iterator()
            while (it.hasNext()) {
                sb.append(';').append(it.next())
            }
            return sb.toString()
        }

        companion object {
            const val RESOLUTION_NONE = 0
            const val RESOLUTION_DYNAMIC = 1
            const val RESOLUTION_OPTIONAL = 2
            fun toResolution(value: String?, defaultValue: Int): Int {
                var value = value
                if (!StringUtil.isEmpty(value, true)) {
                    value = value.trim().toLowerCase()
                    if ("dynamic".equals(value)) return RESOLUTION_DYNAMIC
                    if ("optional".equals(value)) return RESOLUTION_OPTIONAL
                }
                return defaultValue
            }
        }
    }

    class PackageDefinition(val name: String?) {
        private var version: Version? = null
        @Throws(BundleException::class)
        fun setVersion(version: String?) {
            this.version = toVersion(version)
        }

        fun setVersion(version: Version?) {
            this.version = version
        }

        fun getVersion(): Version? {
            return version
        }

        @Override
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("name:").append(name)
            sb.append("version:").append(version)
            return sb.toString()
        }
    }

    class BundleDefinition : Serializable {
        val name: String?
        private var bundle: Bundle? = null
        var versionDefiniton: VersionDefinition? = null
            private set

        constructor(name: String?) {
            this.name = name
        }

        constructor(name: String?, version: String?) {
            this.name = name
            if (name == null) throw IllegalArgumentException("Name cannot be null")
            if (!StringUtil.isEmpty(version, true)) setVersion(VersionDefinition.EQ, version)
        }

        constructor(name: String?, version: Version?) {
            this.name = name
            if (name == null) throw IllegalArgumentException("Name cannot be null")
            if (version != null) setVersion(VersionDefinition.EQ, version)
        }

        constructor(bundle: Bundle?) {
            name = bundle.getSymbolicName()
            if (name == null) throw IllegalArgumentException("Name cannot be null")
            if (bundle.getVersion() != null) setVersion(VersionDefinition.EQ, bundle.getVersion())
            this.bundle = bundle
        }

        /**
         * only return a bundle if already loaded, does not load the bundle
         *
         * @return
         */
        val loadedBundle: Bundle?
            get() = bundle

        @Throws(BundleException::class)
        fun getBundle(id: Identification?, addional: List<Resource?>?, startIfNecessary: Boolean): Bundle? {
            return getBundle(id, addional, startIfNecessary, false)
        }

        @Throws(BundleException::class)
        fun getBundle(id: Identification?, addional: List<Resource?>?, startIfNecessary: Boolean, versionOnlyMattersForDownload: Boolean): Bundle? {
            if (bundle == null) {
                bundle = loadBundle(name, version, id, addional, startIfNecessary, versionOnlyMattersForDownload)
            }
            return bundle
        }

        /**
         * get Bundle, also load if necessary from local or remote
         *
         * @return
         * @throws BundleException
         * @throws StartFailedException
         */
        @Throws(BundleException::class)
        fun getBundle(config: Config?, addional: List<Resource?>?): Bundle? {
            return getBundle(config, addional, false)
        }

        @Throws(BundleException::class)
        fun getBundle(config: Config?, addional: List<Resource?>?, versionOnlyMattersForDownload: Boolean): Bundle? {
            var config: Config? = config
            if (bundle == null) {
                config = ThreadLocalPageContext.getConfig(config)
                bundle = loadBundle(name, version, if (config == null) null else config.getIdentification(), addional, false, versionOnlyMattersForDownload)
            }
            return bundle
        }

        fun getLocalBundle(addional: List<Resource?>?): Bundle? {
            if (bundle == null) {
                bundle = loadBundleFromLocal(name, version, addional, true, null)
            }
            return bundle
        }

        @Throws(BundleException::class)
        fun getBundleFile(downloadIfNecessary: Boolean, addional: List<Resource?>?): BundleFile? {
            val config: Config = ThreadLocalPageContext.getConfig()
            return getBundleFile(name, version, if (config == null) null else config.getIdentification(), addional, downloadIfNecessary)
        }

        val op: Int
            get() = if (versionDefiniton == null) VersionDefinition.EQ else versionDefiniton!!.op
        val version: Version?
            get() = if (versionDefiniton == null) null else versionDefiniton!!.getVersion()
        val versionAsString: String?
            get() = if (versionDefiniton == null) null else versionDefiniton!!.versionAsString

        @Throws(BundleException::class)
        fun setVersion(op: Int, version: String?) {
            setVersion(op, toVersion(version))
        }

        fun setVersion(op: Int, version: Version?) {
            versionDefiniton = VersionDefinition(version, op, false)
        }

        @Override
        override fun toString(): String {
            return "name:" + name + ";version:" + versionDefiniton + ";"
        }

        @Override
        override fun equals(obj: Object?): Boolean {
            if (this === obj) return true
            return if (obj !is BundleDefinition) false else toString().equals(obj.toString())
        }
    }

    init {
        // this is needed in case old version of extensions are used, because lucee no longer bundles this
        packageBundleMapping.put("org.bouncycastle", "bcprov")
        packageBundleMapping.put("org.apache.log4j", "log4j")
    }
}