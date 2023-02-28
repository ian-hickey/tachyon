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
package lucee.runtime.engine

import java.io.InputStream

/**
 * Info to this Version
 */
class InfoImpl @JvmOverloads constructor(bundle: Bundle? = null) : Info {
    // Mod this
    private var releaseDate: DateTime? = null
    private var versionName: String? = null
    private var versionNameExplanation: String? = null
    private val releaseTime: Long
    private var version: Version? = null
    private var level: String? = null
    private var requiredExtensions: List<ExtensionDefintion?>? = null

    /**
     * @return the level
     */
    @Override
    fun getLevel(): String? {
        return level
    }
    // Version <version>.<major>.<minor>.<patches>
    /**
     * @return Returns the releaseDate.
     */
    fun getRealeaseDate(): DateTime? {
        return releaseDate
    }

    /**
     * @return Returns the releaseTime.
     */
    @Override
    fun getRealeaseTime(): Long {
        return releaseTime
    }

    @Override
    fun getVersion(): Version? {
        return version
    }

    fun getRequiredExtension(): List<ExtensionDefintion?>? {
        return requiredExtensions
    }
    /**
     * @return returns the state
     *
     * @Override public int getStateAsInt() { return state; }
     */
    /**
     * @return returns the state
     *
     * @Override public String getStateAsString() { return strState; }
     */
    /*
	 * *
	 * 
	 * @return returns the state
	 * 
	 * public static String toStringState(int state) { if(state==STATE_FINAL) return "final"; else
	 * if(state==STATE_BETA) return "beta"; else if(state==STATE_RC) return "rc"; else return "alpha"; }
	 */
    /*
	 * *
	 * 
	 * @return returns the state
	 * 
	 * public int toIntState(String state) { state=state.trim().toLowerCase(); if("final".equals(state))
	 * return STATE_FINAL; else if("beta".equals(state)) return STATE_BETA; else if("rc".equals(state))
	 * return STATE_RC; else return STATE_ALPHA; }
	 */
    @Override
    fun getVersionName(): String? {
        return versionName
    }

    @Override
    fun getVersionNameExplanation(): String? {
        return versionNameExplanation
    }

    @Override
    fun getFullVersionInfo(): Long {
        return KeyImpl.createHash64(getVersion().toString()) // +state;
    }

    @Override
    fun getCFMLTemplateExtensions(): Array<String?>? {
        return Constants.getCFMLTemplateExtensions()
    }

    @Override
    fun getLuceeTemplateExtensions(): Array<String?>? {
        return Constants.getLuceeTemplateExtensions()
    }

    @Override
    fun getCFMLComponentExtensions(): Array<String?>? {
        return arrayOf(getCFMLComponentExtension())
    }

    @Override
    fun getLuceeComponentExtensions(): Array<String?>? {
        return arrayOf(getLuceeComponentExtension())
    }

    @Override
    fun getCFMLComponentExtension(): String? {
        return Constants.getCFMLComponentExtension()
    }

    @Override
    fun getLuceeComponentExtension(): String? {
        return Constants.getLuceeComponentExtension()
    }

    companion object {
        const val STATE_ALPHA = 2 * 100000000
        const val STATE_BETA = 1 * 100000000
        const val STATE_RC = 3 * 100000000
        const val STATE_FINAL = 0
        fun getDefaultProperties(bundle: Bundle?): Properties? {
            var `is`: InputStream? = null
            var prop: Properties? = Properties()
            val keyToValidate = "felix.log.level"
            return try {
                // check the bundle for the default.properties
                if (bundle != null) {
                    try {
                        `is` = bundle.getEntry("default.properties").openStream()
                        prop.load(`is`)
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                    } finally {
                        IOUtil.closeEL(`is`)
                    }
                }
                if (prop.getProperty(keyToValidate) != null) return prop

                // try from core classloader without leading slash
                prop = Properties()
                val clazz: Class = PageSourceImpl::class.java
                val cl: ClassLoader = clazz.getClassLoader()
                try {
                    `is` = cl.getResourceAsStream("default.properties")
                    prop.load(`is`)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                } finally {
                    IOUtil.closeEL(`is`)
                }
                if (prop.getProperty(keyToValidate) != null) return prop

                // try from core classloader with leading slash
                prop = Properties()
                try {
                    `is` = cl.getResourceAsStream("/default.properties")
                    prop.load(`is`)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                } finally {
                    IOUtil.closeEL(`is`)
                }
                if (prop.getProperty(keyToValidate) != null) return prop

                // try from core class with leading slash
                prop = Properties()
                try {
                    `is` = clazz.getResourceAsStream("/default.properties")
                    prop.load(`is`)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                } finally {
                    IOUtil.closeEL(`is`)
                }
                if (prop.getProperty(keyToValidate) != null) return prop
                prop = Properties()
                try {
                    `is` = clazz.getResourceAsStream("../../default.properties")
                    prop.load(`is`)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                } finally {
                    IOUtil.closeEL(`is`)
                }
                if (prop.getProperty(keyToValidate) != null) prop else Properties()
            } finally {
                IOUtil.closeEL(`is`)
            }
        }

        fun getManifest(bundle: Bundle?): Manifest? {
            val `is`: InputStream? = null
            var manifest: Manifest?
            return try {
                // check the bundle for the default.properties
                if (bundle != null) {
                    try {
                        manifest = load(bundle.getEntry("META-INF/MANIFEST.MF").openStream())
                        if (manifest != null) return manifest
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                    }
                }

                // try from core classloader without leading slash
                val clazz: Class = PageSourceImpl::class.java
                val cl: ClassLoader = clazz.getClassLoader()
                manifest = load(cl.getResourceAsStream("META-INF/MANIFEST.MF"))
                if (manifest != null) return manifest

                // try from core classloader with leading slash
                manifest = load(cl.getResourceAsStream("/META-INF/MANIFEST.MF"))
                if (manifest != null) return manifest

                // try from core class with leading slash
                manifest = load(clazz.getResourceAsStream("/META-INF/MANIFEST.MF"))
                if (manifest != null) return manifest
                manifest = load(clazz.getResourceAsStream("../../META-INF/MANIFEST.MF"))
                if (manifest != null) return manifest

                // check all resources
                try {
                    val e: Enumeration<URL?> = cl.getResources("META-INF/MANIFEST.MF")
                    while (e.hasMoreElements()) {
                        manifest = load(e.nextElement().openStream())
                        if (manifest != null) return manifest
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                null
            } finally {
                IOUtil.closeEL(`is`)
            }
        }

        private fun load(`is`: InputStream?): Manifest? {
            try {
                val m = Manifest(`is`)
                val sn: String = m.getMainAttributes().getValue("Bundle-SymbolicName")
                if ("lucee.core".equals(sn)) return m
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            } finally {
                IOUtil.closeEL(`is`)
            }
            return null
        }

        private fun valid(manifest: Manifest?): Boolean {
            return false
        }

        fun toIntVersion(version: String?, defaultValue: Int): Int {
            return try {
                val aVersion: Array<String?> = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(version, '.'))
                val ma: Int = Caster.toIntValue(aVersion[0])
                val mi: Int = Caster.toIntValue(aVersion[1])
                val re: Int = Caster.toIntValue(aVersion[2])
                val pa: Int = Caster.toIntValue(aVersion[3])
                ma * 1000000 + mi * 10000 + re * 100 + pa
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                defaultValue
            }
        }
    }

    // private int state;
    // private final String strState;
    init {
        try {
            val manifest: Manifest = getManifest(bundle)
                    ?: throw IllegalArgumentException("Failed to get manifest from bundle")
            val mf: Attributes = manifest.getMainAttributes()
            versionName = mf.getValue("Minor-Name")
            if (versionName == null) throw RuntimeException("missing Minor-Name")
            versionNameExplanation = mf.getValue("Minor-Name-Explanation")
            releaseDate = DateCaster.toDateAdvanced(mf.getValue("Built-Date"), null)
            // state=toIntState(mf.getValue("State"));
            level = "os"
            version = OSGiUtil.toVersion(mf.getValue("Bundle-Version"))
            val str: String = mf.getValue("Require-Extension")
            if (StringUtil.isEmpty(str, true)) requiredExtensions = ArrayList<ExtensionDefintion?>() else requiredExtensions = RHExtension.toExtensionDefinitions(str)

            // ListUtil.trimItems(ListUtil.listToStringArray(str, ','));
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw PageRuntimeException(Caster.toPageException(t))
        }
        releaseTime = releaseDate.getTime()
        // strState=toStringState(state);
    }
}