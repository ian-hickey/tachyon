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
package tachyon.loader.osgi

import java.io.File

object BundleLoader {
    @Throws(IOException::class, BundleException::class)
    fun loadBundles(engFac: CFMLEngineFactory, cacheRootDir: File?, jarDirectory: File, rc: File, old: BundleCollection?): BundleCollection {
        // if (rc.getName().toLowerCase().toLowerCase().indexOf("ehcache") != -1)
        // System. err.println(rc.getName());
        val jf = JarFile(rc) // TODO this should work in any case, but we should still improve this code
        return try {
            // Manifest
            val mani: Manifest = jf.getManifest()
                    ?: throw IOException("tachyon core [$rc] is invalid, there is no META-INF/MANIFEST.MF File")
            val attrs: Attributes = mani.getMainAttributes()

            // default properties
            val defProp: Properties = loadDefaultProperties(jf)

            // Get data from Manifest and default.properties

            // Tachyon Core Version
            // String rcv = unwrap(defProp.getProperty("tachyon.core.version"));
            // if(Util.isEmpty(rcv)) throw new IOException("tachyon core ["+rc+"] is invalid, no core version is
            // defined in the {Tachyon-Core}/default.properties File");
            // int version = CFMLEngineFactory.toInVersion(rcv);

            // read the config from default.properties
            val config: Map<String, Object> = HashMap<String, Object>()
            run {
                val it: Iterator<Entry<Object, Object>> = defProp.entrySet().iterator()
                var e: Entry<Object, Object>
                var k: String
                while (it.hasNext()) {
                    e = it.next()
                    k = e.getKey()
                    if (!k.startsWith("org.") && !k.startsWith("felix.")) continue
                    config.put(k, CFMLEngineFactorySupport.removeQuotes(e.getValue() as String, true))
                }
            }

            // close all bundles
            var felix: Felix
            if (old != null) {
                removeBundlesEL(old)
                felix = old.felix
                // stops felix (wait for it)
                BundleUtil.stop(felix, false)
                felix = engFac.getFelix(cacheRootDir, config)
            } else felix = engFac.getFelix(cacheRootDir, config)
            val bc: BundleContext = felix.getBundleContext()

            // get bundle needed for that core
            val rb: String = attrs.getValue("Require-Bundle")
            if (Util.isEmpty(rb)) throw IOException("tachyon core [$rc] is invalid, no Require-Bundle definition found in the META-INF/MANIFEST.MF File")

            // get fragments needed for that core (Tachyon specific Key)
            val rbf: String = attrs.getValue("Require-Bundle-Fragment")

            // load Required/Available Bundles
            val requiredBundles = readRequireBundle(rb) // Require-Bundle
            val requiredBundleFragments = readRequireBundle(rbf) // Require-Bundle-Fragment
            val availableBundles: Map<String, File> = loadAvailableBundles(jarDirectory)

            // deploys bundled bundles to bundle directory
            // deployBundledBundles(jarDirectory, availableBundles);

            // Add Required Bundles
            var e: Entry<String?, String?>
            var f: File?
            var id: String
            val bundles: List<Bundle> = ArrayList<Bundle>()
            var it: Iterator<Entry<String?, String?>> = requiredBundles.entrySet().iterator()
            while (it.hasNext()) {
                e = it.next()
                id = e.getKey().toString() + "|" + e.getValue()
                f = availableBundles[id]
                // StringBuilder sb=new StringBuilder();
                if (f == null) {
                    /*
					 * sb.append(id+"\n"); Iterator<String> _it = availableBundles.keySet().iterator();
					 * while(_it.hasNext()){ sb.append("- "+_it.next()+"\n"); } throw new
					 * RuntimeException(sb.toString());
					 */
                }
                if (f == null) f = engFac.downloadBundle(e.getKey(), e.getValue(), null)
                bundles.add(BundleUtil.addBundle(engFac, bc, f, null))
            }

            // Add Required Bundle Fragments
            val fragments: List<Bundle> = ArrayList<Bundle>()
            it = requiredBundleFragments.entrySet().iterator()
            while (it.hasNext()) {
                e = it.next()
                id = e.getKey().toString() + "|" + e.getValue()
                f = availableBundles[id]
                if (f == null) f = engFac.downloadBundle(e.getKey(), e.getValue(), null) // if identification is not defined, it is loaded from the CFMLEngine
                fragments.add(BundleUtil.addBundle(engFac, bc, f, null))
            }

            // Add Tachyon core Bundle
            val bundle: Bundle
            // bundles.add(bundle = BundleUtil.addBundle(engFac, bc, rc,null));
            bundle = BundleUtil.addBundle(engFac, bc, rc, null)

            // Start the bundles
            BundleUtil.start(engFac, bundles)
            BundleUtil.start(engFac, bundle)
            BundleCollection(felix, bundle, bundles)
        } finally {
            if (jf != null) try {
                jf.close()
            } catch (ioe: IOException) {
            }
        }
    }

    private fun loadAvailableBundles(jarDirectory: File): Map<String, File> {
        val rtn: Map<String, File> = HashMap<String, File>()
        val jars: Array<File> = jarDirectory.listFiles()
        if (jars != null) for (i in jars.indices) {
            if (!jars[i].isFile() || !jars[i].getName().endsWith(".jar")) continue
            try {
                rtn.put(loadBundleInfo(jars[i]), jars[i])
            } catch (ioe: IOException) {
                Exception("Error loading bundle info for [" + jars[i].toString().toString() + "]", ioe).printStackTrace()
            }
        }
        return rtn
    }

    @Throws(IOException::class)
    fun loadBundleInfo(jar: File): String {
        val jf = JarFile(jar)
        return try {
            val attrs: Attributes = jf.getManifest().getMainAttributes()
            val symbolicName: String = attrs.getValue("Bundle-SymbolicName")
            val version: String = attrs.getValue("Bundle-Version")
            if (Util.isEmpty(symbolicName)) throw IOException("OSGi bundle [$jar] is invalid, {Tachyon-Core}META-INF/MANIFEST.MF does not contain a \"Bundle-SymbolicName\"")
            if (Util.isEmpty(version)) throw IOException("OSGi bundle [$jar] is invalid, {Tachyon-Core}META-INF/MANIFEST.MF does not contain a \"Bundle-Version\"")
            "$symbolicName|$version"
        } finally {
            Util.closeEL(jf)
        }
    }

    @Throws(IOException::class)
    private fun readRequireBundle(rb: String): Map<String, String> {
        val rtn: HashMap<String, String> = HashMap<String, String>()
        if (Util.isEmpty(rb)) return rtn
        val st = StringTokenizer(rb, ",")
        var stl: StringTokenizer
        var line: String
        var jarName: String
        var jarVersion: String? = null
        var token: String
        var index: Int
        while (st.hasMoreTokens()) {
            line = st.nextToken().trim()
            if (Util.isEmpty(line)) continue
            stl = StringTokenizer(line, ";")

            // first is the name
            jarName = stl.nextToken().trim()
            while (stl.hasMoreTokens()) {
                token = stl.nextToken().trim()
                if (token.startsWith("bundle-version") && token.indexOf('=').also { index = it } != -1) jarVersion = token.substring(index + 1).trim()
            }
            if (jarVersion == null) throw IOException("missing \"bundle-version\" info in the following \"Require-Bundle\" record: \"$jarName\"")
            rtn.put(jarName, jarVersion)
        }
        return rtn
    }

    /*
	 * private static String unwrap(String str) { return str == null ? null :
	 * CFMLEngineFactory.removeQuotes(str, true); }
	 */
    @Throws(IOException::class)
    fun loadDefaultProperties(jf: JarFile): Properties {
        val ze: ZipEntry = jf.getEntry("default.properties")
                ?: throw IOException("the Tachyon core has no default.properties file!")
        val prop = Properties()
        var `is`: InputStream? = null
        try {
            `is` = jf.getInputStream(ze)
            prop.load(`is`)
        } finally {
            CFMLEngineFactorySupport.closeEL(`is`)
        }
        return prop
    }

    @Throws(BundleException::class)
    fun removeBundles(bc: BundleContext) {
        val bundles: Array<Bundle> = bc.getBundles()
        for (bundle in bundles) removeBundle(bundle)
    }

    @Throws(BundleException::class)
    fun removeBundles(bc: BundleCollection) {
        val bcc: BundleContext = bc.getBundleContext()
        val bundles: Array<Bundle?> = if (bcc == null) arrayOfNulls<Bundle>(0) else bcc.getBundles()

        // stop
        for (bundle in bundles) {
            if (!BundleUtil.isSystemBundle(bundle)) {
                stopBundle(bundle)
            }
        }
        // uninstall
        for (bundle in bundles) {
            if (!BundleUtil.isSystemBundle(bundle)) {
                uninstallBundle(bundle)
            }
        }
    }

    fun removeBundlesEL(bc: BundleCollection) {
        val bcc: BundleContext = bc.getBundleContext()
        val bundles: Array<Bundle?> = if (bcc == null) arrayOfNulls<Bundle>(0) else bcc.getBundles()
        for (bundle in bundles) {
            if (!BundleUtil.isSystemBundle(bundle)) {
                try {
                    stopBundle(bundle)
                } catch (e: BundleException) {
                    e.printStackTrace()
                }
            }
        }
        for (bundle in bundles) {
            if (!BundleUtil.isSystemBundle(bundle)) {
                try {
                    uninstallBundle(bundle)
                } catch (e: BundleException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(BundleException::class)
    fun removeBundle(bundle: Bundle?) {
        stopBundle(bundle)
        uninstallBundle(bundle)
    }

    @Throws(BundleException::class)
    fun uninstallBundle(bundle: Bundle?) {
        if (bundle == null) return
        if (bundle.getState() === Bundle.ACTIVE || bundle.getState() === Bundle.STARTING || bundle.getState() === Bundle.STOPPING) stopBundle(bundle)
        if (bundle.getState() !== Bundle.UNINSTALLED) {
            bundle.uninstall()
        }
    }

    @Throws(BundleException::class)
    fun stopBundle(bundle: Bundle?) {
        if (bundle == null) return

        // wait for starting/stopping
        var sleept = 0
        while (bundle.getState() === Bundle.STOPPING || bundle.getState() === Bundle.STARTING) {
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                break
            }
            sleept += 10
            if (sleept > 5000) break // only wait for 5 seconds
        }

        // force stopping (even when still starting)
        if (bundle.getState() === Bundle.ACTIVE || bundle.getState() === Bundle.STARTING) BundleUtil.stop(bundle, false)
    }
}